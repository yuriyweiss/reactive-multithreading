package yuriy.weiss.common.model;

public class StartProcessingResponse {
    private String status;

    public StartProcessingResponse() {
    }

    public StartProcessingResponse( final String status ) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus( final String status ) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StartProcessingResponse{" +
                "status='" + status + '\'' +
                '}';
    }
}
