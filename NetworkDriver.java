import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.*;
public class NetworkDriver {

    public static void main(String[] args) {
        
        Parser parser = new Parser();
        parser.parseFile("config.txt");

        Map<String, AtomicBoolean> atomicFlags = new ConcurrentHashMap<String, AtomicBoolean>();
        AtomicBoolean a1 = new AtomicBoolean();
        AtomicBoolean a2 = new AtomicBoolean();
        AtomicBoolean a3 = new AtomicBoolean();

        a1.set(false);
        a2.set(false);
        a3.set(false);

        Network net1 = new Network(parser.get_node_info(), atomicFlags);
        System.out.println("Done!");
    }
}
