package com.example.transcation;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.example.account.messages.AccountRegistryMessages;
import com.example.account.models.Account;
import com.example.account.service.AccountRegistryActor;
import com.example.transcaction.messages.TransactionRegistryMessages.FailureResponse;
import com.example.transcaction.messages.TransactionRegistryMessages.SuccessResponse;
import com.example.transcaction.messages.TransactionRegistryMessages.Transfer;
import com.example.transcaction.models.Transaction;
import com.example.transcaction.service.TransactionRegistryActor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransactionRegistryActorTest {
    
    private static ActorSystem system;
    
    private static final String TRANSACTION_NUMBER = "1";
    private static final String SOURCE_ACCOUNT_NUMBER = "11";
    private static final String TARGET_ACCOUNT_NUMBER = "22";
    private static final Double SUFFICIENT_AMOUNT = 2.0;
    private static final Double INSUFFICIENT_AMOUNT = 120.0;
    private static final String NAME_1 = "TEST-1";
    private static final Double BALANCE_1 = 10.0;
    private static final String NAME_2 = "TEST-2";
    private static final Double BALANCE_2 = 20.0;
    private final Transaction transaction = new Transaction(TRANSACTION_NUMBER, SOURCE_ACCOUNT_NUMBER,
            TARGET_ACCOUNT_NUMBER, INSUFFICIENT_AMOUNT);
    
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }
    
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system.terminate();
    }
    
    private static ActorRef getTestAccountRegistryActor() {
        Props props = AccountRegistryActor.props();
        return system.actorOf(props);
    }
    
    private static ActorRef getTestTransactionRegistryActorTest(ActorRef actorRef) {
        Props props = TransactionRegistryActor.props(actorRef);
        return system.actorOf(props);
    }
    
    @Test
    public void testTransferReturnsSuccessIfAccountsExistAndSufficientBalance() {
        new TestKit(system) {{
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new AccountRegistryMessages
                    .CreateAccount(new Account(SOURCE_ACCOUNT_NUMBER, NAME_1, BALANCE_1)), getRef());
            expectMsgClass(AccountRegistryMessages.SuccessResponse.class);
            
            accountRegistryActor.tell(new AccountRegistryMessages
                    .CreateAccount(new Account(TARGET_ACCOUNT_NUMBER, NAME_2, BALANCE_2)), getRef());
            expectMsgClass(AccountRegistryMessages.SuccessResponse.class);
            
            Transaction transaction = new Transaction(TRANSACTION_NUMBER, SOURCE_ACCOUNT_NUMBER,
                    TARGET_ACCOUNT_NUMBER, SUFFICIENT_AMOUNT);
            ActorRef transactionRegistryActor = getTestTransactionRegistryActorTest(accountRegistryActor);
            
            transactionRegistryActor.tell(new Transfer(transaction), getRef());
            expectMsgClass(SuccessResponse.class);
        }};
    }
    
    @Test
    public void testTransferReturnsSuccessIfAccountsExistButInSufficientBalance() {
        new TestKit(system) {{
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new AccountRegistryMessages
                    .CreateAccount(new Account(SOURCE_ACCOUNT_NUMBER, NAME_1, BALANCE_1)), getRef());
            expectMsgClass(AccountRegistryMessages.SuccessResponse.class);
            
            accountRegistryActor.tell(new AccountRegistryMessages
                    .CreateAccount(new Account(TARGET_ACCOUNT_NUMBER, NAME_2, BALANCE_2)), getRef());
            expectMsgClass(AccountRegistryMessages.SuccessResponse.class);
            
            Transaction transaction = new Transaction(TRANSACTION_NUMBER, SOURCE_ACCOUNT_NUMBER,
                    TARGET_ACCOUNT_NUMBER, INSUFFICIENT_AMOUNT);
            ActorRef transactionRegistryActor = getTestTransactionRegistryActorTest(accountRegistryActor);
            
            transactionRegistryActor.tell(new Transfer(transaction), getRef());
            expectMsgClass(FailureResponse.class);
        }};
    }
    
    @Test
    public void testTransferReturnsFailureIfSourceAccountsDoesNotExist() {
        Account target = new Account(TARGET_ACCOUNT_NUMBER, NAME_2, BALANCE_2);
        simulateOneAccountDoesNotExist(target);
    }
    
    @Test
    public void testTransferReturnsFailureIfTargetAccountsDoesNotExist() {
        Account source = new Account(SOURCE_ACCOUNT_NUMBER, NAME_1, BALANCE_1);
        simulateOneAccountDoesNotExist(source);
    }
    
    private void simulateOneAccountDoesNotExist(Account account) {
        new TestKit(system) {{
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new AccountRegistryMessages
                    .CreateAccount(account), getRef());
            expectMsgClass(AccountRegistryMessages.SuccessResponse.class);
            
            ActorRef transactionRegistryActor = getTestTransactionRegistryActorTest(accountRegistryActor);
            
            transactionRegistryActor.tell(new Transfer(transaction), getRef());
            expectMsgClass(FailureResponse.class);
        }};
    }
    
}
