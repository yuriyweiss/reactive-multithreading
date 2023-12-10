package yuriy.weiss.web.server.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.web.server.registry.RequestProcessingState;
import yuriy.weiss.web.server.registry.RequestsRegistry;

public class Step01MainDispatcher extends AbstractBehavior<Step01MainDispatcher.Command> {

    public interface Command {
    }

    public record PopulateActors() implements Command {
    }

    public record StartMessageProcessing(StartProcessingRequest request) implements Command {
    }

    private final int capacity;
    private final RequestsRegistry registry;
    private final List<ActorRef<Step02RequestStateChanger.Command>> stateChangers = new ArrayList<>();

    private AtomicInteger roundRobinIndex = new AtomicInteger( 0 );

    public static Behavior<Command> create( int capacity, RequestsRegistry registry ) {
        return Behaviors.setup( context -> new Step01MainDispatcher( context, capacity, registry ) );
    }

    private Step01MainDispatcher( ActorContext<Command> context, int capacity, RequestsRegistry registry ) {
        super( context );
        this.capacity = capacity;
        this.registry = registry;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage( PopulateActors.class, this::onPopulateActors )
                .onMessage( StartMessageProcessing.class, this::onStartMessageProcessing )
                .build();
    }

    private Behavior<Command> onPopulateActors( PopulateActors command ) {
        for ( int i = 0; i < capacity; i++ ) {
            ActorRef<Step02RequestStateChanger.Command> stateChanger =
                    getContext().spawn( Step02RequestStateChanger.create( registry, i ),
                            "step02RequestStateChanger_" + i );
            stateChangers.add( stateChanger );
        }
        return this;
    }

    private Behavior<Command> onStartMessageProcessing( final StartMessageProcessing command ) {
        int nextIndex = getNextIndex();
        getContext().getLog().trace( "{} next index: {}", getContext().getSelf().path(), nextIndex );
        stateChangers.get( nextIndex )
                .tell( new Step02RequestStateChanger.ChangeRequestState( command.request(),
                        RequestProcessingState.IN_PROGRESS ) );
        return this;
    }

    private int getNextIndex() {
        int result = roundRobinIndex.getAndIncrement();
        if ( result == capacity - 1 ) {
            roundRobinIndex.set( 0 );
        } else if ( result > capacity - 1 ) {
            result = capacity - 1;
            roundRobinIndex.set( 0 );
        }
        return result;
    }
}
