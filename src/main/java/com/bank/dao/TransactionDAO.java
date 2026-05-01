package com.bank.dao;

import com.bank.config.DBConnection;
import com.bank.exception.BankException;
import com.bank.model.Transaction;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private DBConnection getDbConnection() {
        return DBConnection.getInstance();
    }

    public int insert(Transaction txn, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions "
                + "(from_account_id, to_account_id, txn_type, amount, balance_after, "
                + "description, reference_no, txn_timestamp) "
                + "VALUES (?,?,?,?,?,?,?,?)";

        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        if (txn.getFromAccountId() == 0)
            ps.setNull(1, Types.INTEGER);
        else
            ps.setInt(1, txn.getFromAccountId());

        if (txn.getToAccountId() == 0)
            ps.setNull(2, Types.INTEGER);
        else
            ps.setInt(2, txn.getToAccountId());

        ps.setString(3, txn.getTxnType());
        ps.setBigDecimal(4, txn.getAmount());
        ps.setBigDecimal(5, txn.getBalanceAfter());
        ps.setString(6, txn.getDescription());
        ps.setString(7, txn.getReferenceNo());
        ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

        ps.executeUpdate();

        ResultSet resultSet = ps.getGeneratedKeys();
        if (resultSet.next()) return resultSet.getInt(1);

        throw new SQLException("Transaction insert failed");
    }

    public List<Transaction> findByAccountId(int accId, LocalDate from, LocalDate to, Connection conn) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE (from_account_id = ? OR to_account_id = ?) AND txn_timestamp BETWEEN ? AND ? ORDER BY txn_timestamp DESC";

        boolean ownsConnection = false;
        if (conn == null) {
            conn = getDbConnection().getConnection();
            ownsConnection = true;
        }

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, accId);
            ps.setInt(2, accId);
            ps.setTimestamp(3, Timestamp.valueOf(from.atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(to.plusDays(1).atStartOfDay().minusSeconds(1)));

            ResultSet resultSet = ps.executeQuery();

            List<Transaction> list = new ArrayList<>();

            while (resultSet.next()) {
                Transaction t = new Transaction();
                t.setTxnId(resultSet.getInt("txn_id"));
                t.setFromAccountId(resultSet.getInt("from_account_id"));
                t.setToAccountId(resultSet.getInt("to_account_id"));
                t.setTxnType(resultSet.getString("txn_type"));
                t.setAmount(resultSet.getBigDecimal("amount"));
                t.setBalanceAfter(resultSet.getBigDecimal("balance_after"));
                t.setDescription(resultSet.getString("description"));
                t.setReferenceNo(resultSet.getString("reference_no"));
                Timestamp timestamp = resultSet.getTimestamp("txn_timestamp");
                if (timestamp != null) {
                    t.setTxnTimestamp(timestamp.toLocalDateTime());
                }
                list.add(t);
            }

            return list;
        } finally {
            if (ownsConnection) {
                getDbConnection().releaseConnection(conn);
            }
        }
    }
}
