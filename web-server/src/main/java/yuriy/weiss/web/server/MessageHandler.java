package yuriy.weiss.web.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import yuriy.weiss.common.model.IsProcessedResponse;
import yuriy.weiss.common.model.ProcessingResponse;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.common.model.StartProcessingResponse;
import yuriy.weiss.web.server.kpi.KpiHolder;

@Component
@Slf4j
public class MessageHandler {

    private final KpiHolder kpiHolder;

    @Autowired
    public MessageHandler( KpiHolder kpiHolder ) {
        this.kpiHolder = kpiHolder;
    }

    public Mono<ServerResponse> startProcessing( final ServerRequest request ) {
        return request
                .bodyToMono( StartProcessingRequest.class )
                .map( this::registerProcessingRequest )
                .then( ServerResponse.ok()
                        .contentType( MediaType.APPLICATION_JSON )
                        .body( BodyInserters.fromValue( new StartProcessingResponse( "success" ) ) ) );
    }

    private Mono<Void> registerProcessingRequest( final StartProcessingRequest request ) {
        log.trace( "got request with id: {}", request.getRequestId() );
        kpiHolder.getCurrRequests().getAndIncrement();
        // TODO save request to DB as CREATED
        // TODO send request to Kafka
        // TODO update DB status SENT
        return Mono.empty();
    }

    public Mono<ServerResponse> isProcessed( final ServerRequest request ) {
        String requestId = request.pathVariable( "requestId" );
        // TODO get request status from DB, check if PROCESSED
        return ServerResponse.ok()
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue( new IsProcessedResponse( requestId, true ) ) );
    }

    public Mono<ServerResponse> getProcessingResult( final ServerRequest request ) {
        String requestId = request.pathVariable( "requestId" );
        // TODO get response from DB if PROCESSED
        kpiHolder.getCurrProcessed().getAndIncrement();
        return ServerResponse.ok()
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue( new ProcessingResponse( requestId, "success", "DUMMY" ) ) );
    }
}
