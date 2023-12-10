package yuriy.weiss.web.server.process;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.web.server.registry.RegistryItem;
import yuriy.weiss.web.server.registry.RequestProcessingState;
import yuriy.weiss.web.server.registry.RequestsRegistry;

@Component
@Slf4j
public class MessageProcessor {

    private final RequestsRegistry requestsRegistry;
    private final ArrayBlockingQueue<StartProcessingRequest> queue = new ArrayBlockingQueue<>( 20000 );

    @Autowired
    public MessageProcessor( final RequestsRegistry requestsRegistry ) {
        this.requestsRegistry = requestsRegistry;
    }

    public void startProcessing() {
        log.info( "starting MessageProcessor" );
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit( () -> {
            while ( !Thread.currentThread().isInterrupted() ) {
                try {
                    StartProcessingRequest request = queue.take();
                    log.info( "processing message: {}", request.getRequestId() );
                    RegistryItem item = requestsRegistry.get( request.getRequestId() );
                    item.setConvertedMessage( request.getMessage() + " converted" );
                    item.setState( RequestProcessingState.PROCESSED );
                } catch ( InterruptedException e ) {
                    Thread.currentThread().interrupt();
                }
            }
        } );
    }

    public void putRequestToQueue( final StartProcessingRequest request ) {
        try {
            queue.put( request );
        } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
        }
    }

    public void logQueueSize() {
        log.info( "QUEUE SIZE: {}", queue.size() );
    }
}
