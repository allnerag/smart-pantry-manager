package com.example.smartpantrymanager;

import java.util.ArrayList;

public class RecipeDetail {
    private final RecipeVariant version;
    private final ArrayList<String> steps;

    public RecipeDetail(RecipeVariant version, ArrayList<String> steps) {
        this.version = version;
        this.steps = new ArrayList<>(steps);
    }

    public RecipeVariant getVersion() {
        return version;
    }

    public ArrayList<String> getSteps() {
        return new ArrayList<>(steps);
    }
}
