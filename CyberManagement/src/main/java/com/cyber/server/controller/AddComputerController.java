package com.cyber.server.controller;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Computer;
import com.cyber.server.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
                room.setCapacity(rs.getInt("capacity"));
                rooms.add(room);
            }
            roomComboBox.getItems().addAll(rooms);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private boolean isComputerNameExists(String computerName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM computers WHERE computer_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, computerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    private boolean isRoomFull(int roomId, int capacity) throws SQLException {
        String sql = "SELECT COUNT(*) FROM computers WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();
            int computerCount = rs.next() ? rs.getInt(1) : 0;

            return computerCount >= capacity;
        }
    }
    private boolean isValidIpAddress(String ipAddress) {
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ipAddress.matches(ipPattern);
    }

    @FXML
    private void handleSaveButton() {
        if (nameInput.getText().isEmpty() || specificationsInput.getText().isEmpty() || ipAddressInput.getText().isEmpty() || roomComboBox.getValue() == null) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        if (!isValidIpAddress(ipAddressInput.getText())) {
            showAlert("Error", "Invalid IP address format!", Alert.AlertType.ERROR);
            return;
        }


        try {
            if (computer == null || !nameInput.getText().equals(computer.getName())) {
                if (isComputerNameExists(nameInput.getText())) {
                    showAlert("Error", "A computer with this name already exists!", Alert.AlertType.ERROR);
                    return;
                }
            }

            Room selectedRoom = roomComboBox.getValue();
            Room currentRoom = (computer != null) ? computer.getRoom() : null;

            if (currentRoom == null || !selectedRoom.equals(currentRoom)) {
                if (selectedRoom != null && isRoomFull(selectedRoom.getId(), selectedRoom.getCapacity())) {
                    showAlert("Error", "This room has reached its maximum capacity!", Alert.AlertType.ERROR);
                    return;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while checking room capacity.", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText("Are you sure you want to save?");
        confirmationAlert.setContentText("Click OK to save the data, or Cancel to go back.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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
            int roomId;

            if (selectedRoom != null) {
                roomId = selectedRoom.getId();
            } else if (computer.getRoom() != null) {
                roomId = computer.getRoom().getId();
            } else {
                throw new SQLException("Room ID is null, which violates foreign key constraint.");
            }

            pstmt.setInt(4, roomId);
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

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
