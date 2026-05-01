package com.bank.model;

import java.time.LocalDateTime;

public class User {
    private int UserId;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;
    private boolean isActive;
    private LocalDateTime createdAt;

    public User(){}


    public User(String fullName, String email, String phone, String passwordHash, String role, boolean isActive, LocalDateTime createdAt) {

        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public User(String name, String email, String phone, String hashed, String customer) {
        this.fullName = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = hashed;
        this.role = customer;
        this.isActive = true;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}

