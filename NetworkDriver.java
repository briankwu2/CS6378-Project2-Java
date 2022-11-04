import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.*;
import java.net.InetAddress;

public class NetworkDriver {

    public static void main(String[] args) throws Exception {
        
        Parser parser = new Parser();
        parser.parseFile("config.txt");

        Map<String, AtomicBoolean> atomicFlags = new ConcurrentHashMap<String, AtomicBoolean>();
        AtomicBoolean a1 = new AtomicBoolean();
        AtomicBoolean a2 = new AtomicBoolean();
        AtomicBoolean a3 = new AtomicBoolean();

        a1.set(false);
        a2.set(false);
        a3.set(false);

        atomicFlags.put("request", a1);
        atomicFlags.put("ready", a2);
        atomicFlags.put("release", a3);

        Network net1 = new Network(parser.get_node_info(), atomicFlags);
        System.out.println("Done!");

        if (InetAddress.getLocalHost().getHostName() == "dc01.utdallas.edu")
        {
            a1.set(true); // 
        }

        net1.start(); // Start CL Protocol;


        while(true)
        {
            if (a2.get())
                System.out.println("CS Is Ready!");
            a2.set(false);
        }

    }
}
