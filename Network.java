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
    private List<String> received_msgs = Collections.synchronizedList(new ArrayList<String>());
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
            System.out.println("[CONFIG]: This machines host name is :" + hostName);
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
            System.out.println("[CONFIG]: This machine's node id is :" + my_node_id);
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
        params = new SharedParameters(my_node_id, listenPort, socketMap, writeMap, last_time_stamp, priority_queue,node_info, received_msgs); 

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

        System.out.println("[SOCKET]: Waiting for other nodes...");

         while (socketMap.size() != max_nodes - 1)
         {
            int currSize = socketMap.size();
            if (currSize != socketMap.size())
            {
                currSize = socketMap.size();
                System.out.println("[SOCKET:]Current connections are: " + socketMap.size() + '/' + (max_nodes - 1));
            }
         }

         System.out.println("[SOCKET]: All connections are secured");

    }

    /**
     * Thread run() function that executes the CL protocol.
     */
    @Override    
    public void run()
    {
        while(true)
        {
            // Process any messages received

            while (!received_msgs.isEmpty())
            {
                process_msgs(received_msgs.get(0)); // Processes the messages
                received_msgs.remove(0); // Dequeues the messages
            }

            // If CS_enter() is called, push request onto priority queue and send request message to all other nodes
            if (application_request.get())
            {
                increment_time_stamp();
                my_request = new Request(last_time_stamp.get(my_node_id), my_node_id); // Creates a my request

                priority_queue.add(my_request);
                System.out.print("Request From " + my_request.getNode_id() + " Pushed onto Queue: ");
                my_request.printRequest();

                for (int i = 0; i < max_nodes; i++)
                {
                    if (i == my_node_id) continue; // Skip my own node
                    
                    increment_time_stamp();
                    String send_msg = "request " + last_time_stamp.get(my_node_id) + " " + my_node_id;
                    writeMap.get(i).println(send_msg); // Sends request message to node i

                }

                application_request.set(false);
                
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
                int check_nodes = 0;
                for (int i = 0; i < last_time_stamp.size(); i++)
                {

                    if (i == my_node_id) continue;
                    else if (my_request.getTime_stamp() < last_time_stamp.get(i))
                    {
                        check_nodes++;
                    }
                    else if (my_request.getTime_stamp() == last_time_stamp.get(i))
                    {
                        if(my_request.getNode_id() < i)
                        {
                            check_nodes++;
                        }

                    }

                }

                if (check_nodes == max_nodes - 1)
                {
                    Request pop = priority_queue.poll();
                    System.out.print("[PRIO_Q]: Request popped off prioQ: ");
                    pop.printRequest();
                    cs_ready.set(true);
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


    public void process_msgs(String inputLine)
    {
        Request request = new Request(); 
        int type_of_message = handle_message(inputLine, request);

        // Update my nodes time stamp on largest time stamp and adds one
        
        last_time_stamp.set(my_node_id, Integer.max(request.getTime_stamp(), last_time_stamp.get(my_node_id)) + 1);
        last_time_stamp.set(request.getNode_id(), request.getTime_stamp()); // Update client nodes time stamp
        show_time_stamps(); // FIXME: Debugging Function

        if (type_of_message == 1)// For Request
        {
            priority_queue.add(request); // Pushes request onto priority queue
            System.out.print("[PRIO_Q]: Request From " + request.getNode_id() + " Pushed onto Queue: ");
            request.printRequest();

            // Sends reply message to node it came from
            // Also increments and updates last time stamp
            last_time_stamp.set(my_node_id, request.getTime_stamp() + 1);
            String send_msg = "reply " + last_time_stamp.get(my_node_id) + " " + my_node_id;
            show_time_stamps(); // FIXME: Debugging Comments
            writeMap.get(request.getNode_id()).println(send_msg);

        }
        else if (type_of_message == 2) // For Release
        {
            Request pop = priority_queue.poll(); // Pops the head request off of the prio queue
            System.out.print("[PRIO_Q]: Request popped off prioQ: ");
            pop.printRequest();
        }
        // A reply message does nothing but log the last time stamp of the message (alreadyd one above)
        else if (type_of_message == 3)
        {
        }
        else
        {
            System.out.println("[ERROR]: Incorrect Message Received from " + request.getNode_id());
        }
    }
    /**
     * 
     * @param message
     * @param request
     * @return Returns based on type of message or error:
     * 1 Request
     * 2 Release
     * 3 Reply
     * -1 for improperly formatted message
     */
    public int handle_message(String message, Request request)
    {
        String string_array[] = message.split(" ");

        // Checks if message is correct amount of tokens
        if (!(string_array.length == 3))
        {
            return -1;
        }

        String type_of_request = string_array[0];

        int time_stamp = Integer.parseInt(string_array[1]);
        if (time_stamp < 0) // Checks for incorrect time stamp
            return -1;
        int node_id = Integer.parseInt(string_array[2]);
        if (node_id > params.last_time_stamp.size() - 1)
            return -1;
        request.setTime_stamp(time_stamp);
        request.setNode_id(node_id);

        if (type_of_request.compareTo("request") == 0)
        {
            return 1;
        }
        else if (type_of_request.compareTo("release") == 0)
        {
            return 2;
        }
        else if (type_of_request.compareTo("reply") == 0)
        {
            return 3;
        }
        else // Wrong formatted message
        {
            return -1;
        }
    }

    public void increment_time_stamp()
    {
        last_time_stamp.set(my_node_id, last_time_stamp.get(my_node_id) + 1); // Increments my time stamp
    }

    /**
     * Shows current time stamp vector
     */
    public void show_time_stamps()
    {
        System.out.print("Current Time Stamps: <");
        for (int i = 0; i < last_time_stamp.size(); i++)
        {
            if (i != last_time_stamp.size() - 1)
                System.out.print(last_time_stamp.get(i) + ", ");
            else
                System.out.println(last_time_stamp.get(i) + ">");
        }
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
