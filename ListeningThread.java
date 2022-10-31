import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ListeningThread extends Thread
{
    private BufferedReader inSocket;
    private PrintWriter outFile;

    //FIXME: Insert PrioQ

    /** Constructor to pass in the listening socket
     * 
     * @param inSocket
     */
    public ListeningThread (BufferedReader inSocket)
    {
        this.inSocket = inSocket;
    }

    @Override
    /** Thread function that will constant listen to messages on the socket and interpret the type of message
     *  Will be in the format of "<type of message> <time_stamp> <node_id>""
     *  Will be 3 types of messages
     *  1. Request
     *  2. Release
     *  3. Reply
     *  
     *  
     * 
     */
    public void run()
    {
        try  
        {
            String inputLine;
            while ((inputLine = inSocket.readLine()) != null)
            {

                // Synchronized to ensure that no other threads overwrite messages
                synchronized(outFile)
                {
                    outFile.println(inputLine);
                }
                

            }
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }   
}




