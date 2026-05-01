package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.AuditLogDAO;
import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.AuditLog;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountService {
    private AccountDAO AccountDAO = new AccountDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    private Map<String, Account> cache = new HashMap<>();

    public Account openAccount(int userId, String type) throws BankException {

        List<Account> list =AccountDAO.findUserById(userId);

        if (list.size() >= 3)
            throw new BankException("Limit Reached");

        String accNo = "BNK" + System.currentTimeMillis();

        BigDecimal rate = switch (type){
            case "SAVINGS" ->  new BigDecimal(3.5);
            case "CURRENT" ->  BigDecimal.ZERO;
            case "FIXED_DEPOSIT" -> new BigDecimal("6.5");
            default -> throw new BankException("Invalid type");

        };

        Account acc = new Account(accNo,userId,type,rate);
        acc.setBalance(BigDecimal.ZERO);

        AccountDAO.createAccount(acc);

        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction("OPEN_ACCOUNT");
        log.setDetails("Opened " + type);
        auditLogDAO.log(log);

        return acc;

    }

    public List<Account> getAccounts(int userId) throws BankException{
        return AccountDAO.findUserById(userId);
    }

    public Account getByNumber(String accNo)throws BankException{
        if (cache.containsKey(accNo))
            return cache.get(accNo);

        Account acc = AccountDAO.findByAccountNumber(accNo);

        if (acc == null)
            throw new BankException("Not Found");

        cache.put(accNo,acc);
        return acc;
    }
}
