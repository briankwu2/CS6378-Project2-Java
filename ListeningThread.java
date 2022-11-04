import java.io.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class ListeningThread extends Thread
{
    private BufferedReader inSocket;
    private PrintWriter outSocket;
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
        this.outSocket = params.writeMap.get(client_id);
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
                Request request = new Request(); 
                int type_of_message = handle_message(inputLine, request);

                // Update my nodes time stamp on largest time stamp and adds one
                
                last_time_stamp.set(my_node_id, Integer.max(request.getTime_stamp(), last_time_stamp.get(my_node_id)) + 1);
                last_time_stamp.set(client_id, request.getTime_stamp()); // Update client nodes time stamp

                if (type_of_message == 1)// For Request
                {
                    priority_queue.add(request); // Pushes request onto priority queue

                    // Sends reply message to node it came from
                    // Also increments and updates last time stamp
                    last_time_stamp.set(my_node_id, request.getTime_stamp() + 1);
                    String send_msg = "reply " + last_time_stamp.get(my_node_id) + " " + my_node_id;
                    outSocket.println(send_msg);

                }
                else if (type_of_message == 2) // For Release
                {
                    priority_queue.poll(); // Pops the head request off of the prio queue

                }
                // A reply message does nothing but log the last time stamp of the message (alreadyd one above)
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

        if (node_id > params.last_time_stamp.size())
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


    public static void main(String[] args) {
        Request r1 = new Request();

        
    }

}




