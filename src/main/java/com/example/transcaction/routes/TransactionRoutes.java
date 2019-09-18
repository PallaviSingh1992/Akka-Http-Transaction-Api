package com.example.transcaction.routes;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import com.example.transcaction.messages.TransactionRegistryMessages.Response;
import com.example.transcaction.messages.TransactionRegistryMessages.SuccessResponse;
import com.example.transcaction.messages.TransactionRegistryMessages.Transfer;
import com.example.transcaction.models.Transaction;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static com.example.account.utils.AccountConfigs.AKKA_TIMEOUT_DURATION;
import static com.example.account.utils.AccountConfigs.SERVICE_VERSION;

public class TransactionRoutes extends AllDirectives {
    
    final private ActorRef transactionRegistryActor;
    final private LoggingAdapter logger;
    private Duration timeout = Duration.ofSeconds(AKKA_TIMEOUT_DURATION);
    
    public TransactionRoutes(ActorSystem system, ActorRef transactionRegistryActor) {
        this.transactionRegistryActor = transactionRegistryActor;
        logger = Logging.getLogger(system, this);
    }
    
    public Route routes() {
        return pathPrefix(SERVICE_VERSION, () -> route(
                pathPrefix("transactions", () -> route(
                        postTransaction()
                ))
        ));
    }
    
    private Route postTransaction() {
        return pathEnd(() ->
                post(() -> entity(Jackson.unmarshaller(Transaction.class), transaction -> {
                            logger.info(String.format("[Request] Transaction requested {SRC : %s | TRG : %s | AMOUNT :%s}",
                                    transaction.getSourceAccountNumber(), transaction.getTargetAccountNumber(), transaction.getAmount()));
                            CompletionStage<Response> transactionStatus = Patterns.ask(transactionRegistryActor,
                                    new Transfer(transaction), timeout)
                                    .thenApply(Response.class::cast);
                            
                            return onSuccess(() -> transactionStatus, response -> {
                                if (response instanceof SuccessResponse) {
                                    logger.info(String.format("[Request] Transaction Successful {id : %s}",
                                            transaction.getTransactionId()));
                                    return complete(StatusCodes.CREATED, response, Jackson.marshaller());
                                } else {
                                    logger.info(String.format("[Request] Transaction Failed {id : %s}",
                                            transaction.getTransactionId()));
                                    return complete(StatusCodes.FORBIDDEN, response, Jackson.marshaller());
                                }
                            }).orElse(complete(StatusCodes.INTERNAL_SERVER_ERROR));
                            
                        })
                ));
    }
    
}
