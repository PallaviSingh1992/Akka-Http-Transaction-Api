package com.example.account.service;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.example.account.messages.AccountRegistryMessages.CreateAccount;
import com.example.account.messages.AccountRegistryMessages.Credit;
import com.example.account.messages.AccountRegistryMessages.Debit;
import com.example.account.messages.AccountRegistryMessages.FailureResponse;
import com.example.account.messages.AccountRegistryMessages.GetAccount;
import com.example.account.messages.AccountRegistryMessages.SuccessResponse;
import com.example.account.models.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.example.account.service.AccountRegistryActor.TransactionType.CREDIT;
import static com.example.account.service.AccountRegistryActor.TransactionType.DEBIT;
import static com.example.account.utils.AccountConfigs.MINIMUM_BALANCE;

public class AccountRegistryActor extends AbstractActor {
    
    private LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);
    private final String loggingPrefix = "[ ACCOUNT ] : ";
    
    enum TransactionType {
        CREDIT, DEBIT, QUERY;
    }
    
    private final Map<String, Account> accountsById = new HashMap<>();
    
    public static Props props() {
        return Props.create(AccountRegistryActor.class);
    }
    
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateAccount.class, this::processCreateAccount)
                .match(GetAccount.class, this::getAccountDetails)
                .match(Credit.class, this::credit)
                .match(Debit.class, this::debit)
                .build();
    }
    
    private void processCreateAccount(CreateAccount account) {
        String newAccountNumber = account.getAccount().getAccountNumber();
        if (checkIfAccountExists(newAccountNumber)) {
            sendFailureResponse(String.format("%s Account %s already exits.",
                    loggingPrefix, account.getAccount().getAccountNumber()));
        }
        createAccount(account);
    }
    
    private void getAccountDetails(GetAccount getAccount) {
        getSender().tell(Optional.ofNullable(getAccount(getAccount.getAccountNumber())), getSelf());
    }
    
    private void credit(Credit credit) {
        if (checkIfAccountExists(credit.getAccountNumber())) {
            creditBalance(credit);
        } else {
            sendFailureResponse(String.format("%s %s failed. Account with %s does not Exist.",
                    loggingPrefix, CREDIT, credit.getAccountNumber()));
        }
    }
    
    private void debit(Debit debit) {
        if (checkIfAccountExists(debit.getAccountNumber())) {
            if (checkCreditAvailability(debit)) {
                debitBalance(debit);
            } else {
                sendFailureResponse(String.format("%s %s failed. Account Id %s does not have sufficient balance.",
                        loggingPrefix, DEBIT, debit.getAccountNumber()));
            }
        } else {
            sendFailureResponse(String.format("%s %s failed. Account Id %s does not Exist.",
                    loggingPrefix, DEBIT, debit.getAccountNumber()));
        }
    }
    
    private void createAccount(CreateAccount createAccount) {
        addAccount(createAccount.getAccount());
        
        sendSuccessResponse(String.format("%s Account %s created.", loggingPrefix,
                createAccount.getAccount().getAccountNumber()));
    }
    
    private boolean checkIfAccountExists(String accountNumber) {
        return accountsById.containsKey(accountNumber);
    }
    
    private Account getAccount(String accountNumber) {
        return accountsById.get(accountNumber);
    }
    
    private void addAccount(Account account) {
        accountsById.put(account.getAccountNumber(), account);
    }
    
    private void updateAccount(Account account) {
        accountsById.put(account.getAccountNumber(), account);
    }
    
    private Double getAccountBalance(String accountNumber) {
        return getAccount(accountNumber).getBalance();
    }
    
    private void creditBalance(Credit credit) {
        Double newBalance = credit.getAmount() + getAccountBalance(credit.getAccountNumber());
        updateAccount(new Account(credit.getAccountNumber(),
                getAccount(credit.getAccountNumber()).getName(),
                newBalance));
        
        sendSuccessResponse(String.format("%s %s for amount %s succeeded for Account %s ",
                loggingPrefix, CREDIT, credit.getAmount(), credit.getAccountNumber()));
    }
    
    private void debitBalance(Debit debit) {
        Double newBalance = getAccountBalance(debit.getAccountNumber()) - debit.getAmount();
        updateAccount(new Account(debit.getAccountNumber(),
                getAccount(debit.getAccountNumber()).getName(),
                newBalance));
        
        sendSuccessResponse(String.format("%s %s for amount %s succeeded for Account %s ",
                loggingPrefix, DEBIT, debit.getAmount(), debit.getAccountNumber()));
    }
    
    private boolean checkCreditAvailability(Debit debit) {
        return getAccountBalance(debit.getAccountNumber()) - debit.getAmount() > MINIMUM_BALANCE;
    }
    
    private void sendSuccessResponse(String message) {
        logger.info(message);
        getSender().tell(new SuccessResponse(message), getSelf());
    }
    
    private void sendFailureResponse(String message) {
        logger.error(message);
        getSender().tell(new FailureResponse(message), getSelf());
    }
    
}
