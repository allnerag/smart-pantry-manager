package com.example.smartpantrymanager;

public class RecipeRequirement {
    private final String ingredientId;
    private final double quantity;
    private final String unit;

    public RecipeRequirement(String ingredientId, double quantity, String unit) {
        this.ingredientId = ingredientId;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getIngredientId() {
        return ingredientId;
    }
    public double getQuantity() {
        return quantity;
    }
    public String getUnit() {
        return unit;
    }
}
