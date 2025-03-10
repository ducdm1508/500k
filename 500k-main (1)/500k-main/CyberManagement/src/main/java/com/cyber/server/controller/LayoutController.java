package com.cyber.server.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class LayoutController {
    @FXML
    private BorderPane mainLayout;

    @FXML
    public Button dashboardButton, computerButton, customersButton, reportsButton,roomsButton, foodButton, logoutButton;

    private List<Button> menuButtons;

    @FXML
    public void initialize() {
        menuButtons = List.of(dashboardButton, computerButton, customersButton, reportsButton,foodButton, roomsButton);
        setCenterContent("/com/cyber/server/view/DashBoard.fxml", dashboardButton);
    }

    @FXML
    private void handleDashboardClick() {
      setCenterContent("/com/cyber/server/view/DashBoard.fxml", dashboardButton);
    }

    @FXML
    private void handleComputerClick() {
        setCenterContent("/com/cyber/server/view/Computer.fxml", computerButton);
    }
    @FXML
    private void handleCustomersClick() {
        setCenterContent("/com/cyber/server/view/Customer.fxml", customersButton);
    }
    @FXML
    private void handleReportsClick() {
        setCenterContent("/com/cyber/server/view/Report.fxml", reportsButton);
    }
    @FXML
    private void handleRoomClick() {setCenterContent("/com/cyber/server/view/Room.fxml", roomsButton);}
    @FXML
    private void handleFoodClick() {
        setCenterContent("/com/cyber/server/view/Food.fxml", foodButton);
    }
    @FXML
    private void handleLogoutClick() {

    }

    private void setCenterContent(String fxmlFile,Button activeButton) {
        try {
            Node content = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlFile)));
            mainLayout.setCenter(content);
            updateButtonStyles(activeButton);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateButtonStyles(Button activeButton) {
        for (Button button : menuButtons) {
            button.getStyleClass().remove("active-button");
        }
        activeButton.getStyleClass().add("active-button");
    }

}