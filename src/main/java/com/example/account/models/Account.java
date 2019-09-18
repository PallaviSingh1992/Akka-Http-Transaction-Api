package com.example.account.models;

public class Account {
    
    private final String accountNumber;
    private final String name;
    private final Double balance;
    
    public Account() {
        this.accountNumber = "";
        this.name = "";
        this.balance = 0.0;
    }
    
    public Account(String accountNumber, String name, Double balance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.balance = balance;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public Double getBalance() {
        return balance;
    }
    
}
