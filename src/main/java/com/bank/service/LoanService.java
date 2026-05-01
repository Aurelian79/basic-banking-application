package com.bank.service;

import com.bank.dao.AuditLogDAO;
import com.bank.dao.LoanDAO;
import com.bank.exception.BankException;
import com.bank.model.Loan;

import java.math.BigDecimal;
import java.util.List;

public class LoanService {
        private LoanDAO loanDAO = new LoanDAO();
        private AuditLogDAO auditLogDAO = new AuditLogDAO();

        public Loan apply(int userId , int accId, BigDecimal amount,int months) throws BankException {

            if (amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new BankException("Invalid amount");

            Loan loan = new Loan();
            loan.setUserId(userId);
            loan.setAccountId(accId);
            loan.setLoanAmount(amount);
            loan.setTenureMonths(months);

            loanDAO.apply(loan);
            return loan;
        }

        public List<Loan> getLoans(int userId) throws BankException {
            return loanDAO.findByUserId(userId);
        }

    }

