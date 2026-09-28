package com.example.smartpantrymanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class RecipeRepository {
    private final PantryDatabaseHelper databaseHelper;

    public RecipeRepository(PantryDatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public ArrayList<RecipeSuggestion> getSuggestions() {
        RecipeMatcher matcher = new RecipeMatcher(readAliases());
        return matcher.findMatches(databaseHelper.getAllIngredients(), readVariants());
    }

    public RecipeDetail getRecipeDetail(String variantId) {
        if (variantId == null || variantId.trim().isEmpty()) {
            return null;
        }
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String query = "SELECT v.variant_id, r.recipe_id, r.title, v.label, v.yield_text "
                + "FROM recipe_variants v JOIN recipes r ON r.recipe_id = v.recipe_id "
                + "WHERE v.variant_id = ?";
        try (Cursor cursor = database.rawQuery(query, new String[]{variantId})) {
            if (!cursor.moveToFirst()) {
                return null;
            }
            RecipeVariant version = new RecipeVariant(cursor.getString(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3), cursor.getString(4),
                    readRequirements(variantId));
            ArrayList<String> steps = new ArrayList<>();
            try (Cursor stepCursor = database.rawQuery(
                    "SELECT instruction FROM recipe_steps WHERE variant_id = ? ORDER BY step_number",
                    new String[]{variantId})) {
                while (stepCursor.moveToNext()) {
                    steps.add(stepCursor.getString(0));
                }
            }
            return new RecipeDetail(version, steps);
        }
    }

    public boolean isVersionAvailable(RecipeVariant version) {
        ArrayList<RecipeVariant> versions = new ArrayList<>();
        versions.add(version);
        RecipeMatcher matcher = new RecipeMatcher(readAliases());
        return !matcher.findMatches(databaseHelper.getAllIngredients(), versions).isEmpty();
    }

    private HashMap<String, String> readAliases() {
        HashMap<String, String> aliases = new HashMap<>();
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try (Cursor cursor = database.rawQuery("SELECT alias, ingredient_id FROM ingredient_aliases", null)) {
            while (cursor.moveToNext()) {
                aliases.put(cursor.getString(0), cursor.getString(1));
            }
        }
        return aliases;
    }

    private ArrayList<RecipeVariant> readVariants() {
        ArrayList<RecipeVariant> variants = new ArrayList<>();
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String query = "SELECT v.variant_id, r.recipe_id, r.title, v.label, v.yield_text "
                + "FROM recipe_variants v JOIN recipes r ON r.recipe_id = v.recipe_id "
                + "ORDER BY r.title COLLATE NOCASE, v.variant_id";
        try (Cursor cursor = database.rawQuery(query, null)) {
            while (cursor.moveToNext()) {
                String variantId = cursor.getString(0);
                variants.add(new RecipeVariant(variantId, cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        readRequirements(variantId)));
            }
        }
        return variants;
    }

    private ArrayList<RecipeRequirement> readRequirements(String variantId) {
        ArrayList<RecipeRequirement> requirements = new ArrayList<>();
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        try (Cursor cursor = database.rawQuery(
                "SELECT q.ingredient_id, q.quantity, q.unit, i.name FROM recipe_requirements q "
                        + "JOIN recipe_ingredients i ON i.ingredient_id = q.ingredient_id "
                        + "WHERE q.variant_id = ? ORDER BY i.name COLLATE NOCASE",
                new String[]{variantId})) {
            while (cursor.moveToNext()) {
                requirements.add(new RecipeRequirement(cursor.getString(0), cursor.getString(3),
                        cursor.getDouble(1), cursor.getString(2)));
            }
        }
        return requirements;
    }
}
