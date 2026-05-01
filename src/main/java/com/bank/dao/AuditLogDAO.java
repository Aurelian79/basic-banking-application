package com.bank.dao;

import com.bank.config.DBConnection;
import com.bank.exception.BankException;
import com.bank.model.AuditLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditLogDAO {

    private DBConnection getDbConnection() {
        return DBConnection.getInstance();
    }

    public void log(AuditLog log) throws BankException {
        String sql = "INSERT INTO audit_logs (user_id, action, entity, entity_id, ip_address, details) VALUES (?,?,?,?,?,?)";

        Connection conn = null;

        try {
            conn = getDbConnection().getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, log.getUserId());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getEntity());
            ps.setInt(4, log.getEntityId());
            ps.setString(5, log.getIpAddress());
            ps.setString(6, log.getDetails());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new BankException("Error logging audit", e);
        } finally {
            getDbConnection().releaseConnection(conn);
        }
    }
}
