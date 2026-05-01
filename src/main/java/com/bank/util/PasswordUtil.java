package com.bank.util;

import java.security.MessageDigest;

public class PasswordUtil {

    public static String hash(String password) {
        try {
            // Step 1: Get SHA-256 instance
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Step 2: Convert password to bytes and hash it
            byte[] hashBytes = md.digest(password.getBytes());

            // Step 3: Convert bytes to hex string
            StringBuilder hex = new StringBuilder();

            for (byte b : hashBytes) {
                String hexByte = Integer.toHexString(0xff & b);

                if (hexByte.length() == 1)
                    hex.append('0'); // padding

                hex.append(hexByte);
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}