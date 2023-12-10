package yuriy.weiss.common.model;

public class IsProcessedResponse {
    private String requestId;
    private boolean processed;

    public IsProcessedResponse() {
    }

    public IsProcessedResponse( final String requestId, final boolean processed ) {
        this.requestId = requestId;
        this.processed = processed;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId( final String requestId ) {
        this.requestId = requestId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed( final boolean processed ) {
        this.processed = processed;
    }

    @Override
    public String toString() {
        return "IsProcessedResponse{" +
                "requestId='" + requestId + '\'' +
                ", processed=" + processed +
                '}';
    }
}
