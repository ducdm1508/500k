package com.cyber.server.model;

import java.time.LocalDateTime;

public class Session {
    private int id;
    private Computer computer;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double totalTime;
    private double sessionCost;

    public Session() {
    }

    public Session(Computer computer, LocalDateTime startTime, LocalDateTime endTime, double totalTime, double sessionCost) {
        this.computer = computer;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalTime = totalTime;
        this.sessionCost = sessionCost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Computer getComputer() {
        return computer;
    }

    public void setComputer(Computer computer) {
        this.computer = computer;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public double getSessionCost() {
        return sessionCost;
    }

    public void setSessionCost(double sessionCost) {
        this.sessionCost = sessionCost;
    }
}