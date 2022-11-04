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


        if (InetAddress.getLocalHost().getHostName().compareTo("dc01.utdallas.edu") == 0)
        {
            request.set(true);  
        }

        net1.start(); // Start CL Protocol;
        System.out.println("Network Thread started...");

        while(!ready.get())
        {
            System.out.println("CS Is Ready!");
            ready.set(false);
            release.set(true);
        }

        request.set(true);
        while(!ready.get())
        {
            System.out.println("CS Is Ready!");
            ready.set(false);
            release.set(true);
        }

    }
}
