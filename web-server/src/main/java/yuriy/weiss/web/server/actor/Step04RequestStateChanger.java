package yuriy.weiss.web.server.actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.web.server.registry.RequestProcessingState;
import yuriy.weiss.web.server.registry.RequestsRegistry;

public class Step04RequestStateChanger extends AbstractBehavior<Step04RequestStateChanger.Command> {

    public interface Command {
    }

    public record ChangeRequestState(StartProcessingRequest request, RequestProcessingState state) implements Command {
    }

    public static Behavior<Command> create( RequestsRegistry registry ) {
        return Behaviors.setup( context -> new Step04RequestStateChanger( context, registry ) );
    }

    private RequestsRegistry registry;

    private Step04RequestStateChanger( ActorContext<Command> context, RequestsRegistry registry ) {
        super( context );
        this.registry = registry;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage( ChangeRequestState.class, this::onChangeRequestState )
                .build();
    }

    private Behavior<Command> onChangeRequestState( ChangeRequestState message ) {
        String requestId = message.request().getRequestId();
        RequestProcessingState newState = message.state();
        registry.updateState( requestId, newState );
        getContext().getLog().trace( "{} state updated {}", getContext().getSelf().path(), requestId );
        return this;
    }
}
