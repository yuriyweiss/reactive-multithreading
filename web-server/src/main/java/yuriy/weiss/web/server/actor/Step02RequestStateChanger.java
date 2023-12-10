package yuriy.weiss.web.server.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.web.server.registry.RequestProcessingState;
import yuriy.weiss.web.server.registry.RequestsRegistry;

public class Step02RequestStateChanger extends AbstractBehavior<Step02RequestStateChanger.Command> {

    public interface Command {
    }

    public record ChangeRequestState(StartProcessingRequest request, RequestProcessingState state) implements Command {
    }

    public static Behavior<Command> create( RequestsRegistry registry, int index ) {
        return Behaviors.setup( context -> new Step02RequestStateChanger( context, registry, index ) );
    }

    private RequestsRegistry registry;
    private ActorRef<Step03MessageProcessor.Command> nextStep;

    private Step02RequestStateChanger( ActorContext<Command> context, RequestsRegistry registry, int index ) {
        super( context );
        this.registry = registry;
        this.nextStep = context.spawn( Step03MessageProcessor.create( registry, index ),
                "step03MessageProcessor_" + index );
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
        nextStep.tell( new Step03MessageProcessor.ProcessMessage( message.request() ) );
        return this;
    }
}
