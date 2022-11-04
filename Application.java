import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.*;
import java.net.InetAddress;
import java.util.Random;
import java.time.*;


public class Application {

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

        net1.start(); // Start CL Protocol;
        System.out.println("Network Thread started...");
        System.out.println("-------------------------");

        // Currently testing 
        double interRequestDelay = 20.0 / 1000.0;
        double csExeTime = 10.0 / 1000.0;
        int maxRequests = 5;

        double csExeTimeRand = 0.0;
        double interRequestDelayRand = 0.0;
        Random rand = new Random();
        int currRequest = 1;
        Instant start;
        Instant end;
        Duration timeElapsed;

        while (maxRequests > 0)
        {
            csExeTimeRand = csExeTime + (rand.nextDouble() % 50) / 1000.0;
            interRequestDelayRand = interRequestDelay + (rand.nextDouble() % 50) / 1000;

            System.out.println("[APPLICATION]: Trying Request " + currRequest);
            maxRequests -= 1;

            request.set(true);

            while (!ready.get())
            {
            }

            ready.set(false); // Ready no longer true

            start =  Instant.now();
            end = Instant.now();
            timeElapsed = Duration.between(start, end);
            while ((timeElapsed.toMillis() / 1000.0) < csExeTimeRand)
            {
                end = Instant.now();
                timeElapsed = Duration.between(start, end);
            }

            System.out.println("[APPLICATION]: Time Executing is " +
                                timeElapsed.toMillis() + "ms");
            System.out.println("[APPLICATION]: Time Should Have Executed: " + csExeTimeRand + "ms");
            System.out.println("[APPLICATION]: Done Executing ----------------------");


            // Waiting interRequestDelay before sending next request.
            release.set(true);

            start =  Instant.now();
            end = Instant.now();
            timeElapsed = Duration.between(start, end);
            while ((timeElapsed.toMillis() / 1000.0) < interRequestDelayRand)
            {
                end = Instant.now();
                timeElapsed = Duration.between(start, end);
            }

            System.out.println("[APPLICATION]: Time Waited " + timeElapsed.toMillis() + "ms" );
            System.out.println("[APPLICATION]: Time Should Have Waited " + timeElapsed.toMillis() + "ms");

        }

        System.out.println("[APPLICATION]: All Requests satisfied ");
        System.out.println("---------------------------------");


    }
}


