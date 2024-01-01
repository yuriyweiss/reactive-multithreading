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
public class SmallBatchGenerator {

    private static final int REQUESTS_COUNT = 100;
    private static final boolean NEED_WAIT_FOR_RESPONSE = false;

    private final HttpCommandsSender commandsSender = new HttpCommandsSender();

    public void sendRequestsWaitThenConsumeResults() throws IOException {
        log.info( "start generation" );
        List<StartProcessingRequest> requests = new ArrayList<>();
        for ( int i = 0; i < REQUESTS_COUNT; i++ ) {
            StartProcessingRequest request =
                    new StartProcessingRequest( UUID.randomUUID().toString(),
                            LocalDateTime.now(),
                            UUID.randomUUID() + " " + LocalDateTime.now() );
            requests.add( request );
            commandsSender.sendStartProcessingRequest( request );
            if ( i % 10 == 0 ) {
                ThreadUtils.sleep( 10L );
            }
        }
        log.info( "generated: {}", requests.size() );
        if ( NEED_WAIT_FOR_RESPONSE ) {
            List<StartProcessingRequest> waiting = new ArrayList<>( requests );
            while ( !waiting.isEmpty() ) {
                List<StartProcessingRequest> processed = new ArrayList<>();
                for ( int i = 0; i < waiting.size(); i++ ) {
                    StartProcessingRequest waitingRequest = waiting.get( i );
                    if ( commandsSender.isProcessed( waitingRequest ) ) {
                        processed.add( waitingRequest );
                    }
                    if ( i % 10 == 0 ) {
                        ThreadUtils.sleep( 10L );
                    }
                }
                waiting.removeAll( processed );
                log.info( "processed: {}", processed.size() );
            }
            for ( int i = 0; i < requests.size(); i++ ) {
                StartProcessingRequest request = requests.get( i );
                commandsSender.sendGetProcessingResult( request.getRequestId() );
                if ( i % 10 == 0 ) {
                    ThreadUtils.sleep( 10L );
                }
            }
        }
        log.info( "generation finished" );
    }
}
