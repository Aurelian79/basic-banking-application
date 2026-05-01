package com.bank.service;

import com.bank.dao.AuditLogDAO;
import com.bank.dao.AccountDAO;
import com.bank.dao.LoanDAO;
import com.bank.exception.BankException;
import com.bank.model.Account;
import com.bank.model.Loan;
import com.bank.model.User;
import com.bank.util.FileLogger;

import java.math.BigDecimal;
import java.util.List;

public class LoanService {
        private LoanDAO loanDAO = new LoanDAO();
        private AuditLogDAO auditLogDAO = new AuditLogDAO();
        private AccountDAO accountDAO = new AccountDAO();
        private FileLogger fileLogger = FileLogger.getInstance();

        public Loan apply(User currentUser, int accId, BigDecimal amount,int months) throws BankException {

            if (amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new BankException("Invalid amount");

            Account account = accountDAO.findById(accId);
            if (account == null) {
                throw new BankException("Account not found");
            }
            validateOwnership(currentUser, account);

            Loan loan = new Loan();
            loan.setUserId(currentUser.getUserId());
            loan.setAccountId(accId);
            loan.setLoanAmount(amount);
            loan.setTenureMonths(months);

            loanDAO.apply(loan);
            return loan;
        }

        public List<Loan> getLoans(int userId) throws BankException {
            return loanDAO.findByUserId(userId);
        }

        private void validateOwnership(User currentUser, Account account) {
            if (account.getUserId() != currentUser.getUserId()) {
                fileLogger.log(
                        currentUser.getUserId(),
                        "UNAUTHORIZED_ACCESS",
                        "ACCOUNT",
                        account.getAccountId(),
                        "N/A",
                        "User tried to apply loan on another user's account"
                );
                throw new SecurityException("Unauthorized access");
            }
        }

    }

