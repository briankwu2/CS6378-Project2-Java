import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/** ServerClass that is a thread that always is listening to incoming client connection requests.
 *  
 */
public class ServerClass extends Thread 
{
    private ServerSocket serverSocket;
    private Map<Integer,Socket> socketMap;
    private Map<Integer, PrintWriter> writeMap;
    private PriorityBlockingQueue<Request> priority_queue;
    private int port;
    SharedParameters params;


    /**
     * Constructor that initializes the server socket and opens up to listen for incoming connections
     * Also initializes shared thread-safe lists such as SocketList and writeMap 
     * Passes on these sockets to the main Network class
     * 
     * @param port Port that the Server is listening on
     * @param socketMap List of connected sockets
     * @param writeMap List of connected out streams that can be written to
     * @throws IOException
     */
    public ServerClass(SharedParameters params) throws IOException
    {
        serverSocket = new ServerSocket(port); // Creates a new server socket
        this.params = params;
        this.socketMap = params.socketMap;
        this.port = params.listenPort;
        this.writeMap = params.writeMap;
        this.priority_queue = params.priority_queue;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Socket server = serverSocket.accept();
                String clientName = server.getInetAddress().getHostName(); 
                int client_id = params.findNodeID(clientName);
                if (client_id == -1)
                {
                    System.out.println("Incoming client " + clientName + " not found in node info. Ignoring connection.");
                    continue;
                }

                synchronized(socketMap){
                    socketMap.put(client_id, server);
                }
                synchronized(this.writeMap)
                {
                    PrintWriter out = new PrintWriter(server.getOutputStream(),true);
                    writeMap.put(client_id, out);
                }

                // Try-with-resources block that opens up a listening thread that will receive and interpret incoming messages from this client node
                try 
                {

                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(server.getInputStream()));
                    Thread listeningThread = new ListeningThread(in, client_id,params);
                    listeningThread.start(); // Starts a new listening thread that will append to the output file
                    System.out.println("New listening thread created. Listening to " + server.getRemoteSocketAddress());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                System.out.println("Socket connected to " + server.getRemoteSocketAddress());
            }
            catch (IOException e)
            {
                System.err.println("Could not listen on port " + port);
            }
            
        }
    }


    public static void main(String[] args) throws IOException {
        
    }


}