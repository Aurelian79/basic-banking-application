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
import java.math.RoundingMode;
import java.util.List;

public class LoanService {
        private LoanDAO loanDAO = new LoanDAO();
        private AuditLogDAO auditLogDAO = new AuditLogDAO();
        private AccountDAO accountDAO = new AccountDAO();
        private FileLogger fileLogger = FileLogger.getInstance();

        public Loan apply(User currentUser, int accId, BigDecimal amount,int months) throws BankException {

            if (amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new BankException("Invalid amount");
            if (months <= 0) {
                throw new BankException("Invalid tenure months");
            }

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
            BigDecimal interestRate = resolveInterestRate(months);
            int emiAmount = calculateEmi(amount, interestRate, months);
            loan.setInterestRate(interestRate);
            loan.setEmiAmount(emiAmount);

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

        private BigDecimal resolveInterestRate(int months) {
            if (months <= 12) return new BigDecimal("10.00");
            if (months <= 36) return new BigDecimal("11.00");
            return new BigDecimal("12.00");
        }

        private int calculateEmi(BigDecimal principal, BigDecimal annualRatePct, int months) {
            BigDecimal monthlyRate = annualRatePct
                    .divide(new BigDecimal("1200"), 10, RoundingMode.HALF_UP);

            BigDecimal onePlusRPowerN = BigDecimal.ONE.add(monthlyRate).pow(months);
            BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowerN);
            BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);

            return numerator
                    .divide(denominator, 0, RoundingMode.HALF_UP)
                    .intValue();
        }

    }

