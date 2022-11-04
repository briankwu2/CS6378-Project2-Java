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
        AtomicBoolean request = new AtomicBoolean();
        AtomicBoolean ready = new AtomicBoolean();
        AtomicBoolean release = new AtomicBoolean();

        request.set(false);
        ready.set(false);
        release.set(false);

        atomicFlags.put("request", request);
        atomicFlags.put("ready", ready);
        atomicFlags.put("release", release);

        Network net1 = new Network(parser.get_node_info(), atomicFlags);
        System.out.println("Done!");

        boolean node_zero = InetAddress.getLocalHost().getHostName().compareTo("dc01.utdallas.edu") == 0;
        if (node_zero)
        {
            request.set(true);  
        }

        net1.start(); // Start CL Protocol;
        System.out.println("Network Thread started...");

        while(true && node_zero)
        {
            if (ready.get())
            {
                System.out.println("CS Is Ready!");
                ready.set(false);
                release.set(true);
                break;
            }
        }

        request.set(true);

        while(true)
        {
            if (ready.get())
            {
                System.out.println("CS Is Ready!");
                ready.set(false);
                release.set(true);
                break;
            }
        }

    }
}
