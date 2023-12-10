package yuriy.weiss.web.server.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yuriy.weiss.common.model.StartProcessingRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class RequestsRegistryImpl implements RequestsRegistry {

    private final Map<String, RegistryItem> registry = new HashMap<>();

    @Override
    public synchronized void putToCache( final StartProcessingRequest request ) {
        RegistryItem item = new RegistryItem( RequestProcessingState.ADDED, null );
        registry.put( request.getRequestId(), item );
    }

    @Override
    public void updateState( final String requestId, final RequestProcessingState state ) {
        RegistryItem item = registry.get( requestId );
        if ( item != null ) {
            item.setState( state );
        }
    }

    @Override
    public RegistryItem get( final String requestId ) {
        return registry.get( requestId );
    }

    @Override
    public synchronized void removeRepliedRequests() {
        log.info( "cleaning registry; registry size: {}", registry.size() );
        Set<String> keysToRemove = new HashSet<>();
        registry.forEach( ( key, value ) -> {
            if ( value.getState() == RequestProcessingState.RESPONSE_SENT ) {
                keysToRemove.add( key );
            }
        } );
        keysToRemove.forEach( registry::remove );
    }

    @Override
    public int getSize() {
        return registry.size();
    }
}
