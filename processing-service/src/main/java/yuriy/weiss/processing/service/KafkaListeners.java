package yuriy.weiss.processing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
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
    private static final int UPPER_QUEUE_BOUNDARY = 50;
    private static final long WAIT_UNTIL_QUEUE_DRAINED = 200L;

    private final JdbcTemplate mysqlJdbcTemplate;
    private final ObjectMapper objectMapper;
    private final KpiHolder kpiHolder;

    private final ExecutorService executorService = Executors.newFixedThreadPool( 20 );

    @Autowired
    public KafkaListeners(
            JdbcTemplate mysqlJdbcTemplate,
            ObjectMapper objectMapper,
            KpiHolder kpiHolder ) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.objectMapper = objectMapper;
        this.kpiHolder = kpiHolder;
    }

    @KafkaListener( topics = "REACT-REQUEST",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "10" )
    public void listenForRequests( final String message ) {
        log.trace( "GOT message: {}", message );
        kpiHolder.getCurrRequests().getAndIncrement();
        try {
            final StartProcessingRequest request = objectMapper.readValue( message, StartProcessingRequest.class );
            while ( ( ( ThreadPoolExecutor ) executorService ).getQueue().size() > UPPER_QUEUE_BOUNDARY ) {
                ThreadUtils.sleep( WAIT_UNTIL_QUEUE_DRAINED );
            }
            executorService.submit( () -> processRequest( request ) );
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

    public void logQueueSize() {
        log.info("QUEUE size: {}", ( ( ThreadPoolExecutor ) executorService ).getQueue().size());
    }
}
