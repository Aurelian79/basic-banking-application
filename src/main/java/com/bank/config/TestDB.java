package com.bank.config;

import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {

        DBConnection db = DBConnection.getInstance();

        Connection conn = db.getConnection();

        if (conn != null) {
            System.out.println("🎉 Connection successful!");
        }

        db.releaseConnection(conn);
    }
}