package yuriy.weiss.web.server.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.Logger;
import yuriy.weiss.common.model.StartProcessingRequest;
import yuriy.weiss.common.utils.ThreadUtils;
import yuriy.weiss.web.server.registry.RegistryItem;
import yuriy.weiss.web.server.registry.RequestProcessingState;
import yuriy.weiss.web.server.registry.RequestsRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Step03MessageProcessor extends AbstractBehavior<Step03MessageProcessor.Command> {

    public interface Command {
    }

    public record ProcessMessage(StartProcessingRequest request) implements Command {
    }

    public static Behavior<Command> create( RequestsRegistry registry, int index ) {
        return Behaviors.setup( context -> new Step03MessageProcessor( context, registry, index ) );
    }

    private final Executor blockingDispatcher;
    private final RequestsRegistry registry;
    private final ActorRef<Step04RequestStateChanger.Command> nextStep;

    private Step03MessageProcessor( ActorContext<Command> context, RequestsRegistry registry, int index ) {
        super( context );
        this.blockingDispatcher = context
                .getSystem()
                .dispatchers()
                .lookup( DispatcherSelector.fromConfig( "processor-blocking-dispatcher" ) );
        this.registry = registry;
        this.nextStep = context.spawn( Step04RequestStateChanger.create( registry ),
                "step04RequestStateChanger_" + index );
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage( ProcessMessage.class, this::onProcessMessage )
                .build();
    }

    private Behavior<Command> onProcessMessage( ProcessMessage message ) {
        triggerFutureBlockingOperation( getContext().getLog(), getContext().getSelf().path().toString(),
                message, blockingDispatcher );
        return this;
    }

    private void triggerFutureBlockingOperation( Logger log, String path, ProcessMessage message,
            Executor blockingDispatcher ) {
        CompletableFuture
                .runAsync(
                        () -> {
                            String requestId = message.request().getRequestId();
                            RegistryItem registryItem = registry.get( requestId );
                            if ( registryItem != null ) {
                                registryItem.setConvertedMessage( message.request().getMessage() + "_converted" );
                            }
                            ThreadUtils.sleep( ThreadUtils.nextGaussian( 500 ) );
                            log.trace( "{} message processed: {}", path, requestId );
                            nextStep.tell( new Step04RequestStateChanger.ChangeRequestState( message.request(),
                                    RequestProcessingState.PROCESSED ) );
                        }, blockingDispatcher )
                .exceptionallyAsync(
                        e -> {
                            e.printStackTrace();
                            return null;
                        }, blockingDispatcher );
    }
}
