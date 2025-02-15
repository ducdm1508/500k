package com.cyber.client.model;

public class Food {
    private String name;
    private String description;
    private String imgSrc;
    private double price;
    private FoodCategory category; // Assuming this is an object of FoodCategory
    private String color;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getColor() {
        return color;
    }

    public FoodCategory getCategory() {
        return category;
    }

    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // New method to get the category name
    public String getCategoryName() {
        return category != null ? category.getName() : "";
    }
}