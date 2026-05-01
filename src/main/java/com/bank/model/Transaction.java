package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private int txnId;
    private int fromAccountId;  // 0 or null if it's a deposit (no source)
    private int toAccountId;    // 0 or null if it's a withdrawal (no destination)
    private String txnType;     //"DEPOSIT" "WITHDRAWAL" "TRANSFER"
    private BigDecimal amount;
    private BigDecimal balanceAfter;   //a snapshot afer the transaction
    private String description;
    private String referencoNo;       //unique tracking ID
    private LocalDateTime txnTimestamp;

    public Transaction(){}

    public int getTxnId() {
        return txnId;
    }

    public void setTxnId(int txnId) {

        this.txnId = txnId;
    }

    public int getFromAccountId() {

        return fromAccountId;
    }

    public void setFromAccountId(int fromAccountId) {

        this.fromAccountId = fromAccountId;
    }

    public int getToAccountId() {

        return toAccountId;
    }

    public void setToAccountId(int toAccountId) {

        this.toAccountId = toAccountId;
    }

    public String getTxnType() {

        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNo() {
        return referencoNo;
    }

    public void setReferenceNo(String referencoNo) {
        this.referencoNo = referencoNo;
    }

    public LocalDateTime getTxnTimestamp() {
        return txnTimestamp;
    }

    public void setTxnTimestamp(LocalDateTime txnTimestamp) {
        this.txnTimestamp = txnTimestamp;
    }
}
