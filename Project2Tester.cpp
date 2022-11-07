#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <limits.h>
#include <stdlib.h>
#include <iostream>
#include <vector>
#include <map>
#include <fstream>
#include <array>
#include <queue>
#include <utility>

/*

Tester Arguments: (Base Config Name. I.E. No .txt) (# of nodes)

Outputs to terminal:
# of files Checked (Not done yet)
# of Conflicts found
# of Expected Conflicts found.

*/

std::priority_queue<std::pair<int,int>, std::vector<std::pair<int,int>>, std::greater<std::pair<int,int>> > timeStamps;

std::map<std::pair<int,int>,int> testTimeStamps;

int parsingFile(std::string fileToParse){

	// Creating the file reader from first argument
	std::ifstream configReader(fileToParse);

	if (!configReader.is_open()) {
		std::cout << "Cannot find file." << std::endl;
		return -1;
	}

	// Variables for parser.
	std::vector<std::string> tokens;
	bool readingToken = false;
	std::string token = "";
	bool valid = true;

	std::string fileLine;

	while (std::getline(configReader, fileLine)) {
		
		//std::cout << fileLine << std::endl;

		if (fileLine.size() == 0) continue;

		// Checking for nextLine character b/c for some reason just '\n' doesn't work.
		if (fileLine.back() == 13){
			fileLine.pop_back();
		}

		//Tokenize the given line.
		for (int i = 0; i < fileLine.size(); i++) {
			// If reading a token and a space is hit then must be end of token.
			if (fileLine.at(i) == ' ' && readingToken) {
				tokens.push_back(token);
				token = "";
				readingToken = false;
			}

			//If a # character is found then just save last token and exit.
			if (fileLine.at(i) == '#' && readingToken) {
				tokens.push_back(token);
				token = "";
				readingToken = false;
				break;
			}
			else if (fileLine.at(i) == '#') {
				token = "";
				readingToken = false;
				break;
			}

			// If character that is not a newline then
			// if not currently readingToken. Start readingtoken.
			// push the char into a string that will be the token.
			if (fileLine.at(i) != ' ' && fileLine.at(i) != 13) {
				if (!readingToken) readingToken = true;
				token.push_back(fileLine.at(i));
			}

			// If end of line then store token.
			if (i == fileLine.size() - 1 && readingToken) {
				tokens.push_back(token);
				token = "";
				readingToken = false;
			}
			
			if(tokens.size() > 4){
				break;
			}

		}

		valid = true;

		if (tokens.size() != 0) {
			//Are we checking for Global parameters and are all the tokens valid if so?
			if (tokens.size() == 2) {
				int prevInt = 0;
				int tokenLen = tokens.size();
				while (tokenLen != 0) {
					tokenLen--;
					// are the charcters in the token digits.
					for (int j = 0; j < tokens.at(tokenLen).length(); j++) {
						if (!std::isdigit(tokens.at(tokenLen).at(j))) {
							valid = false;
							break;
						}
					}

					if (valid) {
						if(tokenLen == 1){
							prevInt = std::stoi(tokens.at(tokenLen));
						}else{
							timeStamps.push(std::make_pair(std::stoi(tokens.at(tokenLen)),prevInt));
						}
					}
					else {
						break;
					}
				}// End of while
			} else if(tokens.size() == 3){

				int prevInt = 0;
				int tokenLen = 2;
				while (tokenLen != 0) {
					tokenLen--;
					// are the charcters in the token digits.
					for (int j = 0; j < tokens.at(tokenLen).length(); j++) {
						if (!std::isdigit(tokens.at(tokenLen).at(j))) {
							valid = false;
							break;
						}
					}

					if (valid) {
						if(tokenLen == 1){
							prevInt = std::stoi(tokens.at(tokenLen));
						}else{
							timeStamps.push(std::make_pair(std::stoi(tokens.at(tokenLen)),prevInt));
							if (tokens.at(2) == "T"){
								testTimeStamps[std::make_pair(std::stoi(tokens.at(tokenLen)),prevInt)];
							}
						}
					}
					else {
						break;
					}
				}// End of while

			}
		}
		tokens.clear();
		token = "";
		valid = true;

	} // End of While Loop

	configReader.close();
	return 0;
}

int main(int argc, char** argv)
{
	// Checking that there is only one argument
	if(argc != 3){
		std::cout << "Wrong number of arguments." << std::endl;
		return 0;
	}

	// Config file parser.
	/*

	Input: Command line argument string that is the config file name and # of nodes

	Output: # of critical section collision. # of which are not planned.
	*/

	std::string numNodesStr (argv[2]);

	// Checking that the second argument is an int:
	for (char numChar : numNodesStr){	

		if(!std::isdigit(numChar)){
			std:: cout << "Second argument is not a valid integer." << std::endl;
			return 0;
		}

	}

	int numNodes = std::stoi(numNodesStr);
	std::string baseFile (argv[1]);
	for (int i = 0; i < numNodes; i++){
		std::string fileName = baseFile+"_"+std::to_string(i)+".out";
		if (parsingFile(fileName) == -1){
			std::cout << "Invalid file name or file not found." << std::endl;
			return 0;
		}
	}

	if(timeStamps.empty()){

		std::cout << "No valid pairs found." << std::endl;
		return 0;

	}

	std::pair <int,int> prevPair = timeStamps.top();

	timeStamps.pop();

	int numOfConflicts = 0;
	int numOfTestConflicts = 0;

	while (!timeStamps.empty()){

		if(prevPair.second > timeStamps.top().first){

			numOfConflicts++;
			
			if(testTimeStamps.count(prevPair) == 1 || testTimeStamps.count(timeStamps.top()) == 1){
			
				std::cout<< "Test Conflict: " << std::to_string(timeStamps.top().first) << " " << std::to_string(timeStamps.top().second) << " with " << std::to_string(prevPair.first) << " " << std::to_string(prevPair.second) << std::endl;
				numOfTestConflicts++;
			}else{
				std::cout<< "Conflict: " << std::to_string(timeStamps.top().first) << " " << std::to_string(timeStamps.top().second) << " with " << std::to_string(prevPair.first) << " " << std::to_string(prevPair.second) << std::endl;
			}

		}

		prevPair = timeStamps.top();
		timeStamps.pop();

	}

	std::cout << "Number of Conflicts: " << std::to_string(numOfConflicts) << std::endl;
	std::cout << "Number of Expected Conflicts: " << std::to_string(numOfTestConflicts) << std::endl;

}

