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
        String sql = "UPDATE computers SET computer_name = ?, specifications = ?, ip_address = ?, room_id = ? WHERE computer_id = ?";
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

            pstmt.setInt(5, computer.getId());
            pstmt.executeUpdate();
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