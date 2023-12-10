package yuriy.weiss.common.utils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Утилиты для работы с датами.
 */
public class DateUtils {
    public static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of( "Europe/Moscow" );
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone( DEFAULT_TIME_ZONE );
    public static final ZoneOffset DEFAULT_ZONE_OFFSET = DEFAULT_TIME_ZONE.getRules().getOffset( Instant.now() );

    public static final String DATE_HH_MM_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_HH_MM_PATTERN = "dd.MM.yyyy HH:mm";
    public static final String DATE_TIME_HH_MM_SS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ofPattern(
            DATE_TIME_HH_MM_PATTERN ).withZone( DEFAULT_TIME_ZONE );
    public static final DateTimeFormatter LOCAL_DATE_TIME_LONG =
            DateTimeFormatter.ofPattern( DATE_TIME_HH_MM_SS_PATTERN ).withZone( DEFAULT_TIME_ZONE );
    public static final DateTimeFormatter LOCAL_DATE = DateTimeFormatter.ofPattern( DATE_HH_MM_PATTERN ).withZone(
            DEFAULT_TIME_ZONE );

    private DateUtils() {
        //empty constructor
    }

    private static SimpleDateFormat getSimpleDateFormatDateTimeHhMmPattern() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat( DATE_TIME_HH_MM_PATTERN );
        simpleDateFormat.setTimeZone( DEFAULT_TIMEZONE );
        return simpleDateFormat;
    }

    public static String formatDateAndShortTime( Date date ) {
        return date == null ? "" : getSimpleDateFormatDateTimeHhMmPattern().format( date );
    }

    public static String formatDateAndShortTime( LocalDateTime dateTime ) {
        return dateTime == null ? "" : LOCAL_DATE_TIME.format( dateTime );
    }

    public static String formatDateAndLongTime( LocalDateTime dateTime ) {
        return dateTime == null ? "" : LOCAL_DATE_TIME_LONG.format( dateTime );
    }

    public static String formatDate( LocalDate date ) {
        return date == null ? "" : LOCAL_DATE.format( date );
    }

    public static String formatDate( LocalDateTime date ) {
        return date == null ? "" : LOCAL_DATE_TIME.format( date );
    }

    public static String formatDate( final LocalDateTime date, final String format ) {
        return date == null ? "" : DateTimeFormatter.ofPattern( format ).withZone( DEFAULT_TIME_ZONE ).format( date );
    }

    public static LocalDate parseDate( String date ) {
        return date == null || date.isEmpty() ? null : LocalDate.parse( date, LOCAL_DATE );
    }

    public static LocalDate parseDate( String date, String format ) {
        return date == null || date.isEmpty() ? null : LocalDate.parse( date,
                DateTimeFormatter.ofPattern( format ).withZone( DEFAULT_TIME_ZONE ) );
    }

    public static LocalDate parseDate( String date, DateTimeFormatter formatter ) {
        return date == null || date.isEmpty() ? null : LocalDate.parse( date, formatter );
    }

    public static LocalDateTime parseDateTime( String date, DateTimeFormatter formatter ) {
        return date == null || date.isEmpty() ? null : LocalDateTime.parse( date, formatter );
    }

    public static String getLocalDateStringOrNull( String dateTimeString, DateTimeFormatter dateTimeFormatter,
            DateTimeFormatter dateFormatter ) {
        LocalDateTime dateTime = parseDateTime( dateTimeString, dateTimeFormatter );
        return dateTime == null ? null : dateTime.toLocalDate().format( dateFormatter );
    }

    public static LocalDateTime formatDateTime( String date ) {
        return date == null || date.isEmpty() ? null : LocalDateTime.parse( date, LOCAL_DATE_TIME );
    }

    public static LocalDateTime formatDateTime( String date, String format ) {
        return date == null || date.isEmpty() ? null : LocalDateTime.parse( date,
                DateTimeFormatter.ofPattern( format ).withZone( DEFAULT_TIME_ZONE ) );
    }

    public static Date toDate( LocalDateTime localDateTime ) {
        return localDateTime == null ? null : Date.from(
                localDateTime.toInstant( DEFAULT_TIME_ZONE.getRules().getOffset( localDateTime ) ) );
    }

    public static Date toDate( LocalDate localDate ) {
        return localDate == null ? null : Date.from( localDate.atStartOfDay( DEFAULT_TIME_ZONE ).toInstant() );
    }

    public static LocalDateTime toLocalDateTime( Date date ) {
        return date == null ? null : date.toInstant().atZone( ZoneId.systemDefault() ).toLocalDateTime();
    }

    public static Calendar toCalendar( LocalDateTime localDateTime ) {
        return localDateTime == null ? null : GregorianCalendar.from( localDateTime.atZone( DEFAULT_TIME_ZONE ) );
    }

    public static Calendar toCalendar( LocalDate localDate ) {
        return localDate == null ? null : GregorianCalendar.from( localDate.atStartOfDay( DEFAULT_TIME_ZONE ) );
    }

    public static LocalDateTime toLocalDateTime( Calendar calendar ) {
        return calendar == null ? null : LocalDateTime.ofInstant( calendar.toInstant(), DEFAULT_TIME_ZONE );
    }

    public static long toEpochMilli( LocalDateTime localDateTime ) {
        return localDateTime == null ? 0l : localDateTime.toInstant( DEFAULT_ZONE_OFFSET ).toEpochMilli();
    }
}
