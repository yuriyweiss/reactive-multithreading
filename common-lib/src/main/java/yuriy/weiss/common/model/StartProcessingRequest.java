package yuriy.weiss.common.model;

import java.time.LocalDateTime;

public class StartProcessingRequest {
    private String requestId;
    private LocalDateTime requestDateTime;
    private String message;

    public StartProcessingRequest() {
    }

    public StartProcessingRequest( final String requestId, final LocalDateTime requestDateTime, final String message ) {
        this.requestId = requestId;
        this.requestDateTime = requestDateTime;
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId( final String requestId ) {
        this.requestId = requestId;
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    public void setRequestDateTime( final LocalDateTime requestDateTime ) {
        this.requestDateTime = requestDateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage( final String message ) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "StartProcessingRequest{" +
                "requestId='" + requestId + '\'' +
                ", requestDateTime=" + requestDateTime +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals( final Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        final StartProcessingRequest that = ( StartProcessingRequest ) o;

        if ( !requestId.equals( that.requestId ) ) return false;
        if ( !requestDateTime.equals( that.requestDateTime ) ) return false;
        return message.equals( that.message );
    }

    @Override
    public int hashCode() {
        int result = requestId.hashCode();
        result = 31 * result + requestDateTime.hashCode();
        result = 31 * result + message.hashCode();
        return result;
    }
}
