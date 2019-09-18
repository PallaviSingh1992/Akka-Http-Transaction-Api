package com.example.account;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.example.account.messages.AccountRegistryMessages.CreateAccount;
import com.example.account.messages.AccountRegistryMessages.Credit;
import com.example.account.messages.AccountRegistryMessages.Debit;
import com.example.account.messages.AccountRegistryMessages.FailureResponse;
import com.example.account.messages.AccountRegistryMessages.GetAccount;
import com.example.account.messages.AccountRegistryMessages.SuccessResponse;
import com.example.account.models.Account;
import com.example.account.service.AccountRegistryActor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

public class AccountRegistryActorTest {
    private static ActorSystem system;
    
    private static final String ACCOUNT_NUMBER = "1";
    private static final String NAME = "TEST";
    private static final Double BALANCE = 10.0;
    private static final Double AMOUNT = 2.0;
    private static final Double DEBIT = 20.0;
    
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
    
    @Test
    public void testCreateAccountReturnsSuccessResponseIfAccountDoesNotExist() {
        new TestKit(system) {{
            Account account = new Account(ACCOUNT_NUMBER, NAME, BALANCE);
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new CreateAccount(account), getRef());
            
            expectMsgClass(SuccessResponse.class);
        }};
    }
    
    @Test
    public void testCreateAccountReturnsFailureResponseIfAccountDoesExists() {
        new TestKit(system) {{
            Account account = new Account(ACCOUNT_NUMBER, NAME, BALANCE);
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new CreateAccount(account), getRef());
            expectMsgClass(SuccessResponse.class);
            
            accountRegistryActor.tell(new CreateAccount(account), getRef());
            expectMsgClass(FailureResponse.class);
        }};
    }
    
    @Test
    public void testGetAccountReturnsExistingAccount() {
        new TestKit(system) {{
            Account account = new Account(ACCOUNT_NUMBER, NAME, BALANCE);
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new CreateAccount(account), getRef());
            expectMsgClass(SuccessResponse.class);
            
            accountRegistryActor.tell(new GetAccount(ACCOUNT_NUMBER), getRef());
            expectMsg(Optional.of(account));
        }};
    }
    
    @Test
    public void testGetAccountReturnsFailureIfAccountNotExisting() {
        new TestKit(system) {{
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new GetAccount(ACCOUNT_NUMBER), getRef());
            expectMsg(Optional.empty());
        }};
    }
    
    @Test
    public void testCreditReturnsSuccessResponseIfAccountExists() {
        new TestKit(system) {{
            Account account = new Account(ACCOUNT_NUMBER, NAME, BALANCE);
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new CreateAccount(account), getRef());
            expectMsgClass(SuccessResponse.class);
            
            accountRegistryActor.tell(new Credit(ACCOUNT_NUMBER, AMOUNT), getRef());
            expectMsgClass(SuccessResponse.class);
        }};
    }
    
    @Test
    public void testCreditReturnsFailureIfAccountDoesNotExists() {
        new TestKit(system) {{
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new Credit(ACCOUNT_NUMBER, AMOUNT), getRef());
            expectMsgClass(FailureResponse.class);
        }};
    }
    
    @Test
    public void testDebitReturnsSuccessResponseIfAccountExistsWithMinimumBalance() {
        new TestKit(system) {{
            Account account = new Account(ACCOUNT_NUMBER, NAME, BALANCE);
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new CreateAccount(account), getRef());
            expectMsgClass(SuccessResponse.class);
            
            accountRegistryActor.tell(new Debit(ACCOUNT_NUMBER, AMOUNT), getRef());
            expectMsgClass(SuccessResponse.class);
        }};
    }
    
    @Test
    public void testDebitReturnsFailureResponseIfAccountExistsWithoutMinimumBalance() {
        new TestKit(system) {{
            Account account = new Account(ACCOUNT_NUMBER, NAME, BALANCE);
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new CreateAccount(account), getRef());
            expectMsgClass(SuccessResponse.class);
            
            accountRegistryActor.tell(new Debit(ACCOUNT_NUMBER, DEBIT), getRef());
            expectMsgClass(FailureResponse.class);
        }};
    }
    
    @Test
    public void testDebitReturnsFailureResponseIfAccountDoesNotExists() {
        new TestKit(system) {{
            ActorRef accountRegistryActor = getTestAccountRegistryActor();
            
            accountRegistryActor.tell(new Debit(ACCOUNT_NUMBER, AMOUNT), getRef());
            expectMsgClass(FailureResponse.class);
        }};
    }
    
}
