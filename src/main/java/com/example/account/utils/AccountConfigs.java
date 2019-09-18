package com.example.account.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AccountConfigs {
    
    private AccountConfigs() {
    
    }
    
    private static final Config CONF = ConfigFactory.load();
    public static final String SERVICE_VERSION = CONF.getString("service.version");
    //Actor Configs
    public static final Long AKKA_TIMEOUT_DURATION = CONF.getLong("actor.timeout.duration");
    public static final String SERVER_HOST = CONF.getString("http.host");
    public static final Integer SERVER_PORT = CONF.getInt("http.port");
    public static final String SERVER_ADDRESS = SERVER_HOST + ":" + SERVER_PORT;
    public static final String ACCOUNT_ACTOR_NAME = "account_";
    public static final Double MINIMUM_BALANCE = 0.0;
}
