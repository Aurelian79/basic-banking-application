package com.bank.service;

import com.bank.config.DBConnection;
import com.bank.dao.AccountDAO;
import com.bank.dao.AuditLogDAO;
import com.bank.dao.TransactionDAO;
import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class TransactionService {
    private AccountDAO accountDAO = new AccountDAO();
    private TransactionDAO txnDAO = new TransactionDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();
    private DBConnection getDbConnection() {
        return DBConnection.getInstance();
    }

    public void deposit(int accId, BigDecimal amount) throws BankException{

        if(amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BankException("Invalid amount");

        Connection conn = null;

        try {
            conn = getDbConnection().getConnection();
            conn.setAutoCommit(false);

            Account account = accountDAO.findById(accId);
            if (account == null) {
                throw new BankException("Account not found");
            }

            BigDecimal newBalance = account.getBalance().add(amount);

            accountDAO.updateBalance(accId, newBalance, conn);

            Transaction txn = new Transaction();
            txn.setTxnType("DEPOSIT");
            txn.setToAccountId(accId);
            txn.setAmount(amount);
            txn.setBalanceAfter(newBalance);
            txn.setDescription("Cash deposit");
            txn.setReferenceNo(generateReference());

            txnDAO.insert(txn, conn);

            conn.commit();
        } catch (Exception e) {
            try {conn.rollback();} catch (Exception ex) {}
                throw new BankException("Deposit Failed",e);
        }finally {
            try { conn.setAutoCommit(true); } catch (Exception ignored) {}
            getDbConnection().releaseConnection(conn);
        }

    }

    public void withdraw(int accId, BigDecimal amount) throws BankException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Invalid amount");
        }

        Connection conn = null;
        try {
            conn = getDbConnection().getConnection();
            conn.setAutoCommit(false);

            Account account = accountDAO.findById(accId);
            if (account == null) {
                throw new BankException("Account not found");
            }
            if (account.getBalance().compareTo(amount) < 0) {
                throw new BankException("Insufficient balance");
            }

            BigDecimal newBalance = account.getBalance().subtract(amount);
            accountDAO.updateBalance(accId, newBalance, conn);

            Transaction txn = new Transaction();
            txn.setTxnType("WITHDRAWAL");
            txn.setFromAccountId(accId);
            txn.setAmount(amount);
            txn.setBalanceAfter(newBalance);
            txn.setDescription("Cash withdrawal");
            txn.setReferenceNo(generateReference());

            txnDAO.insert(txn, conn);
            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            throw new BankException("Withdrawal failed", e);
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            getDbConnection().releaseConnection(conn);
        }
    }

    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) throws BankException {
        if (fromAccountNumber == null || toAccountNumber == null || fromAccountNumber.isBlank() || toAccountNumber.isBlank()) {
            throw new BankException("Account numbers are required");
        }
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new BankException("Source and destination accounts must be different");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Invalid amount");
        }

        Connection conn = null;
        try {
            conn = getDbConnection().getConnection();
            conn.setAutoCommit(false);

            Account from = accountDAO.findByAccountNumber(fromAccountNumber);
            Account to = accountDAO.findByAccountNumber(toAccountNumber);

            if (from == null || to == null) {
                throw new BankException("Account not found");
            }
            if (from.getBalance().compareTo(amount) < 0) {
                throw new BankException("Insufficient balance");
            }

            BigDecimal fromBalance = from.getBalance().subtract(amount);
            BigDecimal toBalance = to.getBalance().add(amount);

            accountDAO.updateBalance(from.getAccountId(), fromBalance, conn);
            accountDAO.updateBalance(to.getAccountId(), toBalance, conn);

            Transaction txn = new Transaction();
            txn.setTxnType("TRANSFER");
            txn.setFromAccountId(from.getAccountId());
            txn.setToAccountId(to.getAccountId());
            txn.setAmount(amount);
            txn.setBalanceAfter(fromBalance);
            txn.setDescription(description);
            txn.setReferenceNo(generateReference());

            txnDAO.insert(txn, conn);
            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            throw new BankException("Transfer failed", e);
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (Exception ignored) {}
            getDbConnection().releaseConnection(conn);
        }
    }

    public List<Transaction> getTransactions(int accountId, LocalDate from, LocalDate to) throws BankException {
        try {
            return txnDAO.findByAccountId(accountId, from, to, null);
        } catch (Exception e) {
            throw new BankException("Failed to fetch transactions", e);
        }
    }

    private String generateReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


}
