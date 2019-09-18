package com.example;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.example.account.routes.AccountRoutes;
import com.example.account.service.AccountRegistryActor;
import com.example.transcaction.routes.TransactionRoutes;
import com.example.transcaction.service.TransactionRegistryActor;

import static com.example.account.utils.AccountConfigs.SERVER_ADDRESS;
import static com.example.account.utils.AccountConfigs.SERVER_HOST;
import static com.example.account.utils.AccountConfigs.SERVER_PORT;

public class AppServer extends AllDirectives {
    
    final private LoggingAdapter logger;
    private final AccountRoutes accountRoutes;
    private final TransactionRoutes transactionRoutes;
    
    public AppServer(ActorSystem system,
                     ActorRef accountRegistryActor,
                     ActorRef transactionRegistryActor) {
        this.accountRoutes = new AccountRoutes(system, accountRegistryActor);
        this.transactionRoutes = new TransactionRoutes(system, transactionRegistryActor);
        logger = Logging.getLogger(system, this);
    }
    
    public Route createRoute() {
        return route(
                accountRoutes.routes(),
                transactionRoutes.routes()
        );
    }
    
    public static void main(String[] args) {
        // Setting up the Actor System
        ActorSystem system = ActorSystem.create("TransactionApiServer");
        
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        
        // Setting Up Actors and System
        ActorRef accountRegistryActor = system.actorOf(AccountRegistryActor.props(),
                "accountRegistryActor");
        
        ActorRef transactionRegistryActor = system.actorOf(TransactionRegistryActor.props(accountRegistryActor),
                "transactionRegistryActor");
        
        // Initializing the Application Server
        AppServer server = new AppServer(
                system,
                accountRegistryActor,
                transactionRegistryActor
        );
        
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = server
                .createRoute()
                .flow(system, materializer);
        
        http.bindAndHandle(routeFlow,
                ConnectHttp.toHost(SERVER_HOST, SERVER_PORT),
                materializer);
        
        server.logger.info("[ Server up at {} \n Press Ctrl-C / Ctrl-Z to Stop ]", SERVER_ADDRESS);
    }
}
