import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class ListeningThread extends Thread
{
    private BufferedReader inSocket;
    private SharedParameters params;
    private List<String> received_msgs;
    private int client_id;
    private AtomicBoolean endThread;
    
    /** Constructor to pass in the listening socket and priority queue 
     * 
     * @param inSocket
     */
    public ListeningThread (BufferedReader inSocket, int client_id, SharedParameters params)
    {
        this.inSocket = inSocket;
        this.received_msgs = params.received_msgs;
        this.client_id = client_id;
        this.endThread = params.endThread;
        // System.out.println("Listening to " + client_id);
    }

    @Override
    /** Thread function that will constant listen to messages on the socket and interpret the type of message
     *  Will be in the format of "<type of message> <time_stamp> <node_id>""
     *  Will be 3 types of messages
     *  1. Request
     *  2. Release
     *  3. Reply
     */
    public void run()
    {
        try  
        {
            String inputLine;
            while ((inputLine = inSocket.readLine()) != null && !endThread.get())
            {
                received_msgs.add(inputLine);
                System.out.println("[From Node " + client_id + "]: " +  inputLine);
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        
    }   

    

}




