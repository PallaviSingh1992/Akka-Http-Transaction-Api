package com.example.transcaction.models;

public class Transaction {
    
    private final String transactionId;
    private final String sourceAccountNumber;
    private final String targetAccountNumber;
    private final Double amount;
    
    public Transaction() {
        this.transactionId = "";
        this.sourceAccountNumber = "";
        this.targetAccountNumber = "";
        this.amount = 0.0;
    }
    
    public Transaction(String transactionId, String sourceAccountNumber,
                       String targetAccountNumber, Double amount) {
        this.transactionId = transactionId;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
    }
    
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }
    
    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }
    
    public Double getAmount() {
        return amount;
    }
    
}
