package com.bank.dao;

import com.bank.config.DBConnection;
import com.bank.exception.BankException;
import com.bank.model.Loan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    private DBConnection getDbConnection() {
        return DBConnection.getInstance();
    }

    public int apply(Loan loan) throws BankException {
        String sql = "INSERT INTO loans (user_id, account_id, loan_amount, interest_rate, tenure_months, status) VALUES (?,?,?,?,?,'PENDING')";

        Connection conn = null;

        try {
            conn = getDbConnection().getConnection();

            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, loan.getUserId());
            ps.setInt(2, loan.getAccountId());
            ps.setBigDecimal(3, loan.getLoanAmount());
            ps.setBigDecimal(4, loan.getInterestRate());
            ps.setInt(5, loan.getTenureMonths());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);

            throw new BankException("Loan apply failed");

        } catch (SQLException e) {
            throw new BankException("Error applying loan", e);
        } finally {
            getDbConnection().releaseConnection(conn);
        }
    }

    public List<Loan> findByUserId(int userId) throws BankException {
        String sql = "SELECT * FROM loans WHERE user_id = ?";

        List<Loan> list = new ArrayList<>();
        Connection conn = null;

        try {
            conn = getDbConnection().getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Loan l = new Loan();
                l.setLoanId(rs.getInt("loan_id"));
                l.setUserId(rs.getInt("user_id"));
                l.setAccountId(rs.getInt("account_id"));
                l.setLoanAmount(rs.getBigDecimal("loan_amount"));
                l.setInterestRate(rs.getBigDecimal("interest_rate"));
                l.setTenureMonths(rs.getInt("tenure_months"));
                l.setEmiAmount(rs.getInt("emi_amount"));
                l.setStatus(rs.getString("status"));
                list.add(l);
            }

            return list;

        } catch (SQLException e) {
            throw new BankException("Error fetching loans", e);
        } finally {
            getDbConnection().releaseConnection(conn);
        }
    }
}
