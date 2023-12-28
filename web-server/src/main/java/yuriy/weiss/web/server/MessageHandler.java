package yuriy.weiss.web.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import yuriy.weiss.common.model.*;
import yuriy.weiss.web.server.kpi.KpiHolder;

@Component
@Slf4j
public class MessageHandler {

    private final KpiHolder kpiHolder;
    private final JdbcTemplate mysqlJdbcTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageHandler(
            KpiHolder kpiHolder,
            JdbcTemplate mysqlJdbcTemplate,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper ) {
        this.kpiHolder = kpiHolder;
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public Mono<ServerResponse> startProcessing( final ServerRequest request ) {
        log.trace( "startProcessing" );
        return request
                .bodyToMono( StartProcessingRequest.class )
                .map( this::registerProcessingRequest )
                .then( ServerResponse.ok()
                        .contentType( MediaType.APPLICATION_JSON )
                        .body( BodyInserters.fromValue( new StartProcessingResponse( "success" ) ) ) );
    }

    private Mono<Void> registerProcessingRequest( final StartProcessingRequest request ) {
        log.trace( "RECEIVED: {}", request.getRequestId() );
        kpiHolder.getCurrRequests().getAndIncrement();
        saveRequestToDb( request );
        if ( sendRequestToKafka( request ) ) {
            updateDbStatus( request.getRequestId(), "SENT" );
        }
        log.trace( "SENT: {}", request.getRequestId() );
        return Mono.empty();
    }

    private void saveRequestToDb( StartProcessingRequest request ) {
        mysqlJdbcTemplate.update(
                "insert into request_data(rquid, status, create_date, message) " +
                        "values(?, ?, ?, ?)",
                request.getRequestId(), "CREATED", request.getRequestDateTime(), request.getMessage() );
    }

    private boolean sendRequestToKafka( StartProcessingRequest request ) {
        boolean result = true;
        try {
            kafkaTemplate.send( "REACT-REQUEST", objectMapper.writeValueAsString( request ) );
        } catch ( Exception e ) {
            log.error( "sendRequestToKafka failed", e );
            updateDbStatus( request.getRequestId(), "ERROR" );
            result = false;
        }
        return result;
    }

    private void updateDbStatus( String requestId, String status ) {
        mysqlJdbcTemplate.update(
                "update request_data set status = ? where rquid = ?",
                status, requestId );
    }

    public Mono<ServerResponse> isProcessed( final ServerRequest request ) {
        String requestId = request.pathVariable( "requestId" );
        return ServerResponse.ok()
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue(
                        new IsProcessedResponse( requestId, isRequestProcessed( requestId ) ) ) );
    }

    private boolean isRequestProcessed( String requestId ) {
        String status = mysqlJdbcTemplate.queryForObject(
                "select status from request_data where rquid = ?",
                String.class, requestId );
        RequestStatus requestStatus = RequestStatus.getByName( status );
        return requestStatus == null || requestStatus.isFinished();
    }

    public Mono<ServerResponse> getProcessingResult( final ServerRequest request ) {
        String requestId = request.pathVariable( "requestId" );
        kpiHolder.getCurrProcessed().getAndIncrement();
        return ServerResponse.ok()
                .contentType( MediaType.APPLICATION_JSON )
                .body( BodyInserters.fromValue( getProcessingResponse( requestId ) ) );
    }

    private ProcessingResponse getProcessingResponse( final String requestId ) {
        return mysqlJdbcTemplate.queryForObject(
                "select status, response from request_data where rquid = ?",
                ( rs, rowNum ) ->
                        new ProcessingResponse(
                                requestId,
                                rs.getString( "STATUS" ),
                                rs.getString( "RESPONSE" ) ),
                requestId );
    }
}
