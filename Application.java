import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.*;
import java.time.*;
import java.io.*;

public class Application {

    public static void main(String[] args) throws Exception {
             

        Parser parser = new Parser();
        parser.parseFile("config.txt");

        Map<String, AtomicBoolean> atomicFlags = new ConcurrentHashMap<String, AtomicBoolean>();
        AtomicBoolean request = new AtomicBoolean();
        AtomicBoolean ready = new AtomicBoolean();
        AtomicBoolean release = new AtomicBoolean();
        AtomicBoolean endThread = new AtomicBoolean();

        request.set(false);
        ready.set(false);
        release.set(false);
        endThread.set(false);

        atomicFlags.put("request", request);
        atomicFlags.put("ready", ready);
        atomicFlags.put("release", release);

        Network net1 = new Network(parser.get_node_info(), atomicFlags);
        System.out.println("Done!");

        net1.start(); // Start CL Protocol;
        System.out.println("Network Thread started...");
        System.out.println("-------------------------");

        // BufferedWriter for writing to files.
        BufferedWriter metricFile = new BufferedWriter(new FileWriter("metric_" + net1.get_my_node_id() + ".csv"));
        metricFile.write("Format: <csExeTime>, <interRequestDelay>\n");

        // Create hook to catch CTRL+C and clean up and close all relevant I/O before exiting.
        Runtime.getRuntime().addShutdownHook(new Thread() 
        {
            public void run() {
                System.out.println("Closing FileWriters...");

                try {
                    metricFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Application Done Cleaning...");
                }
        });

        // Mean Values of Global Parameters
        double interRequestDelay = NodeInfo.interRequestDelay; // In milliseconds
        double csExeTime = NodeInfo.csExeTime; // In milliseconds
        int maxRequests = NodeInfo.maxRequests;

        // Random number generators
        RandomNumberGenerator rng = RandomNumberGenerator.Exponential;


        // Actual Time Values
        double csExeTimeRand;
        double interRequestDelayRand;

        // Duration to time CS Execution Time
        int currRequest = 1;
        Instant start;
        Instant end;
        Duration timeElapsed;

        while (maxRequests > 0)
        {

            interRequestDelayRand = rng.getRandom(interRequestDelay) / 1000.0;
            csExeTimeRand = rng.getRandom(csExeTime) / 1000.0;

            System.out.println("[APPLICATION]: Trying Request " + currRequest++);
            System.out.println("----------------------------------------------");
            maxRequests--;

            request.set(true);

            // Do nothing while waiting for ready flag to be available
            while (!ready.get())
            {
            }

            ready.set(false); // Ready no longer true

            end = Instant.now();
            timeElapsed = Duration.between(end, end); // To set a timeElapsed of 0.
            start =  Instant.now();
            while ((timeElapsed.toMillis() / 1000.0) < csExeTimeRand)
            {
                end = Instant.now();
                timeElapsed = Duration.between(start, end);
            }

            System.out.println("[APPLICATION]: Time Executing is " +
                                timeElapsed.toMillis() + "ms");
            System.out.println("[APPLICATION]: Time Should Have Executed: " + csExeTimeRand * 1000 + "ms");
            System.out.println("[APPLICATION]: Done Executing");
            metricFile.write(timeElapsed + ", ");

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
            System.out.println("[APPLICATION]: Time Should Have Waited " + interRequestDelayRand * 1000 + "ms");
            metricFile.write(timeElapsed + "\n");

        }

        System.out.println("[APPLICATION]: All Requests satisfied ");
        System.out.println("---------------------------------");

        metricFile.close(); // Close file writer
        endThread.set(true); // Should end all threads.

    }

}


