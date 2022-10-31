import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


/** ServerClass that is a thread that always is listening to incoming client connection requests.
 *  
 */
public class ServerClass extends Thread 
{
    private ServerSocket serverSocket;
    private List<Socket> socketList;
    private List<PrintWriter> outList;
    private int port;


    /**
     * Constructor that initializes the server socket and opens up to listen for incoming connections
     * Also initializes shared thread-safe lists such as SocketList and outList 
     * Passes on these sockets to the main Network class
     * 
     * @param port Port that the Server is listening on
     * @param socketList List of connected sockets
     * @param outList List of connected out streams that can be written to
     * @throws IOException
     */
    public ServerClass(int port, List<Socket> socketList, List<PrintWriter> outList) throws IOException
    {
        serverSocket = new ServerSocket(port); // Creates a new server socket
        this.socketList = socketList;
        this.port = port;
        this.outList = outList;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                Socket server = serverSocket.accept();

                synchronized(this.socketList){
                    socketList.add(server);
                }
                synchronized(this.outList)
                {
                    PrintWriter out = new PrintWriter(server.getOutputStream(),true);
                    outList.add(out);
                }

                // Try-with-resources block that opens up a listening thread that will receive and interpret incoming messages from this client node
                try 
                {
                    BufferedReader in = new BufferedReader(
                        new InputStreamReader(server.getInputStream()));
                    Thread listeningThread = new ListeningThread(in);
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
        // Tests ServerClass Functionality
        System.out.println("Server Class has now started...");
        List<Socket> sockList = new ArrayList<Socket>();
        int port = Integer.parseInt(args[0]);
        List<PrintWriter> outList = new ArrayList<PrintWriter>();

        Thread t = new ServerClass(port, sockList, outList);
        t.start(); 
        
    }


}