package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class PersonalRecipeRepository {
    private final PantryDatabaseHelper helper;

    public PersonalRecipeRepository(PantryDatabaseHelper helper) {
        this.helper = helper;
    }

    public boolean isPersonal(String recipeId) {
        return isPersonal(helper.getReadableDatabase(), recipeId);
    }

    private boolean isPersonal(SQLiteDatabase database, String recipeId) {
        try (Cursor cursor = database.rawQuery(
                "SELECT user_created FROM recipes WHERE recipe_id = ?", new String[]{recipeId})) {
            return cursor.moveToFirst() && cursor.getInt(0) == 1;
        }
    }

    public ArrayList<String> getIngredientNames() {
        ArrayList<String> names = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT name FROM recipe_ingredients ORDER BY name COLLATE NOCASE", null)) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0));
            }
        }
        return names;
    }

    // All recipe changes share one transaction, so a failed save keeps the old recipe intact.
    public String save(String recipeId, String title, int servings,
                       ArrayList<PantryItem> ingredients, ArrayList<String> steps) {
        validate(title, servings, ingredients, steps);
        return saveWithYield(recipeId, title, servings + (servings == 1 ? " serving" : " servings"), ingredients, steps);
    }

    public String saveWithYield(String recipeId, String title, String yieldText,
                               ArrayList<PantryItem> ingredients, ArrayList<String> steps) {
        validate(title, 1, ingredients, steps);
        validateYield(yieldText);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.beginTransaction();
        try {
            String savedId = recipeId;
            if (savedId == null) {
                savedId = "personal_" + UUID.randomUUID();
            } else {
                if (!isPersonal(database, savedId)) {
                    throw new IllegalArgumentException("Use the version editor to change an included recipe.");
                }
                removeVersions(database, savedId);
            }
            ContentValues recipe = new ContentValues();
            recipe.put("title", title.trim());
            if (recipeId == null) {
                recipe.put("recipe_id", savedId);
                recipe.put("user_created", 1);
                database.insertOrThrow("recipes", null, recipe);
            } else {
                database.update("recipes", recipe, "recipe_id = ?", new String[]{savedId});
            }
            String variantId = savedId + "_version";
            ContentValues variant = new ContentValues();
            variant.put("variant_id", variantId);
            variant.put("recipe_id", savedId);
            variant.put("label", "Your recipe");
            variant.put("yield_text", yieldText.trim());
            database.insertOrThrow("recipe_variants", null, variant);
            insertRequirements(database, variantId, ingredients);
            for (int index = 0; index < steps.size(); index++) {
                ContentValues step = new ContentValues();
                step.put("variant_id", variantId);
                step.put("step_number", index + 1);
                step.put("instruction", steps.get(index).trim());
                database.insertOrThrow("recipe_steps", null, step);
            }
            database.setTransactionSuccessful();
            return variantId;
        } finally {
            database.endTransaction();
        }
    }

    public void updateVersion(String variantId, String title, String yieldText,
                              ArrayList<PantryItem> ingredients, ArrayList<String> steps) {
        validate(title, 1, ingredients, steps);
        validateYield(yieldText);
        SQLiteDatabase database = helper.getWritableDatabase();
        database.beginTransaction();
        try {
            String recipeId;
            try (Cursor cursor = database.rawQuery("SELECT recipe_id FROM recipe_variants WHERE variant_id = ?",
                    new String[]{variantId})) {
                if (!cursor.moveToFirst()) {
                    throw new IllegalArgumentException("This recipe version is no longer available.");
                }
                recipeId = cursor.getString(0);
            }
            ContentValues recipe = new ContentValues();
            recipe.put("title", title.trim());
            database.update("recipes", recipe, "recipe_id = ?", new String[]{recipeId});
            ContentValues version = new ContentValues();
            version.put("yield_text", yieldText.trim());
            database.update("recipe_variants", version, "variant_id = ?", new String[]{variantId});
            // Replace only the selected version, preserving its ID, label and sibling versions.
            database.delete("recipe_requirements", "variant_id = ?", new String[]{variantId});
            database.delete("recipe_steps", "variant_id = ?", new String[]{variantId});
            insertRequirements(database, variantId, ingredients);
            for (int index = 0; index < steps.size(); index++) {
                ContentValues step = new ContentValues();
                step.put("variant_id", variantId);
                step.put("step_number", index + 1);
                step.put("instruction", steps.get(index).trim());
                database.insertOrThrow("recipe_steps", null, step);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    private void validateYield(String yieldText) {
        if (yieldText == null || !yieldText.trim().matches("[1-9][0-9]*\\s+\\S.*")) {
            throw new IllegalArgumentException("Enter the amount made, such as 2 servings or 12 muffins.");
        }
    }

    private void validate(String title, int servings, ArrayList<PantryItem> ingredients,
                          ArrayList<String> steps) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter a recipe name.");
        }
        if (servings < 1) {
            throw new IllegalArgumentException("Enter a whole number of servings greater than zero.");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Add at least one ingredient.");
        }
        for (PantryItem ingredient : ingredients) {
            if (RecipeMatcher.normalizeName(ingredient.getName()).isEmpty()) {
                throw new IllegalArgumentException("Enter a name for every ingredient.");
            }
            double quantity = ingredient.getQuantity();
            if (quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
                throw new IllegalArgumentException("Every ingredient needs a quantity greater than zero.");
            }
            baseUnit(ingredient.getUnit());
        }
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Add at least one preparation step.");
        }
        for (String step : steps) {
            if (step == null || step.trim().isEmpty()) {
                throw new IllegalArgumentException("Preparation steps cannot be blank.");
            }
        }
    }

    private void insertRequirements(SQLiteDatabase database, String variantId,
                                    ArrayList<PantryItem> ingredients) {
        HashSet<String> usedIds = new HashSet<>();
        for (PantryItem ingredient : ingredients) {
            String ingredientId = findOrCreateIngredient(database, ingredient);
            if (!usedIds.add(ingredientId)) {
                throw new IllegalArgumentException("Use one row per ingredient; combine duplicate amounts.");
            }
            BigDecimal quantity = BigDecimal.valueOf(ingredient.getQuantity());
            if ("kg".equals(ingredient.getUnit()) || "l".equals(ingredient.getUnit())) {
                quantity = quantity.multiply(BigDecimal.valueOf(1000));
            }
            if (Double.isInfinite(quantity.doubleValue())) {
                throw new IllegalArgumentException("That ingredient quantity is too large.");
            }
            ContentValues requirement = new ContentValues();
            requirement.put("variant_id", variantId);
            requirement.put("ingredient_id", ingredientId);
            requirement.put("quantity", quantity.doubleValue());
            requirement.put("unit", baseUnit(ingredient.getUnit()));
            database.insertOrThrow("recipe_requirements", null, requirement);
        }
    }

    private String findOrCreateIngredient(SQLiteDatabase database, PantryItem ingredient) {
        String name = RecipeMatcher.normalizeName(ingredient.getName());
        try (Cursor cursor = database.rawQuery(
                "SELECT ingredient_id FROM ingredient_aliases WHERE alias = ?", new String[]{name})) {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        }
        String ingredientId = "personal_" + UUID.randomUUID();
        ContentValues values = new ContentValues();
        values.put("ingredient_id", ingredientId);
        values.put("name", ingredient.getName().trim());
        values.put("unit", baseUnit(ingredient.getUnit()));
        database.insertOrThrow("recipe_ingredients", null, values);
        ContentValues alias = new ContentValues();
        alias.put("alias", name);
        alias.put("ingredient_id", ingredientId);
        database.insertOrThrow("ingredient_aliases", null, alias);
        return ingredientId;
    }

    private String baseUnit(String unit) {
        if ("g".equals(unit) || "kg".equals(unit)) {
            return "g";
        }
        if ("ml".equals(unit) || "l".equals(unit)) {
            return "ml";
        }
        if ("count".equals(unit)) {
            return "count";
        }
        throw new IllegalArgumentException("Choose g, kg, ml, l or count.");
    }

    public boolean delete(String recipeId) {
        SQLiteDatabase database = helper.getWritableDatabase();
        database.beginTransaction();
        try {
            if (!isPersonal(database, recipeId)) {
                return false;
            }
            removeVersions(database, recipeId);
            database.delete("recipes", "recipe_id = ?", new String[]{recipeId});
            database.setTransactionSuccessful();
            return true;
        } finally {
            database.endTransaction();
        }
    }

    private void removeVersions(SQLiteDatabase database, String recipeId) {
        String selection = "variant_id IN (SELECT variant_id FROM recipe_variants WHERE recipe_id = ?)";
        database.delete("recipe_steps", selection, new String[]{recipeId});
        database.delete("recipe_requirements", selection, new String[]{recipeId});
        database.delete("recipe_variants", "recipe_id = ?", new String[]{recipeId});
    }
}
