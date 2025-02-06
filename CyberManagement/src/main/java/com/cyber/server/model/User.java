package com.cyber.server.model;

public class User {
    private String username;
    private String password;
    private double balance;
    private String phoneNumber;

    public User(String username, String password, double balance, String phoneNumber) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}