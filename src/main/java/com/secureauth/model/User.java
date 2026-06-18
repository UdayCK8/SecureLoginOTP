package com.secureauth.model;

import java.time.LocalDateTime;

/**
 * Plain Old Java Object (POJO) representing a registered user.
 * Mirrors the structure of the `users` table.
 */
public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String email;
    private LocalDateTime createdAt;
    private boolean active;

    public User() {
    }

    public User(String username, String passwordHash, String passwordSalt, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "User{userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active + '}';
    }
}
