package com.example.account.routes;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import com.example.account.messages.AccountRegistryMessages.CreateAccount;
import com.example.account.messages.AccountRegistryMessages.GetAccount;
import com.example.account.messages.AccountRegistryMessages.Response;
import com.example.account.messages.AccountRegistryMessages.SuccessResponse;
import com.example.account.models.Account;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static com.example.account.utils.AccountConfigs.AKKA_TIMEOUT_DURATION;
import static com.example.account.utils.AccountConfigs.SERVICE_VERSION;

public class AccountRoutes extends AllDirectives {
    
    final private ActorRef accountRegistryActor;
    final private LoggingAdapter logger;
    private Duration timeout = Duration.ofSeconds(AKKA_TIMEOUT_DURATION);
    
    public AccountRoutes(ActorSystem system, ActorRef accountRegistryActor) {
        this.accountRegistryActor = accountRegistryActor;
        logger = Logging.getLogger(system, this);
    }
    
    public Route routes() {
        return pathPrefix(SERVICE_VERSION, () -> route(
                pathPrefix("accounts", () -> route(
                        postAccount(),
                        path(PathMatchers.segment(),
                                accountNumber -> route(
                                        getAccount(accountNumber))
                        ))
                )));
    }
    
    private Route postAccount() {
        return pathEnd(() ->
                post(() ->
                        entity(Jackson.unmarshaller(Account.class), account -> {
                            logger.info(String.format("[ Request ] : Create New Account with details {id : %s, name: %s, balance : %s}",
                                    account.getAccountNumber(), account.getName(), account.getBalance()));
                            
                            CompletionStage<Response> accountCreated = Patterns.ask(accountRegistryActor,
                                    new CreateAccount(account), timeout)
                                    .thenApply(Response.class::cast);
                            
                            return onSuccess(() -> accountCreated, response -> {
                                if (response instanceof SuccessResponse) {
                                    logger.info(String.format("[ Response ] : New Account created with details {id : %s}",
                                            account.getAccountNumber()));
                                    return complete(StatusCodes.CREATED, response, Jackson.marshaller());
                                } else {
                                    logger.info(String.format("[ Response ] : Conflicting Account with {id : %s} already exists",
                                            account.getAccountNumber()));
                                    return complete(StatusCodes.CONFLICT, response, Jackson.marshaller());
                                }
                            }).orElse(complete(StatusCodes.INTERNAL_SERVER_ERROR));
                        })
                ));
    }
    
    private Route getAccount(String accountNumber) {
        return get(() -> {
            logger.info(String.format("[ Request ] : Get Account details for {id : %s }", accountNumber));
            CompletionStage<Optional<Account>> optionalAccount = Patterns.ask(accountRegistryActor,
                    new GetAccount(accountNumber), timeout)
                    .thenApply(Optional.class::cast);
            return onSuccess(() -> optionalAccount,
                    account -> {
                        if (account.isPresent()) {
                            logger.info(String.format("[ Response ] : Rendering Account details for {id : %s }", accountNumber));
                            return complete(StatusCodes.OK, account.get(), Jackson.marshaller());
                        } else {
                            logger.info(String.format("[ Response ] : Account {id : %s } not found", accountNumber));
                            return complete(StatusCodes.NOT_FOUND);
                        }
                    }).orElse(complete(StatusCodes.INTERNAL_SERVER_ERROR));
        });
    }
    
}
