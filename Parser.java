import java.util.*;
import java.io.*;
import java.net.InetAddress;
public class Parser {

    private List<NodeInfo> node_info = Collections.synchronizedList(new ArrayList<NodeInfo>());
    public int my_node_id;
    public void parseFile(String configFile)
    {
        try
        {
            // Variables for the parser
            ArrayList<String> tokens = new ArrayList<>();
            ArrayList<String> ipPort = new ArrayList<>();
            String token = "";
            String fileLine;
            boolean readingToken = false;
            boolean valid = true;
            boolean nodeIDCheckFlag = true;
            String hostName = InetAddress.getLocalHost().getHostName();
            int node = 0; 
            int stageOfConfig = 0;

            ArrayList<String> node_ips = new ArrayList<>();
            ArrayList<Integer> node_ports = new ArrayList<>();

            Scanner scan = new Scanner(new File(configFile)); 
            while(scan.hasNextLine())
            {
                fileLine = scan.nextLine();
                if (fileLine.length() == 0) continue;  // Ignore new lines

                // Tokenize the given line
                for (int i =0; i < fileLine.length(); i++)
                {
                    // If reading a token and a space is hit then must be end of token
                    if (fileLine.charAt(i) == ' ' && readingToken)
                    {
                        tokens.add(token);
                        token = "";
                        readingToken = false;
                    }

                    // If a # character is found then just save last token and exit
                    if (fileLine.charAt(i) == '#' && readingToken)
                    {
                        tokens.add(token);
                        token = "";
                        readingToken = false;
                        break;
                    }
                    else if (fileLine.charAt(i) == '#')
                    {
                        token = "";
                        readingToken = false;
                        break;
                    }
                    
                    // If character that is not a newline('\n') then
                    // if not currently readingToken, start reading tokens.
                    // Push the char into a string that will be the token
                    if (fileLine.charAt(i) != ' ' && fileLine.charAt(i) != '\n')
                    {
                        if (!readingToken) readingToken = true;
                        token += fileLine.charAt(i); // Concatenates the char to token   
                    }

                    // If end of line then store token
                    if (i == fileLine.length() - 1 && readingToken)
                    {
                        tokens.add(token);
                        token = "";
                        readingToken = false;
                    }

                    // Ignore any other lines if token size is above 4.
                    if(tokens.size() > 4)
                    {
                        break;
                    }

                }

                valid = true;

                if (tokens.size() != 0)
                {
                    // Are we checking for global parameters and are the tokens valid? If so...
                    if (stageOfConfig == 0 && tokens.size() == 4)
                    {
                        int tokenLen = tokens.size();
                        ArrayList<Integer> globalParams = new ArrayList<>(4);

                        while (tokenLen != 0)
                        {
                            tokenLen--;
                            // Are the characters in the tokens digits?
                            for (int j = 0; j < tokens.get(tokenLen).length(); j++)
                            {
                                if (Character.isDigit(tokens.get(tokenLen).charAt(j)))
                                {
                                    valid = false;
                                    break;
                                }
                            }

                            if (valid)
                            {
                                globalParams.add(tokenLen,Integer.parseInt(tokens.get(tokenLen)));
                            }
                            else
                            {
                                globalParams.clear();
                                break;
                            }
                        }
                        if(valid) stageOfConfig = 1;

                        // Set global parameters
                        NodeInfo.setGlobalParameters(
                            globalParams.get(0),
                            globalParams.get(1),
                            globalParams.get(2),
                            globalParams.get(3)
                        );
                    } //end of global parameters

                    else if (stageOfConfig == 1 && tokens.size() == 3)
                    {
                        int tokenLen = tokens.size();
                        for (int tokenI = 0; tokenI < tokenLen; tokenI++)
                        {
                            //Check if the second token and if second token follows format of "dcXX"
                            if (tokenI == 1 && tokens.get(1).length() == 4 && tokens.get(1).substring(0, 2).compareTo("dc") == 0)
                            {
                                if(!Character.isDigit(tokens.get(tokenI).charAt(2)))
                                {
                                    valid = false;
                                }
                                if (!Character.isDigit(tokens.get(tokenI).charAt(3)))
                                {
                                    valid = false;
                                }
                            }
                            else
                            {
                                // Are all the chars in the token an int? If not then invalid token
                                for (int j = 0; j < token.length(); j++)
                                {
                                    if (!Character.isDigit(tokens.get(tokenI).charAt(j)))
                                    {
                                        valid = false;
                                        break;
                                    }
                                }
                            }
                            if (valid)
                            {
                                ipPort.add(tokens.get(tokenI));
                            }
                            else
                            {
                                ipPort.clear();
                                break;
                            }

                        } 
                        // if valid 3 tokens for node# ip port
                        if (valid && ipPort.size() != 0)
                        {
                            node_ips.add(ipPort.get(1) + ".utdallas.edu");
                            node_ports.add(Integer.parseInt(ipPort.get(2)));
                            if (nodeIDCheckFlag && ipPort.get(1).charAt(2) == hostName.charAt(2) && ipPort.get(1).charAt(3) == hostName.charAt(3))
                            {
                                node = node_ips.size() - 1;
                                nodeIDCheckFlag = false;
                            }
                            ipPort.clear();
                        }
                    } // End of Checking IP Addresses

                } // end of tokens.size != 0

                tokens.clear();
                token = "";
                valid = true;

            }// end of file reading

            if (stageOfConfig == 0)
            {
                System.out.println("ERROR: Global Parameters not properly formatted.");
                throw new Exception();
            }

            if (NodeInfo.num_nodes != node_ips.size())
            {
                System.out.println("ERROR: Not enough ips given as a number of nodes or not properly formatted.");
                throw new Exception();
            }


            // Test
            System.out.println(NodeInfo.num_nodes);
            System.out.println(NodeInfo.interRequestDelay);
            System.out.println(NodeInfo.csExeTime);
            System.out.println(NodeInfo.maxRequests);

            for (int i = 0; i < node_ips.size();i++)
            {
                System.out.println(node_ips.get(i));
            }
            for (int i = 0; i < node_ports.size(); i++)
            {
                System.out.println(node_ports.get(i));
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
            



    }

    public List<NodeInfo> get_node_info()
    {
        return node_info;
    }

    public static void main(String[] args) {
        Parser parse = new Parser();

        parse.parseFile("config.txt");
    }
    
}
