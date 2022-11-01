import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.PriorityBlockingQueue;

public class ListeningThread extends Thread
{
    private BufferedReader inSocket;
    private PriorityBlockingQueue<Request> priority_queue;
    private List<Integer> last_time_stamp;
    private int my_node_id;
    private int client_id;
    private SharedParameters params;
    /** Constructor to pass in the listening socket and priority queue 
     * 
     * @param inSocket
     */
    public ListeningThread (BufferedReader inSocket, int client_id, SharedParameters params)
    {
        this.inSocket = inSocket;
        this.client_id = client_id;
        this.params = params;
        this.priority_queue = params.priority_queue;
        this.my_node_id = params.my_node_id;
        this.last_time_stamp = params.last_time_stamp;
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
            while ((inputLine = inSocket.readLine()) != null)
            {
                ArrayList<Integer> msg_info = new ArrayList<>();
                int type_of_message = handle_message(inputLine,msg_info);

                if (type_of_message == 1)// For Request
                {
                    Request req = new Request(msg_info.get(0), msg_info.get(1));
                    priority_queue.add(req); // Pushes request onto priority queue

                    // Sends reply message to node it came from
                    // Also increments and updates last time stamp
                    last_time_stamp.set(my_node_id, req.getTime_stamp() + 1);


                }
                else if (type_of_message == 2) // For Release
                {

                }
                else if (type_of_message == 3)
                {

                }
                

            


            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        
    }   

    /**
     * 
     * @param message
     * @param msg_info
     * @return Returns based on type of message or error:
     * 1 Request
     * 2 Release
     * 3 Reply
     * -1 for improperly formatted message
     */
    public int handle_message(String message, ArrayList<Integer> msg_info)
    {
        String string_array[] = message.split(" ");

        // Checks if message is correct amount of tokens
        if (!(string_array.length == 3))
        {
            return -1;
        }

        String type_of_request = string_array[0];
        int time_stamp = Integer.parseInt(string_array[1]);
        int node_id = Integer.parseInt(string_array[2]);
        msg_info.add(time_stamp);
        msg_info.add(node_id);

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

    public static void main(String[] args) {
        ListeningThread l1 = new ListeningThread(null, null,null);
        ArrayList<Integer> ar1 = new ArrayList<>();

        System.out.println(l1.handle_message("request 5 10", ar1));
        System.out.println(l1.handle_message("release 5 10", ar1));
        System.out.println(l1.handle_message("reply 5 10", ar1));
        System.out.println(l1.handle_message("reply 5", ar1));
        System.out.println(l1.handle_message("erar aionf", ar1));
        
        
        
        
        
    }
}




