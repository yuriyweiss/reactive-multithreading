package yuriy.weiss.common.model;

public enum RequestStatus {

    CREATED( false ),
    SENT( false ),
    PROCESSING( false ),
    PROCESSED( true ),
    ERROR( true );

    private final boolean finished;

    RequestStatus( boolean finished ) {
        this.finished = finished;
    }

    public boolean isFinished() {
        return finished;
    }

    public static RequestStatus getByName( final String statusName ) {
        for ( RequestStatus requestStatus : values() ) {
            if ( statusName.equals( requestStatus.name() ) ) {
                return requestStatus;
            }
        }
        return null;
    }
}
