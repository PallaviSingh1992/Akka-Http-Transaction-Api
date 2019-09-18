package com.example.transcaction.service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import com.example.account.messages.AccountRegistryMessages;
import com.example.account.messages.AccountRegistryMessages.Credit;
import com.example.account.messages.AccountRegistryMessages.Debit;
import com.example.account.messages.AccountRegistryMessages.GetAccount;
import com.example.transcaction.messages.TransactionRegistryMessages;
import com.example.transcaction.messages.TransactionRegistryMessages.Transfer;
import com.example.transcaction.models.Transaction;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.account.utils.AccountConfigs.AKKA_TIMEOUT_DURATION;

public class TransactionRegistryActor extends AbstractActor {
    
    private LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);
    private final String loggingPrefix = "[ TRANSACTION ] : ";
    private static ActorRef accountActorRef;
    private Duration timeout = Duration.ofSeconds(AKKA_TIMEOUT_DURATION);
    
    private final Map<String, Transaction> transcationsById = new HashMap<>();
    
    public static Props props(ActorRef accountService) {
        accountActorRef = accountService;
        return Props.create(TransactionRegistryActor.class);
    }
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Transfer.class, this::processTransfer)
                .build();
    }
    
    private void processTransfer(Transfer transfer) {
        ActorRef replyTo = sender();
        Transaction txn = transfer.getTransaction();
        
        if (checkIfTransactionExists(txn.getTransactionId())) {
            
            Patterns.ask(accountActorRef, new GetAccount(txn.getSourceAccountNumber()), timeout)
                    .thenApply(Optional.class::cast)
                    .thenAcceptBoth(Patterns.ask(accountActorRef, new GetAccount(txn.getTargetAccountNumber()), timeout)
                            .thenApply(Optional.class::cast), (srcAccount, trgAccount) -> {
                        if (srcAccount.isPresent() && trgAccount.isPresent()) {
                            initiateTransfer(txn, replyTo);
                        } else if (!srcAccount.isPresent()) {
                            sendFailureResponse(replyTo, String.format("%s Source Account %s does not exits",
                                    loggingPrefix, txn.getSourceAccountNumber()));
                        } else {
                            sendFailureResponse(replyTo, String.format("%s Target Account %s does not exits",
                                    loggingPrefix, txn.getTargetAccountNumber()));
                        }
                    });
        } else {
            sendFailureResponse(replyTo, String.format("%s Transaction Id %s is already committed/rolledBack",
                    loggingPrefix, txn.getTransactionId()));
        }
    }
    
    private boolean checkIfTransactionExists(String txnId) {
        return !transcationsById.containsKey(txnId);
    }
    
    private void initiateTransfer(Transaction txn, ActorRef replyTo) {
        Patterns.ask(accountActorRef, new Debit(txn.getSourceAccountNumber(), txn.getAmount()), timeout)
                .thenAcceptBoth(Patterns.ask(accountActorRef, new Credit(txn.getTargetAccountNumber(), txn.getAmount()), timeout),
                        (debitTxn, creditTxn) -> {
                            if (debitTxn instanceof AccountRegistryMessages.SuccessResponse
                                    && creditTxn instanceof AccountRegistryMessages.SuccessResponse) {
                                transcationsById.put(txn.getTransactionId(), txn);
                                
                                sendSuccessResponse(replyTo, String.format("%s Amount %s successfully credited to %s from %s",
                                        loggingPrefix, txn.getAmount(), txn.getTargetAccountNumber(),
                                        txn.getSourceAccountNumber()));
                            } else {
                                handleFailure(debitTxn, creditTxn, txn, replyTo);
                            }
                        });
    }
    
    private void handleFailure(Object debitTxn, Object creditTxn, Transaction txn, ActorRef replyTo) {
        if (debitTxn instanceof AccountRegistryMessages.FailureResponse
                && creditTxn instanceof AccountRegistryMessages.SuccessResponse) {
            revertTargetAccountBalance(txn);
        }
        if (debitTxn instanceof AccountRegistryMessages.SuccessResponse
                && creditTxn instanceof AccountRegistryMessages.FailureResponse) {
            revertSourceAccountBalance(txn);
        }
        
        sendFailureResponse(replyTo, String.format("%s Transaction %s Failed",
                loggingPrefix, txn.getTransactionId()));
    }
    
    private void revertSourceAccountBalance(Transaction txn) {
        Patterns.ask(accountActorRef, new Debit(txn.getTargetAccountNumber(), txn.getAmount()),
                timeout);
    }
    
    private void revertTargetAccountBalance(Transaction txn) {
        Patterns.ask(accountActorRef, new Credit(txn.getSourceAccountNumber(), txn.getAmount()),
                timeout);
    }
    
    private void sendFailureResponse(ActorRef ref, String message) {
        logger.error(message);
        ref.tell(new TransactionRegistryMessages.FailureResponse(message), getSelf());
    }
    
    private void sendSuccessResponse(ActorRef ref, String message) {
        logger.info(message);
        ref.tell(new TransactionRegistryMessages.SuccessResponse(message), getSelf());
    }
    
}
