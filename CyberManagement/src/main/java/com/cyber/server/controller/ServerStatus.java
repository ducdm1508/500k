package com.cyber.server.controller;

import com.cyber.server.database.DatabaseConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ServerStatus {
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is running on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Client connected: " + clientIP);
                updateStatus(clientIP, "ONLINE");
                new Thread(() -> handleClient(clientSocket, clientIP)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket, String clientIP) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Message from " + clientIP + ": " + message);
                if (message.equalsIgnoreCase("OFFLINE")) {
                    updateStatus(clientIP, "OFFLINE");
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + clientIP);
            updateStatus(clientIP, "OFFLINE");
        }
    }

    private static void updateStatus(String ipAddress, String status) {
        String useDB="USE netcafedb";
        String query = "UPDATE computers SET status = ? WHERE ip_address = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatementUseDb = connection.prepareStatement(useDB);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatementUseDb.execute();
            preparedStatement.setString(1, status);
            preparedStatement.setString(2, ipAddress);

            int rows = preparedStatement.executeUpdate();
            if (rows > 0) {
                System.out.println("Updated status of " + ipAddress + " to " + status);
            } else {
                System.out.println("No computer found with IP: " + ipAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
