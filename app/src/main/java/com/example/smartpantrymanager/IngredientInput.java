package com.example.smartpantrymanager;

public class IngredientInput {
    private IngredientInput() {
    }

    public static double parseQuantity(String enteredQuantity) {
        if (enteredQuantity == null) {
            throw new IllegalArgumentException("A quantity is required.");
        }

        // Accept a decimal point or comma, but no grouping separators or exponent notation.
        String quantityText = enteredQuantity.trim().replace(',', '.');
        if (!quantityText.matches("[0-9]*\\.?[0-9]+")) {
            throw new IllegalArgumentException("Enter a positive decimal number.");
        }

        double quantity = Double.parseDouble(quantityText);
        if (Double.isInfinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive and finite.");
        }
        return quantity;
    }
}
