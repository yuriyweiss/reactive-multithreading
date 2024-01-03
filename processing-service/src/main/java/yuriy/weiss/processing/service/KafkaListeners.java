package yuriy.weiss.processing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import yuriy.weiss.common.kpi.KpiHolder;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.common.utils.ThreadUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class KafkaListeners {

    private static final long AVERAGE_LONG_TASK_SLEEP = 1000L;
    private static final long WAIT_UNTIL_QUEUE_DRAINED = 200L;

    private final JdbcTemplate mysqlJdbcTemplate;
    private final ObjectMapper objectMapper;
    private final KpiHolder kpiHolder;

    @Value( "${spring.kafka.topic.partitions.count}" )
    private int partitionsCount;
    @Value( "${message.processor.threads.in.pool}" )
    private int threadsInPool;
    private int upperQueueBoundary;

    private ExecutorService[] executorServices;

    @Autowired
    public KafkaListeners(
            JdbcTemplate mysqlJdbcTemplate,
            ObjectMapper objectMapper,
            KpiHolder kpiHolder ) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.objectMapper = objectMapper;
        this.kpiHolder = kpiHolder;
    }

    @PostConstruct
    private void setup() {
        System.out.println( "partitions count: " + partitionsCount );
        System.out.println( "threads in pool: " + threadsInPool );
        this.executorServices = new ExecutorService[partitionsCount];
        for ( int i = 0; i < partitionsCount; i++ ) {
            executorServices[i] = Executors.newFixedThreadPool( threadsInPool );
        }
        this.upperQueueBoundary = ( int ) Math.round( threadsInPool * 1.5 );
    }

    @KafkaListener( topics = "REACT-REQUEST",
            concurrency = "${spring.kafka.topic.partitions.count}",
            containerFactory = "kafkaListenerContainerFactory" )
    public void requestsListener( @Payload final String message,
            @Header( KafkaHeaders.RECEIVED_PARTITION ) int partition ) {
        loadMessageFromPartition( message, partition );
    }

    private void loadMessageFromPartition( String message, int partition ) {
        log.trace( "GOT message: {}", message );
        kpiHolder.getCurrRequests().getAndIncrement();
        try {
            final StartProcessingRequest request = objectMapper.readValue( message, StartProcessingRequest.class );
            while ( getQueueSize( executorServices[partition] ) > upperQueueBoundary ) {
                ThreadUtils.sleep( WAIT_UNTIL_QUEUE_DRAINED );
            }
            executorServices[partition].submit( () -> processRequest( request ) );
            log.trace( "SUBMITTED: {}", request.getRequestId() );
        } catch ( Exception e ) {
            log.error( "ERROR request parsing and putting to queue [{}]", message );
            log.debug( "error stacktrace:", e );
        }
    }

    private void processRequest( final StartProcessingRequest request ) {
        try {
            saveRequestToDb( request );
            performLongRunningTask();
            changeToProcessedStatus( request, "processed_response_" + request.getMessage() );
            log.trace( "PROCESSED request : {}", request.getRequestId() );
            kpiHolder.getCurrProcessed().getAndIncrement();
        } catch ( Exception e ) {
            log.error( "ERROR request processing: {}", request.getRequestId() );
            log.debug( "error stacktrace:", e );
        }
    }

    private void saveRequestToDb( StartProcessingRequest request ) {
        int count = mysqlJdbcTemplate.queryForObject(
                "select count(*) from request_data where rquid = ?",
                Integer.class, request.getRequestId() );
        if ( count == 0 ) {
            mysqlJdbcTemplate.update(
                    "insert into request_data(rquid, status, create_date, request_date, message) " +
                            "values(?, ?, ?, ?, ?)",
                    request.getRequestId(), "PROCESSING", LocalDateTime.now(),
                    request.getRequestDateTime(), request.getMessage() );
        }
    }

    private void performLongRunningTask() {
        ThreadUtils.sleep( ThreadUtils.nextGaussian( AVERAGE_LONG_TASK_SLEEP ) );
    }

    private void changeToProcessedStatus( final StartProcessingRequest request, final String response ) {
        mysqlJdbcTemplate.update(
                "update request_data " +
                        "set status = 'PROCESSED', " +
                        "    response_date = ?, " +
                        "    response = ? " +
                        "where rquid = ?",
                LocalDateTime.now(), response, request.getRequestId() );
    }

    public void logQueueSizes() {
        StringBuilder sizes = new StringBuilder();
        for ( ExecutorService executorService : executorServices ) {
            sizes.append( " " ).append( getQueueSize( executorService ) );
        }
        log.info( "QUEUE sizes:{}", sizes );
    }

    private int getQueueSize( ExecutorService executorService ) {
        return ( ( ThreadPoolExecutor ) executorService ).getQueue().size();
    }
}
