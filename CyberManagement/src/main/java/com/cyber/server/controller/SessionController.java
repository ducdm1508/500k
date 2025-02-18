package com.cyber.server.controller;

import com.cyber.server.database.DatabaseConnection;
import com.cyber.server.model.Computer;
import com.cyber.server.model.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class SessionController {
    @FXML
    private TableView<Session> sessionTable;

    @FXML
    private TableColumn<Session, String> computerNameColumn;

    @FXML
    private TableColumn<Session, String> startTimeColumn;

    @FXML
    private TableColumn<Session, String> endTimeColumn;

    @FXML
    private TableColumn<Session, String> totalTimeColumn;

    @FXML
    private TableColumn<Session, String> sessionCostColumn;

    private final ObservableList<Session> sessionList = FXCollections.observableArrayList();
    private boolean isDataChanged = false;

    @FXML
    public void initialize() {
        configureTable();
        loadSessionFromData();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
            checkForUpdates();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    private void checkForUpdates() {
        boolean newSessionAdded = insertSessionIfOnline();
        boolean sessionUpdated = updateSessionIfOffline();

        if (newSessionAdded || sessionUpdated || isDataChanged) {
            loadSessionFromData();
            isDataChanged = false;
        }
    }
    public void configureTable() {
        computerNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getComputer().getName()));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        startTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStartTime().format(timeFormatter)));

        endTimeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEndTime() != null
                        ? cellData.getValue().getEndTime().format(timeFormatter)
                        : ""));

        totalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalTime"));
        sessionCostColumn.setCellValueFactory(new PropertyValueFactory<>("sessionCost"));

        sessionTable.setItems(sessionList);
    }

    public void loadSessionFromData() {
        sessionList.clear(); // Xóa danh sách hiện tại
        String sql = "SELECT s.*, c.computer_name " +
                "FROM sessions s " +
                "JOIN computers c ON s.computer_id = c.computer_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Session session = new Session();
                Computer computer = new Computer();

                computer.setId(resultSet.getInt("computer_id"));
                computer.setName(resultSet.getString("computer_name"));

                session.setId(resultSet.getInt("session_id"));
                session.setComputer(computer);
                session.setStartTime(resultSet.getTimestamp("start_time").toLocalDateTime());

                Timestamp endTime = resultSet.getTimestamp("end_time");
                session.setEndTime(endTime != null ? endTime.toLocalDateTime() : null);

                session.setTotalTime(resultSet.getDouble("total_time"));
                session.setSessionCost(resultSet.getDouble("session_cost"));

                sessionList.add(session);
                System.out.println("Đã thêm phiên: " + session.getId() + ", Máy tính: " + computer.getName());
            }
            System.out.println("Dữ liệu đã được tải thành công. Số phiên: " + sessionList.size());
            sessionTable.refresh();
            sessionTable.setItems(sessionList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean insertSessionIfOnline() {
        String sql = "INSERT INTO sessions (computer_id, start_time, end_time, total_time, session_cost) " +
                "SELECT c.computer_id, NOW(), NULL, NULL, NULL " +
                "FROM computers c " +
                "WHERE c.status = 'online' " +
                "AND NOT EXISTS ( " +
                "    SELECT 1 FROM sessions s " +
                "    WHERE s.computer_id = c.computer_id AND s.end_time IS NULL" +
                ")";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Đã thêm phiên mới cho các máy tính đang online.");

                isDataChanged = true; // Đánh dấu dữ liệu đã thay đổi
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int rate_per_hour = 10000;

    public boolean updateSessionIfOffline() {
        String sql = "UPDATE sessions s " +
                "JOIN computers c ON s.computer_id = c.computer_id " +
                "SET s.end_time = NOW(), " +
                "    s.total_time = TIMESTAMPDIFF(SECOND, s.start_time, NOW()) / 3600, " +
                "    s.session_cost = (TIMESTAMPDIFF(SECOND, s.start_time, NOW()) / 3600) * ? " +
                "WHERE c.status = 'offline' AND s.end_time IS NULL";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, rate_per_hour);
            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Đã cập nhật phiên cho các máy tính chuyển sang offline.");
                isDataChanged = true; // Đánh dấu dữ liệu đã thay đổi
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}