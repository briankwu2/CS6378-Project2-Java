import java.time.*;

public class testingDuration {
 
    public static void main(String[] args) {
    
        Instant start;
        Instant end;
       
        start = Instant.now();
        end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);

        System.out.println(Long.toString(timeElapsed.toNanos()));

        double meanExe = 100;
        RandomNumberGenerator rand = RandomNumberGenerator.Exponential;
        double randomTime = rand.getRandom(meanExe);
            
        for (int i = 0; i < 100; i++)
        {
            randomTime = rand.getRandom(1.0 / meanExe);
            System.out.println(randomTime);
        }

        };

    }

