package com.cyber.client.controller;

import com.cyber.client.database.DatabaseConnection;
import com.cyber.client.model.Food;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class FoodController implements Initializable {
    public static final String CURRENCY = "$";

    @FXML
    public Label descriptionLabel;

    @FXML
    private VBox chosenFruitCard;

    @FXML
    private Label fruitNameLable;

    @FXML
    private Label fruitPriceLabel;

    @FXML
    private ImageView fruitImg;

    @FXML
    private GridPane grid;

    @FXML
    private ComboBox<String> categoryComboBox; // ComboBox for categories

    @FXML
    private TextField searchTextField; // TextField for search input

    @FXML
    private Button searchButton; // Button to trigger search

    private final List<Food> foods = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        foods.addAll(getDataFromDatabase());

        // Populate categories and add "All Categories" option
        List<String> categories = getCategoriesFromDatabase();
        categories.add(0, "All Categories"); // Add "All Categories" at the top
        categoryComboBox.getItems().addAll(categories);
        categoryComboBox.getSelectionModel().selectFirst(); // Select the first category by default

        if (foods.size() > 0) {
            setChosenFruit(foods.get(0));
        }

        loadFoodItems(); // Load food items into the grid

        // Add listener to filter foods when a category is selected
        categoryComboBox.setOnAction(event -> {
            String selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
            if ("All Categories".equals(selectedCategory)) {
                // If "All Categories" is selected, load all food items
                foods.clear();
                foods.addAll(getDataFromDatabase());
                refreshFoodGrid();
            } else {
                filterFoodsByCategory(selectedCategory);
            }
        });

        // Add listener to the search button
        searchButton.setOnAction(event -> performSearch());

        // Add listener to the TextField to reload all items when cleared
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                // If the TextField is empty, reload all food items
                foods.clear();
                foods.addAll(getDataFromDatabase());
                refreshFoodGrid();
            }
        });
    }

    private List<Food> getDataFromDatabase() {
        List<Food> foods = new ArrayList<>();
        String query = "SELECT FOODS.food_name, FOODS.description, FOODS.price, FOODS.quantity, FOODS.image_url, FOOD_CATEGORIES.category_name " +
                "FROM FOODS " +
                "JOIN FOOD_CATEGORIES ON FOODS.category_id = FOOD_CATEGORIES.category_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Food food = new Food();
                food.setName(rs.getString("food_name"));
                food.setDescription(rs.getString("description"));
                food.setPrice(rs.getDouble("price"));
                food.setImgSrc(rs.getString("image_url"));
                food.setColor("36454F");
                foods.add(food);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foods;
    }

    private List<String> getCategoriesFromDatabase() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT category_name FROM FOOD_CATEGORIES";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    private void filterFoodsByCategory(String category) {
        foods.clear(); // Clear the current food list
        String query = "SELECT FOODS.food_name, FOODS.description, FOODS.price, FOODS.quantity, FOODS.image_url " +
                "FROM FOODS " +
                "JOIN FOOD_CATEGORIES ON FOODS.category_id = FOOD_CATEGORIES.category_id " +
                "WHERE FOOD_CATEGORIES.category_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Food food = new Food();
                food.setName(rs.getString("food_name"));
                food.setDescription(rs.getString("description"));
                food.setPrice(rs.getDouble("price"));
                food.setImgSrc(rs.getString("image_url"));
                food.setColor("36454F");
                foods.add(food);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refreshFoodGrid(); // Refresh the grid with the filtered foods
    }

    private void refreshFoodGrid() {
        grid.getChildren().clear(); // Clear existing items
        loadFoodItems(); // Load food items into the grid
    }

    private void loadFoodItems() {
        int column = 0;
        int row = 1;
        try {
            for (int i = 0; i < foods.size(); i++) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/com/cyber/client/view/Item.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();
                ItemController itemController = fxmlLoader.getController();
                itemController.setData(foods.get(i), this);
                if (column == 3) {
                    column = 0;
                    row++;
                }
                grid.add(anchorPane, column++, row);
                grid.setMinWidth(Region.USE_COMPUTED_SIZE);
                grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
                grid.setMaxWidth(Region.USE_PREF_SIZE);
                grid.setMinHeight(Region.USE_COMPUTED_SIZE);
                grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
                grid.setMaxHeight(Region.USE_PREF_SIZE);

                GridPane.setMargin(anchorPane, new Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onFruitClicked(Food food) {
        setChosenFruit(food);
    }

    private void setChosenFruit(Food food) {
        fruitNameLable.setText(food.getName());
        descriptionLabel.setText(food.getDescription());
        fruitPriceLabel.setText(CURRENCY + food.getPrice());
        fruitImg.setImage(loadImage(food.getImgSrc()));
        chosenFruitCard.setStyle("-fx-background-color: #" + food.getColor() + ";\n" +
                "    -fx-background-radius: 30;");
    }

    public static Image loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
                return new Image(imageUrl);
            } else if (imageUrl.startsWith("file:/")) {
                return new Image(imageUrl);
            } else {
                return new Image(Objects.requireNonNull(FoodController.class.getResourceAsStream("/com/cyber/client/assets/kiwi.png")));
            }
        } else {
            return new Image(Objects.requireNonNull(FoodController.class.getResourceAsStream("/com/cyber/client/assets/kiwi.png")));
        }
    }

    private void performSearch() {
        String searchQuery = searchTextField.getText().toLowerCase().trim(); // Get the search query and trim spaces
        if (searchQuery.isEmpty()) {
            // If the search query is empty, reload all food items
            foods.clear();
            foods.addAll(getDataFromDatabase());
        } else {
            // Filter foods based on the search query
            List<Food> filteredFoods = new ArrayList<>();
            for (Food food : getDataFromDatabase()) {
                // Check if the food name, description, price, or category contains the search query
                if (food.getName().toLowerCase().contains(searchQuery) ||
                        food.getDescription().toLowerCase().contains(searchQuery) ||
                        String.valueOf(food.getPrice()).contains(searchQuery) || // Check price
                        food.getCategoryName().toLowerCase().contains(searchQuery)) { // Check category name
                    filteredFoods.add(food);
                }
            }
            foods.clear();
            foods.addAll(filteredFoods);
        }
        refreshFoodGrid(); // Refresh the grid with the filtered foods
    }
}