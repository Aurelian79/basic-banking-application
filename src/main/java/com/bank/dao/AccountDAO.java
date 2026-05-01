package com.bank.dao;

import com.bank.config.DBConnection;
import com.bank.exception.BankException;
import com.bank.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private DBConnection getDbConnection() {
        return DBConnection.getInstance();
    }

    public int createAccount(Account account) throws BankException {
        String sql = "INSERT INTO accounts (account_number, user_id, account_type, balance, interest_rate) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;

        try{
            conn = getDbConnection().getConnection();

            PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, account.getAccountNumber());
            ps.setInt(2, account.getUserId());
            ps.setString(3, account.getAccountType());
            ps.setBigDecimal(4, account.getBalance());
            ps.setBigDecimal(5, account.getInterestRate());

            ps.executeUpdate();

            ResultSet resultSet= ps.getGeneratedKeys();

            if(resultSet.next())
                return resultSet.getInt(1);
            throw new BankException("Failed to create account");


        } catch (SQLException e) {
            throw new BankException("Error fetching accounts" + e);
        }finally {
            getDbConnection().releaseConnection(conn);
        }
    }
    public List<Account> findUserById(int userId) throws BankException{
        String sql ="SELECT * FROM accounts WHERE user_id = ?";

        List <Account> list = new ArrayList<>();
        Connection conn = null;

        try {
            conn = getDbConnection().getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet resultSet = ps.executeQuery();


            while (resultSet.next()) {
                Account acc = new Account();
                acc.setAccountId(resultSet.getInt("account_id"));
                acc.setAccountNumber(resultSet.getString("account_number"));
                acc.setUserId(resultSet.getInt("user_id"));
                acc.setAccountType(resultSet.getString("account_type"));
                acc.setBalance(resultSet.getBigDecimal("balance"));
                acc.setStatus(resultSet.getString("status"));
                acc.setInterestRate(resultSet.getBigDecimal("interest_rate"));
                acc.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

                list.add(acc);
            }

            return list;


        } catch(SQLException e) {
            throw new BankException("Error fetching accounts",e);
        }finally {
         getDbConnection().releaseConnection(conn);
        }
    }

    public Account findByAccountNumber(String accNo) throws BankException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";

        Connection conn = null;
        try {
            conn = getDbConnection().getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, accNo);

            ResultSet resultSet = ps.executeQuery();

            if (!resultSet.next())
                return null;

            return mapRow(resultSet);

        } catch (SQLException e) {
            throw new BankException("Error finding account", e);
        } finally {
            getDbConnection().releaseConnection(conn);
        }
    }

    public Account findById(int accountId) throws BankException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";

        Connection conn = null;
        try {
            conn = getDbConnection().getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, accountId);

            ResultSet resultSet = ps.executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            return mapRow(resultSet);

        } catch (SQLException e) {
            throw new BankException("Error finding account by id", e);
        } finally {
            getDbConnection().releaseConnection(conn);
        }
    }

    public void updateBalance(int accountId, BigDecimal balance, Connection conn) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setBigDecimal(1, balance);
        ps.setInt(2, accountId);

        ps.executeUpdate();
    }

    private Account mapRow(ResultSet resultSet) throws SQLException {
        Account acc = new Account();
        acc.setAccountId(resultSet.getInt("account_id"));
        acc.setAccountNumber(resultSet.getString("account_number"));
        acc.setUserId(resultSet.getInt("user_id"));
        acc.setAccountType(resultSet.getString("account_type"));
        acc.setBalance(resultSet.getBigDecimal("balance"));
        acc.setStatus(resultSet.getString("status"));
        acc.setInterestRate(resultSet.getBigDecimal("interest_rate"));

        if (resultSet.getTimestamp("created_at") != null) {
            acc.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        }

        return acc;
    }
}



