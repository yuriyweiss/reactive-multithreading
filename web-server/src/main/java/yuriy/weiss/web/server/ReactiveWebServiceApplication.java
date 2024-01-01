package yuriy.weiss.web.server;

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
@ComponentScan( { "yuriy.weiss.web.server", "yuriy.weiss.common" } )
public class ReactiveWebServiceApplication {

    public static void main( String[] args ) {
        ConfigurableApplicationContext context = SpringApplication.run( ReactiveWebServiceApplication.class, args );

        KpiHolder kpiHolder = context.getBean( KpiHolder.class );
        ScheduledExecutorService updateKpiService = Executors.newSingleThreadScheduledExecutor();
        updateKpiService.scheduleAtFixedRate( kpiHolder::updateCountersAndPrintStats, 5L, 5L, TimeUnit.SECONDS );
    }
}
