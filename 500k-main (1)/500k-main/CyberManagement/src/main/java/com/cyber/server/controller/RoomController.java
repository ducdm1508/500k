package com.cyber.server.controller;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Room;
import com.cyber.server.model.RoomType;
import com.cyber.server.model.RoomTypeName;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class RoomController {

    @FXML
    private FlowPane roomGrid;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<RoomTypeName> roomType; // ComboBox for selecting room type
    @FXML
    private TextField capacityField;

    private ObservableList<Room> roomList = FXCollections.observableArrayList();
    private Room selectedRoom;

    private static final String SELECT_ROOMS_QUERY = "SELECT room_id, room_name, capacity, room_type_id FROM ROOMS"; // Include room_id
    private static final String SELECT_ROOM_TYPE_QUERY = "SELECT type_name FROM ROOM_TYPES WHERE room_type_id = ?";
    private static final String INSERT_ROOM_QUERY = "INSERT INTO ROOMS (room_name, room_type_id, capacity) VALUES (?, (SELECT room_type_id FROM ROOM_TYPES WHERE type_name = ?), ?)";
    private static final String UPDATE_ROOM_QUERY = "UPDATE ROOMS SET room_name = ?, room_type_id = (SELECT room_type_id FROM ROOM_TYPES WHERE type_name = ?), capacity = ? WHERE room_id = ?";
    private static final String DELETE_ROOM_QUERY = "DELETE FROM ROOMS WHERE room_id = ?"; // Use room_id for deletion

    @FXML
    public void initialize() {
        loadRooms();
        loadRoomTypes();
    }

    private void loadRooms() {
        roomGrid.getChildren().clear();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ROOMS_QUERY);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int roomId = resultSet.getInt("room_id"); // Fetch the room ID
                String roomName = resultSet.getString("room_name"); // Fetch the room name
                int capacity = resultSet.getInt("capacity"); // Fetch the room capacity
                int roomTypeId = resultSet.getInt("room_type_id"); // Fetch the room type ID

                RoomType roomTypeObj = getRoomTypeById(roomTypeId); // Rename variable to avoid shadowing

                if (roomTypeObj != null) { // Check if roomTypeObj is not null
                    Room room = new Room();
                    room.setId(roomId);
                    room.setName(roomName);
                    room.setType(roomTypeObj);
                    room.setCapacity(capacity);

                    roomList.add(room); // Add the room to the list
                    StackPane roomBox = createRoomBox(room); // Create a visual representation for the room
                    roomGrid.getChildren().add(roomBox); // Add the room box to the FlowPane
                } else {
                    showError("Room type not found for ID: " + roomTypeId);
                }
            }
        } catch (SQLException e) {
            showError("Error loading rooms: " + e.getMessage());
        }
    }

    private int countMachinesInRoom(int roomId) {
        String query = "SELECT COUNT(*) AS machine_count FROM COMPUTERS WHERE room_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, roomId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("machine_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Trả về 0 nếu có lỗi hoặc không có máy
    }

    private RoomType getRoomTypeById(int roomTypeId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ROOM_TYPE_QUERY)) {
            statement.setInt(1, roomTypeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String typeName = resultSet.getString("type_name");
                RoomTypeName roomTypeName = RoomTypeName.valueOf(typeName.toUpperCase());
                return new RoomType(roomTypeId, roomTypeName, "");
            }
        } catch (SQLException e) {
            showError("Error fetching room type: " + e.getMessage());
        }
        return null;
    }

    private void loadRoomTypes() {
        roomType.getItems().clear();
        for (RoomTypeName type : RoomTypeName.values()) {
            roomType.getItems().add(type);
        }
    }

    private StackPane createRoomBox(Room room) {
        StackPane box = new StackPane();
        box.setPrefSize(180, 180);

        // Create a rectangle with rounded corners and drop shadow effect
        Rectangle rect = new Rectangle(180, 180);
        rect.setArcWidth(20);
        rect.setArcHeight(20);
        rect.setFill(Color.LIGHTBLUE);
        rect.setStroke(Color.DARKBLUE);
        rect.setStrokeWidth(2);
        rect.setEffect(new DropShadow(10, Color.GRAY));

        // Create a VBox to hold room information
        VBox vbox = new VBox(8);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(15));

        Label nameLabel = new Label(room.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label capacityLabel = new Label("Capacity: " + room.getCapacity());
        capacityLabel.setStyle("-fx-font-size: 14px;");

        Label typeLabel = new Label("Type: " + room.getType().getRoomTypeName());
        typeLabel.setStyle("-fx-font-size: 14px;");

        int machineCount = countMachinesInRoom(room.getId());
        Label machineLabel = new Label("Machines: " + machineCount);
        machineLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");

        Color defaultColor = Color.LIGHTBLUE;
        Color fullRoomColor = Color.DARKSLATEBLUE; // Màu đậm hơn khi phòng đầy

// Màu sắc khi hover
        Color hoverColor = Color.CORNFLOWERBLUE;

// Nếu số lượng máy >= capacity thì thay đổi màu nền của ô và tên phòng
        if (machineCount >= room.getCapacity()) {
            rect.setFill(fullRoomColor);  // Màu nền đậm hơn
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        } else {
            rect.setFill(defaultColor);  // Màu nền bình thường
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        }

// Hover effect
        box.setOnMouseEntered(e -> {
            rect.setFill(hoverColor);  // Màu khi hover
        });
        box.setOnMouseExited(e -> {
            // Quay lại màu gốc khi không hover nữa, và vẫn giữ màu nếu phòng đầy
            if (machineCount >= room.getCapacity()) {
                rect.setFill(fullRoomColor);  // Quay lại màu đậm khi đầy máy
            } else {
                rect.setFill(defaultColor);  // Quay lại màu bình thường
            }
        });
        vbox.getChildren().addAll(nameLabel, capacityLabel, typeLabel, machineLabel);

        // Hover effect

        // Click event to select room
        box.setOnMouseClicked(event -> {
            selectedRoom = room;
            nameField.setText(room.getName());
            capacityField.setText(String.valueOf(room.getCapacity()));
            roomType.setValue(room.getType().getRoomTypeName());
        });

        box.getChildren().addAll(rect, vbox);

        // Ensure boxes have margin in FlowPane
        FlowPane.setMargin(box, new Insets(10));

        return box;
    }

    @FXML
    private void addRoom() {
        // Check if fields are not empty
        if (nameField.getText().isEmpty() || capacityField.getText().isEmpty() || roomType.getValue() == null) {
            showError("All fields must be filled.");
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ROOM_QUERY)) {
            statement.setString(1, nameField.getText());
            RoomTypeName roomTypeName = roomType.getValue();
            statement.setString(2, roomTypeName.name());
            statement.setString(3, capacityField.getText());
            statement.executeUpdate();
            loadRooms();
            clearFields();
        } catch (SQLException e) {
            showError("Error adding room: " + e.getMessage());
        }
    }

    private void updateRoom(Room room) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ROOM_QUERY)) {
            RoomTypeName roomTypeName = roomType.getValue();
            statement.setString(1, nameField.getText());
            statement.setString(2, roomTypeName.name());
            statement.setInt(3, Integer.parseInt(capacityField.getText()));
            statement.setInt(4, room.getId()); // Ensure room.getId() returns a valid ID

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                loadRooms();
                clearFields();
            } else {
                showError("Room not found for updating.");
            }
        } catch (SQLException e) {
            showError("Error updating room: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Invalid capacity format.");
        }
    }

    @FXML
    private void handleSave() {
        if (selectedRoom != null) {
            updateRoom(selectedRoom);
        } else {
            addRoom();
        }
    }

    @FXML
    private void handleCancel() {
        clearFields();
        selectedRoom = null; // Reset selected room
    }

    @FXML
    private void handleDelete() {
        if (selectedRoom != null) {
            int roomIdToDelete = selectedRoom.getId(); // Use room ID for deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Are you sure you want to delete the room: " + selectedRoom.getName() + "?");
            alert.setContentText("Click OK to confirm, or Cancel to go back.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(DELETE_ROOM_QUERY)) {
                    statement.setInt(1, roomIdToDelete); // Set the room ID for deletion
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        loadRooms();
                        clearFields();
                    } else {
                        showError("Room does not exist.");
                    }
                } catch (SQLException e) {
                    showError("Room does not exist or could not be deleted.");
                }
            }
        } else {
            showError("Please select a room to delete.");
        }
    }

    private void clearFields() {
        nameField.clear();
        roomType.setValue(null);
        capacityField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }
}