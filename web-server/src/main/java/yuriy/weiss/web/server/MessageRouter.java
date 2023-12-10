package yuriy.weiss.web.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration( proxyBeanMethods = false )
public class MessageRouter {

    @Bean
    public RouterFunction<ServerResponse> route( MessageHandler messageHandler ) {
        return RouterFunctions
                .route()
                .POST( "/startProcessing", accept( MediaType.APPLICATION_JSON ), messageHandler::startProcessing )
                .GET( "/isProcessed/{requestId}", messageHandler::isProcessed )
                .GET( "/getProcessingResult/{requestId}", messageHandler::getProcessingResult )
                .build();
    }
}
