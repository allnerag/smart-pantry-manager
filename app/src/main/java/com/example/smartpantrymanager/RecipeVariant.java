package com.example.smartpantrymanager;

import java.util.ArrayList;

public class RecipeVariant {
    private final String id;
    private final String recipeId;
    private final String title;
    private final String label;
    private final String yieldText;
    private final ArrayList<RecipeRequirement> requirements;

    public RecipeVariant(String id, String recipeId, String title, String label,
                         String yieldText, ArrayList<RecipeRequirement> requirements) {
        this.id = id;
        this.recipeId = recipeId;
        this.title = title;
        this.label = label;
        this.yieldText = yieldText;
        this.requirements = new ArrayList<>(requirements);
    }

    public String getId() {
        return id;
    }
    public String getRecipeId() {
        return recipeId;
    }
    public String getTitle() {
        return title;
    }
    public String getLabel() {
        return label;
    }
    public String getYieldText() {
        return yieldText;
    }
    public ArrayList<RecipeRequirement> getRequirements() {
        return new ArrayList<>(requirements);
    }
}
