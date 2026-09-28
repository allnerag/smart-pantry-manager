package com.example.smartpantrymanager;

public class PantryItem {
    private final long id;
    private final String name;
    private final double quantity;
    private final String unit;

    public PantryItem(long id, String name, double quantity, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }
}
