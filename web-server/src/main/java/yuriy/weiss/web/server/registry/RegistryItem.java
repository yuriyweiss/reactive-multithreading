package yuriy.weiss.web.server.registry;

public class RegistryItem {

    private RequestProcessingState state;
    private String convertedMessage;

    public RegistryItem() {
    }

    public RegistryItem( final RequestProcessingState state, final String convertedMessage ) {
        this.state = state;
        this.convertedMessage = convertedMessage;
    }

    public RequestProcessingState getState() {
        return state;
    }

    public void setState( final RequestProcessingState state ) {
        this.state = state;
    }

    public String getConvertedMessage() {
        return convertedMessage;
    }

    public void setConvertedMessage( final String convertedMessage ) {
        this.convertedMessage = convertedMessage;
    }

    @Override
    public String toString() {
        return "RegistryItem{" +
                "state=" + state +
                ", convertedMessage='" + convertedMessage + '\'' +
                '}';
    }
}
