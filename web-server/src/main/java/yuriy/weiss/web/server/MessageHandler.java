package yuriy.weiss.web.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import akka.actor.typed.ActorRef;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import yuriy.weiss.common.model.IsProcessedResponse;
import yuriy.weiss.common.model.ProcessingResponse;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.common.model.StartProcessingResponse;
import yuriy.weiss.web.server.actor.Step01MainDispatcher;
import yuriy.weiss.web.server.kpi.KpiHolder;
import yuriy.weiss.web.server.process.MessageProcessor;
import yuriy.weiss.web.server.registry.RequestProcessingState;
import yuriy.weiss.web.server.registry.RequestsRegistry;

@Component
@Slf4j
public class MessageHandler {

    private final RequestsRegistry requestsRegistry;
    private final KpiHolder kpiHolder;
    private ActorRef<Step01MainDispatcher.Command> mainDispatcher = null;

    @Autowired
    public MessageHandler( final RequestsRegistry requestsRegistry, KpiHolder kpiHolder ) {
        this.requestsRegistry = requestsRegistry;
        this.kpiHolder = kpiHolder;
    }

    public void setMainDispatcher( final ActorRef<Step01MainDispatcher.Command> mainDispatcher ) {
        this.mainDispatcher = mainDispatcher;
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
        if ( mainDispatcher == null ) {
            return Mono.empty();
        }
        log.trace( "got request with id: {}", request.getRequestId() );
        kpiHolder.getCurrRequests().getAndIncrement();
        requestsRegistry.putToCache( request );
        mainDispatcher.tell( new Step01MainDispatcher.StartMessageProcessing( request ) );
        return Mono.empty();
    }

    public Mono<ServerResponse> isProcessed( final ServerRequest request ) {
        String requestId = request.pathVariable( "requestId" );
        boolean processed = requestsRegistry.get( requestId ).getState() == RequestProcessingState.PROCESSED;
        return ServerResponse.ok()
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue( new IsProcessedResponse( requestId, processed ) ) );
    }

    public Mono<ServerResponse> getProcessingResult( final ServerRequest request ) {
        String requestId = request.pathVariable( "requestId" );
        String convertedMessage = requestsRegistry.get( requestId ).getConvertedMessage();
        requestsRegistry.updateState( requestId, RequestProcessingState.RESPONSE_SENT );
        kpiHolder.getCurrProcessed().getAndIncrement();
        return ServerResponse.ok()
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue( new ProcessingResponse( requestId, "success", convertedMessage ) ) );
    }
}
