package com.example.transcaction.messages;

import com.example.transcaction.models.Transaction;

import java.io.Serializable;

public interface TransactionRegistryMessages {
    
    class Transfer implements Serializable {
        private final Transaction transaction;
        
        public Transfer(Transaction transaction) {
            this.transaction = transaction;
        }
        
        public Transaction getTransaction() {
            return transaction;
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
