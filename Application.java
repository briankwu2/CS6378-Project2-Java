import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.*;
import java.time.*;
import java.io.*;

public class Application {

    public static void main(String[] args) throws Exception {
             
        /**
         * TO DO:
         * - Figure out why csExeTime and interRequestDelay are so small?
         * - Fix the timeElapsed thing then
         * - Implement variables to count messages, time between sync delay, and system throughput
         */
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
        atomicFlags.put("thread", endThread);

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


        // Duration for Response Time
        Instant responseStart;
        Instant responseEnd;
        Duration responseElapsed;

        // Duration for System Throughput
        Instant sysStart;
        Instant sysEnd;
        Duration sysElapsed;
        double systemThroughput;
            

        sysStart = Instant.now(); // Start of requests 
        while (maxRequests > 0)
        {

            // Are in milliseconds
            interRequestDelayRand = rng.getRandom(1.0 / interRequestDelay); //lambda =  1 / mean interRequestDelay
            csExeTimeRand = rng.getRandom(1.0 / csExeTime); // lambda = 1 / mean csExeTime
            
            System.out.println("[APPLICATION]: Trying Request " + currRequest++);
            System.out.println("----------------------------------------------");
            maxRequests--;

            request.set(true);
            responseStart = Instant.now(); // Start measuring time of response time
            // Do nothing while waiting for ready flag to be available
            while (!ready.get())
            {
            }

            ready.set(false); // Ready no longer true

            end = Instant.now();
            timeElapsed = Duration.between(end, end); // To set a timeElapsed of 0.
            start =  Instant.now();

            while (timeElapsed.toMillis() < csExeTimeRand)
            {
                end = Instant.now();
                timeElapsed = Duration.between(start, end);
            }

            responseEnd = Instant.now(); // Stop measuring response time
            System.out.println("[APPLICATION]: Time Executing is " +
                                (timeElapsed.toMillis()) + "ms");
            System.out.println("[APPLICATION]: Time Should Have Executed: " + csExeTimeRand + "ms");
            System.out.println("[APPLICATION]: Done Executing");
            metricFile.write(Double.toString(timeElapsed.toMillis()) + ", ");

            // Waiting interRequestDelay before sending next request.
            release.set(true);

            start =  Instant.now();
            end = Instant.now();
            timeElapsed = Duration.between(start, end);
            while (timeElapsed.toMillis()  < interRequestDelayRand)
            {
                end = Instant.now();
                timeElapsed = Duration.between(start, end);
            }

            System.out.println("[APPLICATION]: Time Waited " + (timeElapsed.toMillis()) + "ms" );
            System.out.println("[APPLICATION]: Time Should Have Waited " + interRequestDelayRand + "ms");

            responseElapsed = Duration.between(responseStart, responseEnd);
            metricFile.write(Double.toString(timeElapsed.toMillis()) + "\n"); // Writes interRequestDelay
            metricFile.write("Response Time:" + Double.toString(responseElapsed.toMillis()) + "\n"); // Writes response time

            sysEnd = Instant.now(); // 
            sysElapsed = Duration.between(sysStart, sysEnd);
            systemThroughput =  (double) currRequest / (sysElapsed.toMillis() / 1000.0);
            metricFile.write("System Throughput: " + Double.toString(systemThroughput) + "\n");
            
        }

        System.out.println("[APPLICATION]: All Requests satisfied ");
        System.out.println("---------------------------------");

        metricFile.close(); // Close file writer
        // endThread.set(true); // Should end all threads.

    }

}


