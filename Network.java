import java.util.*; // For lists
import java.net.*;
import java.io.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class Network {
     /* 
     * 1. Connect to all respective nodes (client threads will connect to neighbors)
     *      0 -> 1,2,3,4
     *      1 -> 2,3,4
     *      2 -> 3, 4
     *      3 -> 4
     * 
     */

    private int my_node_id;
    private String hostName;
    private int listenPort;
    private int max_nodes;
    SharedParameters params;

    // I/O Structures
    private List<Socket> socketList = Collections.synchronizedList(new ArrayList<Socket>()); // Creates a thread-safe Socket List
    private List<PrintWriter> outList = Collections.synchronizedList(new ArrayList<PrintWriter>()); // Creates a thread-safe output channel list
    private List<Integer> last_time_stamp = Collections.synchronizedList(new ArrayList<Integer>()); // Creates a thread-safe time stamp array
    private List<NodeInfo> node_info = Collections.synchronizedList(new ArrayList<NodeInfo>()); // ArrayList containing all the info
    private PriorityBlockingQueue<Request> priority_queue;

    /* Public Constructor that assigns the node number, hostname, and listening port.
     * It then creates a server thread that will listen to any client connections
     */
    public Network(int my_node_id, List<NodeInfo> node_info) 
    {
        
        this.my_node_id = my_node_id;
        this.node_info = node_info;
        this.hostName = node_info.get(my_node_id).hostName;
        this.listenPort = node_info.get(my_node_id).listenPort;
        priority_queue = new PriorityBlockingQueue<Request>();

        // Create the last_time_stamp array list and fill it with -1s.
        for (int i = 0; i < max_nodes; i++)
        {
            last_time_stamp.add(-1);
        }

        // Creates a shared parameters class to share with threads
        params = new SharedParameters(my_node_id, listenPort, socketList, outList, last_time_stamp, priority_queue); 


        createServerClass(); // Creates a server thread that listens for connecting nodes and returns a socket to the connecting node

        // Create hook to cleanup everything if terminated or Ctrl-C
        Runtime.getRuntime().addShutdownHook(new Thread() 
        {
            public void run() {
                System.out.println("Exiting All Threads... Cleaning up...");
                cleanUpFunction();
                System.out.println("Done! Bye!");
                }
        });


        // FIXME: Put Socket Establishment Code HERE
    }

    /** Closes all sockets and resources used.
     * 
     */
    public void cleanUpFunction()
    {

        try 
        {

            // Close all the sockets if not already
            for (int i = 0; i < socketList.size(); i++)
            {
                socketList.get(1).close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /** Configures the node to the global parameters read from the configFile
     * FIXME: Move over to the Application Module
     * @param configFile Text file that contains all the nodes and global parameters
     */
    public void configNode(String configFile)
    {
        try(
            BufferedReader inFile = new BufferedReader(new FileReader(configFile));
        )
        {
            String config = inFile.readLine();
            while (config.charAt(0) == '#')
            {
                config = inFile.readLine();
            }
            
            String configParameters[] = config.split(" ");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Creates a server thread that listens and establishes connections
    public void createServerClass()
    {
        try
        {
            Thread listeningServer = new ServerClass(params);
            System.out.println("Server is now listening on port " + listenPort);
            listeningServer.start(); // Starts listening for new client connections
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    // Requests to connect with another node's server thread and establish a connection
    /* TO-DO:
     * - Make a method to find Client_ID (their node id) via the config file
     * - Pass it in to the listening thread
     * 
     */
    public Socket requestConnection(String remoteHost, int remotePort)
    {
        System.out.println("Attempting to connect to " + remoteHost + " on port number " + remotePort + "...");
        
        while(true)
        {
            try
            {
                Socket connectSocket = new Socket(remoteHost, remotePort); // Attempts to create a connection with host/port
                if (connectSocket != null)
                {
                    synchronized(socketList)
                    {
                        socketList.add(connectSocket); // Adds the socket to the socketlist
                    }
                    synchronized(outList)
                    {
                        PrintWriter out = new PrintWriter(connectSocket.getOutputStream(),true);
                        outList.add(out);
                    }
                    try
                    {
                        BufferedReader in = new BufferedReader(
                            new InputStreamReader(connectSocket.getInputStream()));
                        Thread listeningThread = new ListeningThread(in, params);
                        listeningThread.start(); // Starts a new listening thread that will append to the output file
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    System.out.println("Connected to " + remoteHost + " on " + remotePort);
                    return connectSocket;

                }
            }
            catch (Exception e)
            {   
                System.out.println("Unable to connect to the port... retrying...");
            }
        }
    }

    // Thread that will always listen to the socket connections from other nodes
    public void createReceivingThread(BufferedReader inSocket)
    {
        // Create a thread that listens to the specified socket
        // Synchronized Writes to a file
        try
        {
            Thread t = new ListeningThread(inSocket, params);
            t.start(); // Starts the new listening thread that writes to the output file
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Send a random amount of messages between minPerActive and maxPerActive to a random node within the connection list.
    public void sendMessages(String message, int toNodeNum)
    {
        try
        {
            // outList.get(toNodeNum).println(message + ' ' + time_stamp + ' ' my_node_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public int findNodeID(String hostName)
    {
        


    }

}
