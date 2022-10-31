import java.util.concurrent.PriorityBlockingQueue;

// Class to hold data structures that can be passed to other methods
import java.util.*; // For lists
import java.net.*;
import java.io.*;

public class SharedParameters {


    public SharedParameters(
        int my_node_id,
        int listenPort,
        List<Socket> socketList,
        List<PrintWriter> outList,
        List<Integer> last_time_stamp,
        PriorityBlockingQueue<Request> priority_queue)
    {
        this.my_node_id = my_node_id;
        this.listenPort = listenPort;
        this.socketList = socketList;
        this.outList = outList;
        this.last_time_stamp = last_time_stamp;
        this.priority_queue = priority_queue;
    }

    // Variables
    public int my_node_id;
    public int listenPort;
    public List<Socket> socketList;
    public List<PrintWriter> outList;
    public List<Integer> last_time_stamp;
    public PriorityBlockingQueue<Request> priority_queue;
    
}
