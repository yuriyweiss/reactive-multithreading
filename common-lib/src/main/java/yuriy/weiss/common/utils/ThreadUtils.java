package yuriy.weiss.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

/**
 * Реализация метода sleep с периодическим логированием времени просыпания.
 */
@Slf4j
public class ThreadUtils {

    private static final long LOG_INTERVAL = 10000L;
    private static final Random random = new Random();

    private ThreadUtils() {
    }

    public static void sleep( long millisToSleep ) {
        String finishTimeString = LocalDateTime.now()
                .plus( millisToSleep, ChronoUnit.MILLIS )
                .format( DateTimeFormatter.ISO_DATE_TIME );
        try {
            long finishTimeMillis = System.currentTimeMillis() + millisToSleep;
            long sleepInterval;
            do {
                long currentTimeMillis = System.currentTimeMillis();
                long sleepUntil = Math.min( currentTimeMillis + LOG_INTERVAL, finishTimeMillis );
                sleepInterval = sleepUntil - currentTimeMillis;
                if ( sleepInterval > 0L ) {
                    Thread.sleep( sleepInterval );
                }
                log.trace( "ожидание до: {}", finishTimeString );
            } while ( sleepInterval == LOG_INTERVAL );
        } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
        }
    }

    public static long nextGaussian( int average ) {
        int result;
        do {
            double val = random.nextGaussian() * ( average / 5.0 ) + average;
            result = ( int ) Math.round( val );
        } while ( result <= 0 );
        return result;
    }
}
