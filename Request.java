public class Request implements Comparable<Request>
{
    private int time_stamp;
    private int node_id;

    // Default Constructor
    public Request()
    {
        time_stamp = -1;
        node_id = -1;
    }

    public Request(int time_stamp, int node_id)
    {
        this.time_stamp = time_stamp;
        this.node_id = node_id;
    }

    @Override
    /** Overridden compareTo function that prioritizes time stamp and then uses node id as a tie breaker
     * @param compare The Request to compare to
     */
    public int compareTo(Request compare)
    {
        if (this.getTime_stamp() < compare.getTime_stamp())
            return -1;
        else if (this.getTime_stamp() > compare.getTime_stamp())
            return 1;
        else // If time stamps are equal, go to tie breaker
        {
            if (this.getNode_id() < compare.getNode_id())
                return -1;
            else if (this.getNode_id() > compare.getNode_id())
                return 1;
            else
            {
                return 0;
            }
        }
    }

    // Getters
    public int getNode_id() {
        return node_id;
    }

    public int getTime_stamp() {
        return time_stamp;
    }

    // Setters
    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public void setTime_stamp(int time_stamp) {
        this.time_stamp = time_stamp;
    }


    public void printRequest()
    {
        System.out.println("Time Stamp: " + time_stamp + " Node ID: " + node_id);
    }

    // Test the request functions
    public static void main(String[] args) {
        Request r1 = new Request(5,10);
        Request r2 = new Request(3,2);
        Request r3 = new Request(100,10);
        Request r4 = new Request(100, 11);


        System.out.println(r1.compareTo(r2)); // Expect 1
        System.out.println(r1.compareTo(r3)); // Expect -1
        System.out.println(r2.compareTo(r3)); // Expect -1 
        System.out.println(r3.compareTo(r3)); // Expect 0
        System.out.println(r3.compareTo(r4)); // Expect -1
    }

}

