package com.example.account.messages;

import com.example.account.models.Account;

import java.io.Serializable;

public interface AccountRegistryMessages {
    
    class CreateAccount implements Serializable {
        private final Account account;
        
        public CreateAccount(Account account) {
            this.account = account;
        }
        
        public Account getAccount() {
            return account;
        }
    }
    
    class GetAccount implements Serializable {
        private final String accountNumber;
        
        public GetAccount(String accountNumber) {
            this.accountNumber = accountNumber;
        }
        
        public String getAccountNumber() {
            return accountNumber;
        }
        
    }
    
    class Credit implements Serializable {
        private final Double amount;
        private final String accountNumber;
        
        public Credit(String accountNumber, Double amount) {
            this.accountNumber = accountNumber;
            this.amount = amount;
        }
        
        public Double getAmount() {
            return amount;
        }
        
        public String getAccountNumber() {
            return accountNumber;
        }
    }
    
    class Debit implements Serializable {
        private final Double amount;
        private final String accountNumber;
        
        public Debit(String accountNumber, Double amount) {
            this.accountNumber = accountNumber;
            this.amount = amount;
        }
        
        public Double getAmount() {
            return amount;
        }
        
        public String getAccountNumber() {
            return accountNumber;
        }
    }
    
    
    abstract class Response implements Serializable {
    }
    
    class SuccessResponse extends Response {
        private final String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    class FailureResponse extends Response {
        private final String message;
        
        public FailureResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
