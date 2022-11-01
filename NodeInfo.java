public class NodeInfo {

    // Global Parameters
    static public int num_nodes;
    static public int interRequestDelay;
    static public double csExeTime;
    static public int maxRequests;

    // Variables
    public int node_id;
    public String hostName;
    public int listenPort;


    public NodeInfo(int node_id, String hostName, int listenPort)
    {
        this.node_id = node_id;
        this.hostName = hostName;
        this.listenPort = listenPort;
    }

    public static void setGlobalParameters(int num_nodes, int interRequestDelay, double csExeTime, int maxRequests)
    {
        NodeInfo.num_nodes = num_nodes;
        NodeInfo.interRequestDelay = interRequestDelay;
        NodeInfo.csExeTime = csExeTime;
        NodeInfo.maxRequests = maxRequests;

    }

    /**
     * @param hostName String of host name to compare against
     * @return Returns true if the given host name is the same as the node's host name
     */
    public boolean hostNameMatch(String hostName)
    {
        return this.hostName.compareTo(hostName) == 0;

    }

}
