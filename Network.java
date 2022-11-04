import java.util.*; // For lists
import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Network extends Thread {
     /* 
     * 1. Connect to all respective nodes (client threads will connect to neighbors)
     *      0 -> 1,2,3,4
     *      1 -> 2,3,4
     *      2 -> 3, 4
     *      3 -> 4
     * 
     */
    // Node Information
    private int my_node_id;
    private String hostName;
    private int listenPort;
    private int max_nodes;
    private Request my_request;

    // Shared Parameters Class to pass in to threads
    SharedParameters params;

    // Atomic Boolean Flags 
    Map<String, AtomicBoolean> flagMap; // Should be a ConcurrentHashMap class made in Application Class
    AtomicBoolean application_request;
    AtomicBoolean cs_ready;
    AtomicBoolean release_flag;

    // I/O Structures
    private Map<Integer, Socket> socketMap = new ConcurrentHashMap<>();// Creates a thread-safe Socket List
    private Map<Integer, PrintWriter> writeMap = new ConcurrentHashMap<>(); // Creates a thread-safe output channel list
    private List<Integer> last_time_stamp = Collections.synchronizedList(new ArrayList<Integer>()); // Creates a thread-safe time stamp array
    private List<NodeInfo> node_info; // 
    private PriorityBlockingQueue<Request> priority_queue;

    /* Public Constructor that assigns the node number, hostname, and listening port.
     * It then creates a server thread that will listen to any client connections
     */
    public Network(List<NodeInfo> node_info, Map<String, AtomicBoolean> flagMap) 
    {

        // Find which node this machine is 
        try
        {
            hostName = InetAddress.getLocalHost().getHostName();
            System.out.println("This machines host name is :" + hostName);
            for (int i = 0; i < node_info.size(); i++)
            {
                if(node_info.get(i).hostNameMatch(hostName))
                {
                    my_node_id = node_info.get(i).node_id;
                    break;
                }
            }
            if(my_node_id == -1) // Running on the wrong machine
            {
                throw new Exception();
            }
            System.out.println("This machine's node id is :" + my_node_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }     
        

        // Gets the AtomicBooleans from passed in map.
        this.flagMap = flagMap;
        application_request = flagMap.get("request");
        cs_ready = flagMap.get("ready");
        release_flag = flagMap.get("release");


        // Instantiate node information
        this.node_info = node_info;
        this.hostName = node_info.get(my_node_id).hostName;
        this.listenPort = node_info.get(my_node_id).listenPort;
        this.max_nodes = NodeInfo.num_nodes;
        my_request = new Request();
        priority_queue = new PriorityBlockingQueue<Request>();

        // Create the last_time_stamp array list and fill it with -1s.
        for (int i = 0; i < max_nodes; i++)
        {
            last_time_stamp.add(-1);
        }

        // Creates a shared parameters class to share with threads
        params = new SharedParameters(my_node_id, listenPort, socketMap, writeMap, last_time_stamp, priority_queue,node_info); 

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



        // Socket Establishment

        /* 
        Create a strongly created graph that connects to all the other nodes.
        Node i will connect to all nodes above it
        It will listen to all nodes below it.
        E.g.
        Consider 4 nodes
        0 -> 1,2,3
        1 -> 2, 3
        2 -> 3
        3 ->
         */

         for (int i = my_node_id + 1; i < node_info.size();i++)
         {
            String hostConnect = node_info.get(i).hostName;
            int hostPort= node_info.get(i).listenPort;
            try
            {
                requestConnection(hostConnect, hostPort); // Attempts to connect to the host
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
         }

        System.out.println("Waiting for other nodes...");

         while (socketMap.size() != max_nodes - 1)
         {
            int currSize = socketMap.size();
            if (currSize != socketMap.size())
            {
                currSize = socketMap.size();
                System.out.println("Current connections are: " + socketMap.size() + '/' + (max_nodes - 1));
            }
         }

         System.out.println("All connections are secured");

    }

    /**
     * Thread run() function that executes the CL protocol.
     */
    @Override    
    public void run()
    {
        while(true)
        {
            // If CS_enter() is called, push request onto priority queue and send request message to all other nodes
            if (application_request.get())
            {
                System.out.println("Pushing my request onto the priority queue");
                increment_time_stamp();
                my_request = new Request(last_time_stamp.get(my_node_id), my_node_id); // Creates a my request

                priority_queue.add(my_request);
                for (int i = 0; i < max_nodes; i++)
                {
                    if (i == my_node_id) continue; // Skip my own node
                    
                    increment_time_stamp();
                    String send_msg = "request " + last_time_stamp.get(my_node_id) + " " + my_node_id;
                    writeMap.get(i).println(send_msg); // Sends request message to node i

                }

                application_request.set(false);
                System.out.println("Application Request is " + application_request.get());
                
            }

            // Check two conditions to enter critical section

            // First Condtition
            // Check if my_request is in the priority queue and not in critical section
            if (
                !cs_ready.get() &&
                !priority_queue.isEmpty() && 
                my_request.compareTo(priority_queue.peek()) == 0)
            {
                // Second Condition
                // FIXME: Double check this condition/protocol
                // Check if my request's time stamp is lower than all of the other node's latest time stamp messages
                for (int i = 0; i < last_time_stamp.size(); i++)
                {
                    if (i == my_node_id) continue;

                    else if (my_request.getTime_stamp() < last_time_stamp.get(i))
                    {
                        priority_queue.poll(); // Pops off my request
                        cs_ready.set(true);
                    }
                    else if (my_request.getTime_stamp() == last_time_stamp.get(i))
                    {
                        if(my_request.getNode_id() < i)
                        {
                            cs_ready.set(true);
                            priority_queue.poll(); // Pop off my request
                        }

                    }

                }

            }

            // Once critical section is finished, release request and broadcast release message
            if (release_flag.get())
            {
                // To send a release message to all other nodes
                for (int i = 0; i < max_nodes; i++)
                {
                    // Ignore sending anything to my own node
                    if(i == my_node_id) continue;

                    // Send request messages to all other nodes
                    increment_time_stamp();
                    String send_msg = "release " + last_time_stamp.get(my_node_id) + " " + my_node_id;
                    writeMap.get(i).println(send_msg);
                }
                release_flag.set(false); 
            }
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
    public Socket requestConnection(String remoteHost, int remotePort) throws Exception
    {
        System.out.println("Attempting to connect to " + remoteHost + " on port number " + remotePort + "...");

        // Finds out which node id are we trying to connec to
        int client_id = params.findNodeID(remoteHost);
        if (client_id == -1)
        {
            System.out.println("ERROR: Client id not found in requestConnection");
        }

        // Loop to attempt to connect to node 
        while(true)
        {
            try
            {
                Socket connectSocket = new Socket(remoteHost, remotePort); // Attempts to create a connection with host/port
                if (connectSocket != null)
                {
                    synchronized(socketMap)
                    {
                        socketMap.put(client_id, connectSocket); // Adds the socket to the socketlist
                    }
                    synchronized(writeMap)
                    {
                        PrintWriter out = new PrintWriter(connectSocket.getOutputStream(),true);
                        writeMap.put(client_id, out);
                    }

                    try
                    {
                        BufferedReader in = new BufferedReader(
                            new InputStreamReader(connectSocket.getInputStream()));
                        Thread listeningThread = new ListeningThread(in,client_id, params);
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
                Thread.sleep(5000);
            }
        }
    }



    public void increment_time_stamp()
    {
        last_time_stamp.set(my_node_id, last_time_stamp.get(my_node_id) + 1); // Increments my time stamp
    }
    /** 
     * Closes all sockets and resources used.
     */
    public void cleanUpFunction()
    {
        try 
        {
            // Close all the sockets if not already
            for (int i = 0; i < socketMap.size(); i++)
            {
                socketMap.get(1).close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    
}
