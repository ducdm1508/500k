package com.cyber.server.controller;

import com.cyber.server.model.Computer;
import com.cyber.server.model.Room;
import com.cyber.server.model.Status;
import com.cyber.server.database.DatabaseConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HelloController {

    @FXML
    private TableView<Computer> tableView;

    @FXML
    private TableColumn<Computer, String> nameColumn;

    @FXML
    private TableColumn<Computer, String> statusColumn;

    @FXML
    private TableColumn<Computer, String> specificationsColumn;

    @FXML
    private TableColumn<Computer, String> ipAddressColumn;

    @FXML
    private TableColumn<Computer, String> roomColumn;

    @FXML
    private TableColumn<Computer, LocalDateTime> lastMaintenanceDateColumn;

    @FXML
    private TableColumn<Computer, Void> actionColumn;

    @FXML
    private Button addButton;

    private ObservableList<Computer> computerList = FXCollections.observableArrayList();
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        configureTable();
        loadComputersFromDatabase();
        startDatabasePolling();
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        specificationsColumn.setCellValueFactory(new PropertyValueFactory<>("specifications"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        roomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoom().getName()));
        lastMaintenanceDateColumn.setCellValueFactory(new PropertyValueFactory<>("lastMaintenanceDate"));

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();

            {
                editButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.PENCIL));
                deleteButton.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TRASH));

                editButton.setOnAction(event -> {
                    Computer computer = getTableView().getItems().get(getIndex());
                    openEditWindow(computer);
                });

                deleteButton.setOnAction(event -> {
                    Computer computer = getTableView().getItems().get(getIndex());
                    handleDeleteAction(computer);
                });

                editButton.setStyle("-fx-background-color: green;");
                deleteButton.setStyle("-fx-background-color: red;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(10, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadComputersFromDatabase() {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT c.*, r.room_name FROM computers c LEFT JOIN rooms r ON c.room_id = r.room_id")) {
            computerList.clear();
            while (rs.next()) {
                Computer computer = new Computer();
                computer.setId(rs.getInt("computer_id"));
                computer.setName(rs.getString("computer_name"));
                computer.setStatus(Status.valueOf(rs.getString("status").toUpperCase()));
                computer.setSpecifications(rs.getString("specifications"));
                computer.setIpAddress(rs.getString("ip_address"));

                if (rs.getTimestamp("last_maintenance_date") != null) {
                    computer.setLastMaintenanceDate(rs.getTimestamp("last_maintenance_date").toLocalDateTime());
                }

                Room room = new Room();
                room.setName(rs.getString("room_name"));
                computer.setRoom(room);

                computerList.add(computer);
            }
            tableView.setItems(computerList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDatabasePolling() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            loadComputersFromDatabase();
        }, 0, 5, TimeUnit.SECONDS);
    }

    @FXML
    private void openNewWindowAddComputer() {
        openAddEditWindow(null);
    }

    public void openEditWindow(Computer computer) {
        openAddEditWindow(computer);
    }

    private void openAddEditWindow(Computer computer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cyber/server/view/addnewcomputer.fxml"));
            Parent root = loader.load();

            AddComputerController controller = loader.getController();

            if (computer != null) {
                controller.setComputer(computer);
            }

            Stage stage = new Stage();
            stage.setTitle(computer == null ? "Add New Computer" : "Edit Computer");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteAction(Computer computer) {
        if (computer == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Computer");
        alert.setContentText("Are you sure you want to delete the computer: " + computer.getName() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection connection = DatabaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {

                String deleteQuery = "DELETE FROM computers WHERE computer_id = " + computer.getId();
                int rowsAffected = statement.executeUpdate(deleteQuery);

                if (rowsAffected > 0) {
                    computerList.remove(computer);

                    // Show success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("The computer has been successfully deleted.");
                    successAlert.showAndWait();
                } else {
                    // If no rows were affected
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Could not delete the computer. Please try again.");
                    errorAlert.showAndWait();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("An error occurred while deleting the computer. Please try again.");
                errorAlert.showAndWait();
            }
        }
    }

}