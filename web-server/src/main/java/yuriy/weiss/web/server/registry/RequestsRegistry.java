package yuriy.weiss.web.server.registry;

import yuriy.weiss.common.model.StartProcessingRequest;

public interface RequestsRegistry {

    void putToCache( final StartProcessingRequest request );

    void updateState( final String requestId, final RequestProcessingState state );

    RegistryItem get( final String requestId );

    void removeRepliedRequests();

    int getSize();
}
