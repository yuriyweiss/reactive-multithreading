package yuriy.weiss.processing.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;
import yuriy.weiss.common.kpi.KpiHolder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableKafka
@ComponentScan( { "yuriy.weiss.processing.service", "yuriy.weiss.common" } )
public class ProcessingServiceApplication {

    public static void main( String[] args ) {
        ConfigurableApplicationContext context = SpringApplication.run( ProcessingServiceApplication.class, args );

        KpiHolder kpiHolder = context.getBean( KpiHolder.class );
        KafkaListeners kafkaListeners = context.getBean( KafkaListeners.class );

        ScheduledExecutorService updateKpiService = Executors.newSingleThreadScheduledExecutor();
        updateKpiService.scheduleAtFixedRate( () -> {
            kpiHolder.updateCountersAndPrintStats();
            kafkaListeners.logQueueSize();
        }, 5L, 5L, TimeUnit.SECONDS );
    }
}
