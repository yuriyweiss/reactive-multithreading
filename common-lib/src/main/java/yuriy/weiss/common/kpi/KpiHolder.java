package yuriy.weiss.common.kpi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class KpiHolder {

    private static final int BYTES_IN_MB = 1024 * 1024;

    private final AtomicLong prevRequests = new AtomicLong( 0L );
    private final AtomicLong currRequests = new AtomicLong( 0L );

    private final AtomicLong prevProcessed = new AtomicLong( 0L );
    private final AtomicLong currProcessed = new AtomicLong( 0L );

    private double requestsPerSecond = 0.0;
    private double processedPerSecond = 0.0;
    private long requestsDelta;
    private long processedDelta;

    private long prevTime = System.currentTimeMillis();

    public AtomicLong getCurrRequests() {
        return currRequests;
    }

    public AtomicLong getCurrProcessed() {
        return currProcessed;
    }

    public void updateCountersAndPrintStats() {
        updateCounters();
        printStatistics();
    }

    private void updateCounters() {
        long currTime = System.currentTimeMillis();
        long timeDelta = currTime - prevTime;
        prevTime = currTime;
        double timeDeltaSeconds = timeDelta / 1000.0;

        requestsDelta = currRequests.get() - prevRequests.get();
        prevRequests.set( currRequests.get() );
        requestsPerSecond = requestsDelta / timeDeltaSeconds;

        processedDelta = currProcessed.get() - prevProcessed.get();
        prevProcessed.set( currProcessed.get() );
        processedPerSecond = processedDelta / timeDeltaSeconds;
    }

    private void printStatistics() {
        String throughput =
                String.format( "requests: %5d; reqPerSec: %8.2f; processed: %5d; procPerSec: %8.2f",
                        requestsDelta, requestsPerSecond, processedDelta, processedPerSecond );
        log.info( throughput );
        log.info( memoryStats() );
    }

    private String memoryStats() {
        // get Runtime instance
        Runtime instance = Runtime.getRuntime();
        String result = "Total Memory: " + instance.totalMemory() / BYTES_IN_MB + "; ";
        result += "Free Memory: " + instance.freeMemory() / BYTES_IN_MB + "; ";
        result += "Used Memory: " + ( instance.totalMemory() - instance.freeMemory() ) / BYTES_IN_MB + "; ";
        result += "Max Memory: " + instance.maxMemory() / BYTES_IN_MB;
        return result;
    }
}
