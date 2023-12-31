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
import org.springframework.jdbc.core.JdbcTemplate;
import yuriy.weiss.common.model.StartProcessingRequest;

@SpringBootApplication
public class ProcessingServiceApplication {

    private static JdbcTemplate mysqlJdbcTemplate;
    private static ObjectMapper objectMapper;

    public static void main( String[] args ) {
        ConfigurableApplicationContext context = SpringApplication.run( ProcessingServiceApplication.class, args );

        mysqlJdbcTemplate = context.getBean( "mysqlJdbcTemplate", JdbcTemplate.class );
        objectMapper = context.getBean( ObjectMapper.class );

        final ActorSystem<Void> system = ActorSystem.create( Behaviors.empty(), "QuickStart" );
        system.logConfiguration();

        Config config = system.settings().config().getConfig( "processing-service-consumer" );
        ConsumerSettings<String, String> consumerSettings =
                ConsumerSettings.create( config, new StringDeserializer(), new StringDeserializer() );
        final AutoSubscription subscription = Subscriptions.topics( "REACT-REQUEST" );
        final Source<ConsumerRecord<String, String>, Consumer.Control> consumer =
                Consumer.plainSource( consumerSettings, subscription );
        consumer.runWith( Sink.foreach( consumerRecord -> processRecord( consumerRecord, system.log() ) ), system );
    }

    private static void processRecord( final ConsumerRecord<String, String> consumerRecord, Logger log ) {
        log.info( "got record: {}", consumerRecord.value() );
        try {
            StartProcessingRequest request =
                    objectMapper.readValue( consumerRecord.value(), StartProcessingRequest.class );
            // TODO check if status is SENT, then start processing, else ignore message - it is dublicate
            // TODO change status to PROCESSING
            // TODO perform long running task
            // TODO change status to PROCESSED
            // mysqlJdbcTemplate()
        } catch ( Exception e ) {
            // TODO log error and ignore
        }
    }
}
