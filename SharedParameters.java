import java.util.concurrent.PriorityBlockingQueue;
// Class to hold data structures that can be passed to other methods
import java.util.*; // For lists
import java.net.*;
import java.io.*;

public class SharedParameters {


    public SharedParameters(
        int my_node_id,
        int listenPort,
        Map<Integer, Socket> socketMap,
        Map<Integer, PrintWriter> writeMap,
        List<Integer> last_time_stamp,
        PriorityBlockingQueue<Request> priority_queue,
        List<NodeInfo> node_info,
        List<String> received_msgs)
    {
        this.my_node_id = my_node_id;
        this.listenPort = listenPort;
        this.socketMap = socketMap;
        this.writeMap = writeMap;
        this.last_time_stamp = last_time_stamp;
        this.priority_queue = priority_queue;
        this.node_info = node_info;
        this.received_msgs = received_msgs;
    }

    // Variables
    public int my_node_id;
    public int listenPort;
    public Map<Integer, Socket> socketMap;
    public Map<Integer, PrintWriter> writeMap;
    public List<Integer> last_time_stamp;
    public PriorityBlockingQueue<Request> priority_queue;
    public List<NodeInfo> node_info;
    public List<String> received_msgs;

    /**
     * Finds the node id of a given host name.
     * 
     * @param hostName The host name to be compared. In format of "dcXX.utdallas.edu"
     * @return Returns the node_id of the host name if found.
     *         Otherwise, return -1 if not found.
     */
    public int findNodeID(String hostName)
    {
        for (int i = 0; i < node_info.size(); i++)
        {
            if(node_info.get(i).hostNameMatch(hostName))
            {
                return node_info.get(i).node_id;
            }
        }

        return -1; // If hostname not found within node info

    }

}
