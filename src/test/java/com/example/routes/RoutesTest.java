package com.example.routes;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import com.example.AppServer;
import com.example.account.service.AccountRegistryActor;
import com.example.transcaction.service.TransactionRegistryActor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RoutesTest extends JUnitRouteTest {
    
    private TestRoute route;
    private ActorSystem system;
    
    @Before
    public void setUp() {
        system = ActorSystem.create("TransactionTestApiServer");
        
        ActorRef accountRegistryActor = system.actorOf(AccountRegistryActor.props(),
                "testAccountRegistryActor");
        ActorRef transactionRegistryActor = system.actorOf(TransactionRegistryActor.props(accountRegistryActor),
                "testTransactionRegistryActor");
        AppServer server = new AppServer(system, accountRegistryActor, transactionRegistryActor);
        route = testRoute(server.createRoute());
    }
    
    @After
    public void tearDown() {
        system.terminate();
    }
    
    @Test
    public void testPostCreateAccountReturnsCreated() {
        route.run(HttpRequest.POST("/v1/accounts")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(), "{\n" +
                        "\t\"accountNumber\":\"123451\",\n" +
                        "\t\"name\":\"Pallavi Singh\",\n" +
                        "\t\"balance\":10\n" +
                        "}"))
                .assertStatusCode(StatusCodes.CREATED);
    }
    
    @Test
    public void testPostCreateAccountReturnsInternalServerError() {
        route.run(HttpRequest.POST("/v1/accounts")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(), "{\n" +
                        "\t\"balance\":10\n" +
                        "}"))
                .assertStatusCode(StatusCodes.CREATED);
    }
    
    @Test
    public void testPostCreateAccountReturnsConflicted() {
        route.run(HttpRequest.POST("/v1/accounts")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(), "{\n" +
                        "\t\"accountNumber\":\"123451\",\n" +
                        "\t\"name\":\"Pallavi Singh\",\n" +
                        "\t\"balance\":10\n" +
                        "}"))
                .assertStatusCode(StatusCodes.CONFLICT);
    }
    
    @Test
    public void testGetExistingAccountReturnsOK() {
        route.run(HttpRequest.GET("/v1/accounts/123451"))
                .assertStatusCode(StatusCodes.OK);
    }
    
    @Test
    public void testGetNonExistingAccountReturnsNotFound() {
        route.run(HttpRequest.GET("/v1/accounts/123450"))
                .assertStatusCode(StatusCodes.NOT_FOUND);
    }
    
    @Test
    public void testGetExistingAccountReturnsInternalServerError() {
        route.run(HttpRequest.GET("/v1/accounts/abc"))
                .assertStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
    }
    
    @Test
    public void testPostCreateTransactionReturnsCreated() {
        route.run(HttpRequest.POST("/v1/accounts")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(), "{\n" +
                        "\t\"accountNumber\":\"123452\",\n" +
                        "\t\"name\":\"Seema Singh\",\n" +
                        "\t\"balance\":10\n" +
                        "}"))
                .assertStatusCode(StatusCodes.CREATED);
        
        route.run(HttpRequest.POST("/v1/transactions")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(), "{\n" +
                        "\t\"transactionId\":\"1\",\n" +
                        "    \"sourceAccountNumber\":\"123451\",\n" +
                        "    \"targetAccountNumber\":\"123452\",\n" +
                        "    \"amount\":3\n" +
                        "}"))
                .assertStatusCode(StatusCodes.CREATED);
    }
    
    @Test
    public void testPostCreateTransactionReturnsFordbidden() {
        route.run(HttpRequest.POST("/v1/transactions")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(), "{\n" +
                        "\t\"transactionId\":\"1\",\n" +
                        "    \"sourceAccountNumber\":\"123451\",\n" +
                        "    \"targetAccountNumber\":\"123453\",\n" +
                        "    \"amount\":3\n" +
                        "}"))
                .assertStatusCode(StatusCodes.FORBIDDEN);
    }
    
    @Test
    public void testPostCreateTransactionReturnsInternalServerError() {
        route.run(HttpRequest.POST("/v1/transactions")
                .withEntity(MediaTypes.APPLICATION_JSON.toContentType(), "{\n" +
                        "\t\"transactionId\":\"1\",\n" +
                        "    \"sourceAccountNumber\":\"123451\",\n" +
                        "    \"amount\":3\n" +
                        "}"))
                .assertStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
    }
}
