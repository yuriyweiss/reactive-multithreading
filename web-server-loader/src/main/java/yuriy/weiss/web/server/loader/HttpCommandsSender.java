package yuriy.weiss.web.server.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import yuriy.weiss.common.model.IsProcessedResponse;
import yuriy.weiss.common.model.ProcessingResponse;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.common.model.StartProcessingResponse;
import yuriy.weiss.common.utils.ThreadUtils;

import java.io.IOException;

@Slf4j
public class HttpCommandsSender {

    private final WebClient webClient = WebClient.create( "http://localhost:8080" );

    public void sendStartProcessingRequest( final StartProcessingRequest request ) throws IOException {
        log.trace( "send POST startProcessing" );
        Mono<StartProcessingResponse> result = webClient.post()
                .uri( "/startProcessing" )
                .contentType( MediaType.APPLICATION_JSON )
                .bodyValue( request )
                .retrieve()
                .bodyToMono( StartProcessingResponse.class );
        result.subscribe( response ->
                log.trace( "response POST startProcessing: {}", response ) );
    }

    public void sendGetIsProcessed( final String requestId ) throws IOException {
        boolean processed = false;
        while ( !processed ) {
            log.trace( "send GET isProcessed" );
            IsProcessedResponse isProcessedResponse = webClient.get()
                    .uri( "/isProcessed/" + requestId )
                    .retrieve()
                    .bodyToMono( IsProcessedResponse.class )
                    .block();
            log.trace( "response GET isProcessed: {}", isProcessedResponse );
            processed = isProcessedResponse.isProcessed();
            if ( !processed ) {
                ThreadUtils.sleep( 50L );
            }
        }
    }

    public boolean isProcessed( final StartProcessingRequest request ) {
        log.trace( "send GET isProcessed" );
        IsProcessedResponse isProcessedResponse = webClient.get()
                .uri( "/isProcessed/" + request.getRequestId() )
                .retrieve()
                .bodyToMono( IsProcessedResponse.class )
                .block();
        log.trace( "response GET isProcessed: {}", isProcessedResponse );
        return isProcessedResponse.isProcessed();
    }

    public void sendGetProcessingResult( final String requestId ) throws IOException {
        log.trace( "send GET getProcessingResult" );
        Mono<ProcessingResponse> result = webClient.get()
                .uri( "/getProcessingResult/" + requestId )
                .retrieve()
                .bodyToMono( ProcessingResponse.class );
        result.subscribe( response ->
                log.trace( "response GET getProcessingResult: {}", response ) );
    }
}
