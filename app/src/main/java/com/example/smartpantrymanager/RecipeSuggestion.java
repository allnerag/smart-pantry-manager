package com.example.smartpantrymanager;

import java.util.ArrayList;

public class RecipeSuggestion {
    private final String recipeId;
    private final String title;
    private final ArrayList<RecipeVariant> versions = new ArrayList<>();

    public RecipeSuggestion(String recipeId, String title) {
        this.recipeId = recipeId;
        this.title = title;
    }

    public String getRecipeId() {
        return recipeId;
    }
    public String getTitle() {
        return title;
    }
    public ArrayList<RecipeVariant> getVersions() {
        return new ArrayList<>(versions);
    }

    public void addVersion(RecipeVariant version) {
        versions.add(version);
    }
}
