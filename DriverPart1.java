import java.io.*;

public class DriverPart1 {
    public static void main(String[] args) throws Exception{
        // The format is java testing <listenPort> <nodeNum> 
        int listenPort = Integer.parseInt(args[0]);
        int nodeNum = Integer.parseInt(args[1]);
        Network node = new Network(nodeNum, "localhost",listenPort, "config.txt");
        // Test receiving thread and send messages method
        // Create two nodes
        String localHost = "localhost";
    
    }

}
