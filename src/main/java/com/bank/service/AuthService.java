package com.bank.service;

import com.bank.dao.AuditLogDAO;
import com.bank.dao.UserDAO;
import com.bank.exception.BankException;
import com.bank.model.AuditLog;
import com.bank.model.User;
import com.bank.util.PasswordUtil;

public class AuthService {
    private UserDAO userDAO = new UserDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    public User register(String name,String email,String phone,String password) throws BankException {
        if (name == null || email == null || password == null)
            throw new BankException("Invalid Input");

        if(userDAO.findByEmail(email) != null)
            throw new BankException("Email already exists");
        String hashed = PasswordUtil.hash(password);

        User user = new User(name,email,phone,hashed,"CUSTOMER");
        int userId = userDAO.createUser(user);
        user.setUserId(userId);

        AuditLog log = new AuditLog();
        log.setUserId(user.getUserId());
        log.setAction("Register");
        log.setDetails("user Registered");
        auditLogDAO.log(log);

        user.setPasswordHash(null);
        return user;


    }

    public User login(String email,String password)throws BankException{
        User user = userDAO.findByEmail(email);

        if(user == null)
            throw new BankException("Invalid credentials");

        String hashed = PasswordUtil.hash(password);

        if(!hashed.equals(user.getPasswordHash()))
            throw new BankException("Invalid credentials");

        if(!user.isActive())
            throw new BankException("Account Inactive");

        AuditLog log = new AuditLog();
        log.setUserId(user.getUserId());
        log.setAction("LOGIN");
        log.setDetails("User logged in");
        auditLogDAO.log(log);

        user.setPasswordHash(null);
        return user;



    }

    public void logout(int userId) throws BankException {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction("LOGOUT");
        log.setDetails("User logged out");
        auditLogDAO.log(log);
    }
}
