package com.example.account.models;

import java.util.ArrayList;
import java.util.List;

public class Accounts {
    private final List<Account> listOfAccounts;
    
    public Accounts() {
        this.listOfAccounts = new ArrayList<>();
    }
    
    public Accounts(List<Account> listOfAccounts) {
        this.listOfAccounts = listOfAccounts;
    }
}
