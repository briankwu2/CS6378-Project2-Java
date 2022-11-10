# Compilation
In order to compile and run this program, open a terminal and log into a dcXX.utdallas.edu machine for each
of the running nodes.
In order to compile all of the files, run the command:

```bash
javac *.java
```

Then on each of the relevant nodes, run:
```bash
java Application
```

Make sure the config.txt file is configured correctly, as the applications will not fully connect until all of
the nodes listed inside the config.txt file are running their own program.

The program will output 2 output files per node.
"config_#.out"
"metrics_#.csv"

The config file outputs per line, the time stamps at which the node entered its C.S. and time stamp at which it left.
It is used to test for conflicts using the Project2Tester.cpp file.

The metrics file outputs the data obtained on each line for the requests:
csExeTime, interRequestDelayTime, Response Time, System Throughput
All the time variables are in milliseconds.

In order to test for conflicts, run the C++ command:

```bash
g++ -std=c++11 Project2Tester.cpp
```

Then run:
```bash
./a.out config #
```
Where # is the number of nodes configured.

It will output the detected amount of conflicts, and also if there were intentional conflicts.

