package yuriy.weiss.web.server;

import akka.actor.typed.ActorSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import yuriy.weiss.web.server.actor.Step01MainDispatcher;
import yuriy.weiss.web.server.kpi.KpiHolder;
import yuriy.weiss.web.server.registry.RequestsRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ReactiveWebServiceApplication {

    public static void main( String[] args ) {
        ConfigurableApplicationContext context = SpringApplication.run( ReactiveWebServiceApplication.class, args );

        RequestsRegistry registry = context.getBean( RequestsRegistry.class );
        ScheduledExecutorService cleaningService = Executors.newSingleThreadScheduledExecutor();
        cleaningService.scheduleAtFixedRate( registry::removeRepliedRequests, 1L, 5L, TimeUnit.SECONDS );

        KpiHolder kpiHolder = context.getBean( KpiHolder.class );
        ScheduledExecutorService updateKpiService = Executors.newSingleThreadScheduledExecutor();
        updateKpiService.scheduleAtFixedRate( kpiHolder::updateCountersAndPrintStats, 5L, 5L, TimeUnit.SECONDS );

        final ActorSystem<Step01MainDispatcher.Command> mainDispatcher =
                ActorSystem.create( Step01MainDispatcher.create( 200, registry ), "step01MainDispatcher" );
        mainDispatcher.logConfiguration();
        mainDispatcher.tell( new Step01MainDispatcher.PopulateActors() );

        MessageHandler messageHandler = context.getBean( MessageHandler.class );
        messageHandler.setMainDispatcher( mainDispatcher );
    }
}
