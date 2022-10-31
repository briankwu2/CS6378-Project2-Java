import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
public class testPrioQ {

    public static void main(String[] args) {
        PriorityBlockingQueue<Request> pbq
            = new PriorityBlockingQueue<>();

        Random random = new Random();

        for (int i = 0; i < 100; i++)
        {
            pbq.add(new Request(random.nextInt(100), random.nextInt(10)));
        }
        
        // Checks for same time stamp comparisons
        pbq.add(new Request(100, 5));
        pbq.add(new Request(100, 100));
        pbq.add(new Request(100, 12010));

       
        while (!pbq.isEmpty())
        {
            pbq.poll().printRequest();
        }

    }
}
