package yuriy.weiss.common.model;

public class ProcessingResponse {

    private String requestId;
    private String status;
    private String convertedMessage;

    public ProcessingResponse() {
    }

    public ProcessingResponse( final String requestId, final String status, final String convertedMessage ) {
        this.requestId = requestId;
        this.status = status;
        this.convertedMessage = convertedMessage;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId( final String requestId ) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus( final String status ) {
        this.status = status;
    }

    public String getConvertedMessage() {
        return convertedMessage;
    }

    public void setConvertedMessage( final String convertedMessage ) {
        this.convertedMessage = convertedMessage;
    }

    @Override
    public String toString() {
        return "ProcessingResponse{" +
                "requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", convertedMessage='" + convertedMessage + '\'' +
                '}';
    }
}
