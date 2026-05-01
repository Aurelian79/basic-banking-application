package com.bank.dao;

import com.bank.config.DBConnection;
import com.bank.exception.BankException;
import com.bank.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    private DBConnection getDbConnection() {
        return DBConnection.getInstance();
    }

    public int createUser(User user) throws BankException {

        Connection conn = null;
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, role, is_active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW()) RETURNING user_id";

        try {
            conn = getDbConnection().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole());
            ps.setBoolean(6, true);

            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            }

            throw new BankException("Failed to create user");
        } catch (SQLException e) {
            throw new BankException("Error creating user  " , e);
        }finally {
            getDbConnection().releaseConnection(conn);
        }
    }

    public User findByEmail(String email) throws BankException{
        String sql = "SELECT * FROM users WHERE email = ?";
        Connection conn = null;
        try{
            conn = getDbConnection().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,email);

            ResultSet resultSet = ps.executeQuery();

            if(!resultSet.next())
                return null;
            return mapRow(resultSet);

        } catch (SQLException e) {
            throw new BankException("Error finding user by email" , e);
        }finally {
            getDbConnection().releaseConnection(conn);
        }
    }

    public User findById(int userId) throws BankException{
        String sql = "SELECT * FROM users WHERE user_id = ?";
        Connection conn = null;
        try{
            conn = getDbConnection().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,userId);

            ResultSet resultSet = ps.executeQuery();

            if(!resultSet.next())
                return null;
            return mapRow(resultSet);

        } catch (SQLException e) {
            throw new BankException("Error finding user by email" , e);
        }finally {
            getDbConnection().releaseConnection(conn);
        }
    }

    public void updateStatus(int userId,boolean isActive) throws BankException{

        String sql = "UPDATE users SET is_active = ?, updated_at = NOW() where user_id = ?";

        Connection conn = null;
        try{
            conn  = getDbConnection().getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setBoolean(1, isActive);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new BankException("Error updatng user status" , e);
        }finally {
            getDbConnection().releaseConnection(conn);
        }
    }

    public User getUserById(int userId) throws BankException {
        return findById(userId);
    }



    private User mapRow(ResultSet resultSet) throws SQLException {

        User u = new User();
        u.setUserId(resultSet.getInt("user_id"));
        u.setFullName(resultSet.getString("full_name"));
        u.setEmail(resultSet.getString("email"));
        u.setPhone(resultSet.getString("phone"));
        u.setPasswordHash(resultSet.getString("password_hash"));
        u.setRole(resultSet.getString("role"));
        u.setActive(resultSet.getBoolean("is_active"));
        u.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return u;
    }

}
