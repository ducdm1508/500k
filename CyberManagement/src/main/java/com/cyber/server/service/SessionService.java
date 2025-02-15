package com.cyber.server.service;

import com.cyber.server.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SessionService {
    private final int computerId;
    private boolean running = true;

    public SessionService(int computerId) {
        this.computerId = computerId;
    }

    public void startSessionMonitor() {
        new Thread(() -> {
            while (running) {
                loadSessionFromDatabase();
                try {
                    Thread.sleep(1000); // Cập nhật mỗi giây
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public void stopSessionMonitor() {
        running = false;
    }

    private void loadSessionFromDatabase() {
        String query = """
        SELECT s.session_id, u.username, s.start_time, s.end_time, 
               s.total_time, s.session_cost, u.balance, p.price
        FROM SESSIONS s
        JOIN USERS u ON s.user_id = u.user_id
        JOIN PRICING p ON p.service_name = 'Computer Usage'
        WHERE s.computer_id = ?
        ORDER BY s.start_time DESC
        LIMIT 1
    """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, computerId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int sessionId = resultSet.getInt("session_id");
                String username = resultSet.getString("username");
                String startTimeStr = resultSet.getString("start_time");
                String endTimeStr = resultSet.getString("end_time");
                double totalTimeUsed = resultSet.getDouble("total_time"); // Số giờ đã dùng trước đó
                double sessionCost = resultSet.getDouble("session_cost");
                double balance = resultSet.getDouble("balance");
                double pricePerHour = resultSet.getDouble("price");

                // Định dạng thời gian
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
                LocalDateTime endTime;

                // Tính toán thời gian tối đa có thể sử dụng dựa trên số dư
                double maxHours = balance / pricePerHour; // Thời gian tối đa có thể sử dụng
                endTime = startTime.plusHours((long) maxHours); // Cập nhật endTime

                // Tính số phút đã chơi từ start_time đến endTime
                long minutesPlayed = Duration.between(startTime, endTime).toMinutes();
                double costPerMinute = pricePerHour / 60;
                double costUsed = minutesPlayed * costPerMinute;

                // Tính số dư còn lại
                double remainingBalance = balance - costUsed;
                double remainingHours = remainingBalance / pricePerHour;

                System.out.print("\r"); // Xóa dòng cũ để cập nhật mới
                System.out.print(username);
                System.out.print(" - Thời gian đã chơi: " + String.format("%.2f", minutesPlayed / 60.0) + " giờ | ");
                System.out.print("Chi phí: " + String.format("%.2f", costUsed) + " VND | ");
                System.out.print("Còn lại: " + String.format("%.2f", remainingHours) + " giờ | ");
                System.out.print("Số dư: " + String.format("%.2f", remainingBalance) + " VND");
            } else {
                System.out.println("Không có phiên làm việc nào cho máy tính " + computerId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tải dữ liệu phiên làm việc", e);
        }
    }

    public static void main(String[] args) {
        SessionService service = new SessionService(1); // Chạy cho máy tính 1
        service.startSessionMonitor();

    }
}
