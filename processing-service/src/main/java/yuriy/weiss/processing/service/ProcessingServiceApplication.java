package yuriy.weiss.processing.service;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.kafka.AutoSubscription;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import yuriy.weiss.common.kpi.KpiHolder;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.common.utils.ThreadUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ComponentScan( { "yuriy.weiss.processing.service", "yuriy.weiss.common" } )
public class ProcessingServiceApplication {

    private static final int AVERAGE_SLEEP_INTERVAL = 1000;
    private static final int PARALLELISM = 32;

    private static JdbcTemplate mysqlJdbcTemplate;
    private static ObjectMapper objectMapper;
    private static KpiHolder kpiHolder;

    public static void main( String[] args ) {
        ConfigurableApplicationContext context = SpringApplication.run( ProcessingServiceApplication.class, args );

        mysqlJdbcTemplate = context.getBean( "mysqlJdbcTemplate", JdbcTemplate.class );
        objectMapper = context.getBean( ObjectMapper.class );
        kpiHolder = context.getBean( KpiHolder.class );

        final ActorSystem<Void> system = ActorSystem.create( Behaviors.empty(), "QuickStart" );
        system.logConfiguration();

        Config config = system.settings().config().getConfig( "processing-service-consumer" );
        ConsumerSettings<String, String> consumerSettings =
                ConsumerSettings.create( config, new StringDeserializer(), new StringDeserializer() );
        final AutoSubscription subscription = Subscriptions.topics( "REACT-REQUEST" );
        final Source<ConsumerRecord<String, String>, Consumer.Control> consumer =
                Consumer.plainSource( consumerSettings, subscription );
        consumer
                .mapAsyncUnordered( PARALLELISM, consumerRecord ->
                        CompletableFuture.runAsync( () -> processRecord( consumerRecord, system.log() ) ) )
                .runWith( Sink.ignore(), system );

        ScheduledExecutorService updateKpiService = Executors.newSingleThreadScheduledExecutor();
        updateKpiService.scheduleAtFixedRate( kpiHolder::updateCountersAndPrintStats, 5L, 5L, TimeUnit.SECONDS );
    }

    private static void processRecord( final ConsumerRecord<String, String> consumerRecord, Logger log ) {
        log.trace( "GOT record: {}", consumerRecord.value() );
        kpiHolder.getCurrRequests().getAndIncrement();
        try {
            StartProcessingRequest request =
                    objectMapper.readValue( consumerRecord.value(), StartProcessingRequest.class );
            if ( !messageStatusSent( request ) ) {
                log.info( "ignoring message not in SENT status: {}", request.getRequestId() );
                return;
            }
            changeToProcessingStatus( request );
            performLongRunningTask();
            changeToProcessedStatus( request );
            log.trace( "PROCESSED record : {}", request.getRequestId() );
            kpiHolder.getCurrProcessed().getAndIncrement();
        } catch ( Exception e ) {
            log.error( "ERROR record processing [{}]", consumerRecord.value() );
            log.info( "error stacktrace:", e );
        }
    }

    private static boolean messageStatusSent( StartProcessingRequest request ) {
        return 1 == mysqlJdbcTemplate.queryForObject(
                "select count(*) from request_data where rquid = ? and status = 'SENT'",
                Integer.class, request.getRequestId() );
    }

    private static void changeToProcessingStatus( StartProcessingRequest request ) {
        mysqlJdbcTemplate.update(
                "update request_data set status = 'PROCESSING' where rquid = ?", request.getRequestId() );
    }

    private static void performLongRunningTask() {
        ThreadUtils.sleep( ThreadUtils.nextGaussian( AVERAGE_SLEEP_INTERVAL ) );
    }

    private static void changeToProcessedStatus( StartProcessingRequest request ) {
        mysqlJdbcTemplate.update(
                "update request_data " +
                        "set status = 'PROCESSED', " +
                        "    response_date = ? " +
                        "where rquid = ?",
                LocalDateTime.now(), request.getRequestId() );
    }
}
