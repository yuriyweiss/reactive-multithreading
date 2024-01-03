package yuriy.weiss.web.server.loader;

import lombok.extern.slf4j.Slf4j;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.common.utils.ThreadUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ConstantStreamGenerator {

    private static final int GENERATE_BATCH_SIZE = 120;
    private static final long GENERATION_DELAY = 250L;

    private final boolean waitForResponse;

    private final HttpCommandsSender commandsSender = new HttpCommandsSender();

    public ConstantStreamGenerator( boolean waitForResponse ) {
        this.waitForResponse = waitForResponse;
    }

    public void generateConstantRequestsStream() throws IOException {
        log.info( "start generation" );
        List<StartProcessingRequest> oldRequests = new ArrayList<>();
        while ( true ) {
            List<StartProcessingRequest> newRequests = new ArrayList<>();
            for ( int i = 0; i < GENERATE_BATCH_SIZE; i++ ) {
                StartProcessingRequest request =
                        new StartProcessingRequest( UUID.randomUUID().toString(),
                                LocalDateTime.now(),
                                UUID.randomUUID() + " " + LocalDateTime.now() );
                newRequests.add( request );
                commandsSender.sendStartProcessingRequest( request );
                if ( i % 10 == 0 ) {
                    ThreadUtils.sleep( 5L );
                }
            }
            log.info( "generated: {}", newRequests.size() );

            if (waitForResponse) {
                oldRequests.addAll( newRequests );
                List<StartProcessingRequest> processedRequests = new ArrayList<>();
                for ( int i = 0; i < oldRequests.size(); i++ ) {
                    StartProcessingRequest oldRequest = oldRequests.get( i );
                    if ( commandsSender.isProcessed( oldRequest ) ) {
                        processedRequests.add( oldRequest );
                    }
                /*
                if ( i % 30 == 0 ) {
                    ThreadUtils.sleep( 5L );
                }
                 */
                }
                log.info( "processed: {}", processedRequests.size() );
                oldRequests.removeAll( processedRequests );
                log.info( "waiting processing: {}", oldRequests.size() );

                for ( int i = 0; i < processedRequests.size(); i++ ) {
                    StartProcessingRequest processedRequest = processedRequests.get( i );
                    commandsSender.sendGetProcessingResult( processedRequest.getRequestId() );
                /*
                if ( i % 30 == 0 ) {
                    ThreadUtils.sleep( 5L );
                }
                 */
                }
            }

            ThreadUtils.sleep( GENERATION_DELAY );
        }
    }
}
