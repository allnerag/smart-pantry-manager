package com.example.smartpantrymanager;

public class RecipeRequirement {
    private final String ingredientId;
    private final String ingredientName;
    private final double quantity;
    private final String unit;

    public RecipeRequirement(String ingredientId, double quantity, String unit) {
        this(ingredientId, ingredientId, quantity, unit);
    }

    public RecipeRequirement(String ingredientId, String ingredientName, double quantity, String unit) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getIngredientId() {
        return ingredientId;
    }
    public String getIngredientName() {
        return ingredientName;
    }
    public double getQuantity() {
        return quantity;
    }
    public String getUnit() {
        return unit;
    }
}
