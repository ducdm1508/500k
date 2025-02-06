package com.cyber.server.controller;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Computer;
import com.cyber.server.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddComputerController {

    @FXML
    private TextField nameInput;

    @FXML
    private TextField specificationsInput;

    @FXML
    private TextField ipAddressInput;

    @FXML
    private ComboBox<Room> roomComboBox;

    private Computer computer;

    public void setComputer(Computer computer) {
        this.computer = computer;
        if (computer != null) {
            nameInput.setText(computer.getName());
            specificationsInput.setText(computer.getSpecifications());
            ipAddressInput.setText(computer.getIpAddress());

            Room currentRoom = computer.getRoom();
            if (currentRoom != null) {
                roomComboBox.setValue(currentRoom);
            }
        }
    }

    @FXML
    public void initialize() {
        loadRooms();
    }

    private void loadRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Room room = new Room();
                room.setId(rs.getInt("room_id"));
                room.setName(rs.getString("room_name"));
                rooms.add(room);
            }
            roomComboBox.getItems().addAll(rooms);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveButton() {
        try {
            if (computer != null) {
                updateComputer(computer);
            } else {
                addComputer();
            }
            closeWindow();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addComputer() throws SQLException {
        String sql = "INSERT INTO computers (computer_name, specifications, ip_address, room_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nameInput.getText());
            pstmt.setString(2, specificationsInput.getText());
            pstmt.setString(3, ipAddressInput.getText());

            Room selectedRoom = roomComboBox.getValue();
            if (selectedRoom != null) {
                pstmt.setInt(4, selectedRoom.getId());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }

            pstmt.executeUpdate();
        }
    }

    private void updateComputer(Computer computer) throws SQLException {
        System.out.println("🔹 updateComputer() method called!");

        String sql = "UPDATE computers SET computer_name = ?, specifications = ?, ip_address = ?, room_id = ? WHERE computer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nameInput.getText());
            pstmt.setString(2, specificationsInput.getText());
            pstmt.setString(3, ipAddressInput.getText());

            Room selectedRoom = roomComboBox.getValue();
            int roomId;

            // Kiểm tra xem room_id có bị null không
            if (selectedRoom != null) {
                roomId = selectedRoom.getId();
            } else if (computer.getRoom() != null) {
                roomId = computer.getRoom().getId();
            } else {
                System.out.println("❌ ERROR: Room ID is NULL! Update cannot proceed.");
                throw new SQLException("❗ ERROR: Room ID is null, which violates foreign key constraint.");
            }

            System.out.println("✅ Room ID to be updated: " + roomId);

            pstmt.setInt(4, roomId);
            pstmt.setInt(5, computer.getId());

            System.out.println("🔹 Preparing to execute update...");
            System.out.println("Computer ID: " + computer.getId());
            System.out.println("Room ID before update: " + (computer.getRoom() != null ? computer.getRoom().getId() : "NULL"));
            System.out.println("New Room ID: " + roomId);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("🔹 Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                System.out.println("✅ Update successful!");
            } else {
                System.out.println("❌ Update failed. No rows affected.");
            }
        } catch (SQLException e) {
            System.out.println("❗ SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleCancelButton() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameInput.getScene().getWindow();
        stage.close();
    }

}