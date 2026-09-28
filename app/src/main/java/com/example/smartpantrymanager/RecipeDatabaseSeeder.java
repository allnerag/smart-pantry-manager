package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class RecipeDatabaseSeeder {
    private RecipeDatabaseSeeder() {
    }

    static void createAndSeed(SQLiteDatabase database, Context context) {
        // SQLiteOpenHelper runs onCreate/onUpgrade in a transaction. Any failure rolls back
        // all these changes and leaves the previous database version available for retry.
        try {
            JSONObject collection = readCollection(context);
            if (collection.getInt("schemaVersion") != 1 || collection.getInt("collectionVersion") != 1) {
                throw new SQLiteException("Unsupported recipe collection version.");
            }
            createTables(database);
            insertIngredients(database, collection.getJSONArray("ingredients"));
            insertRecipes(database, collection.getJSONArray("recipes"));
            ContentValues version = new ContentValues();
            version.put("collection_version", collection.getInt("collectionVersion"));
            database.insertOrThrow("recipe_collection", null, version);
        } catch (IOException | JSONException exception) {
            SQLiteException databaseException = new SQLiteException("Could not load the recipe collection.");
            databaseException.initCause(exception);
            throw databaseException;
        }
    }

    private static JSONObject readCollection(Context context) throws IOException, JSONException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("recipes.json"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }
        }
        return new JSONObject(text.toString());
    }

    private static void createTables(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE recipe_ingredients ("
                + "ingredient_id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, "
                + "unit TEXT NOT NULL CHECK(unit IN ('g', 'ml', 'count')))" );
        database.execSQL("CREATE TABLE ingredient_aliases ("
                + "alias TEXT PRIMARY KEY NOT NULL, ingredient_id TEXT NOT NULL "
                + "REFERENCES recipe_ingredients(ingredient_id))");
        database.execSQL("CREATE TABLE recipes ("
                + "recipe_id TEXT PRIMARY KEY NOT NULL, title TEXT NOT NULL)");
        database.execSQL("CREATE TABLE recipe_variants ("
                + "variant_id TEXT PRIMARY KEY NOT NULL, recipe_id TEXT NOT NULL REFERENCES recipes(recipe_id), "
                + "label TEXT NOT NULL, yield_text TEXT NOT NULL)");
        database.execSQL("CREATE TABLE recipe_requirements ("
                + "variant_id TEXT NOT NULL REFERENCES recipe_variants(variant_id), "
                + "ingredient_id TEXT NOT NULL REFERENCES recipe_ingredients(ingredient_id), "
                + "quantity REAL NOT NULL CHECK(quantity > 0), "
                + "unit TEXT NOT NULL CHECK(unit IN ('g', 'ml', 'count')), "
                + "PRIMARY KEY(variant_id, ingredient_id))");
        database.execSQL("CREATE TABLE recipe_steps ("
                + "variant_id TEXT NOT NULL REFERENCES recipe_variants(variant_id), "
                + "step_number INTEGER NOT NULL CHECK(step_number > 0), instruction TEXT NOT NULL, "
                + "PRIMARY KEY(variant_id, step_number))");
        database.execSQL("CREATE TABLE recipe_collection (collection_version INTEGER PRIMARY KEY)");
    }

    private static void insertIngredients(SQLiteDatabase database, JSONArray ingredients) throws JSONException {
        for (int index = 0; index < ingredients.length(); index++) {
            JSONObject ingredient = ingredients.getJSONObject(index);
            String ingredientId = ingredient.getString("id");
            ContentValues values = new ContentValues();
            values.put("ingredient_id", ingredientId);
            values.put("name", ingredient.getString("name"));
            values.put("unit", ingredient.getString("unit"));
            database.insertOrThrow("recipe_ingredients", null, values);

            insertAlias(database, ingredient.getString("name"), ingredientId);
            JSONArray aliases = ingredient.getJSONArray("aliases");
            for (int aliasIndex = 0; aliasIndex < aliases.length(); aliasIndex++) {
                insertAlias(database, aliases.getString(aliasIndex), ingredientId);
            }
        }
    }

    private static void insertAlias(SQLiteDatabase database, String alias, String ingredientId) {
        ContentValues values = new ContentValues();
        values.put("alias", alias.trim().toLowerCase(Locale.ROOT));
        values.put("ingredient_id", ingredientId);
        database.insertOrThrow("ingredient_aliases", null, values);
    }

    private static void insertRecipes(SQLiteDatabase database, JSONArray recipes) throws JSONException {
        for (int index = 0; index < recipes.length(); index++) {
            JSONObject recipe = recipes.getJSONObject(index);
            String recipeId = recipe.getString("id");
            ContentValues values = new ContentValues();
            values.put("recipe_id", recipeId);
            values.put("title", recipe.getString("title"));
            database.insertOrThrow("recipes", null, values);

            JSONArray variants = recipe.getJSONArray("variants");
            for (int variantIndex = 0; variantIndex < variants.length(); variantIndex++) {
                insertVariant(database, recipeId, variants.getJSONObject(variantIndex));
            }
        }
    }

    private static void insertVariant(SQLiteDatabase database, String recipeId,
                                      JSONObject variant) throws JSONException {
        String variantId = variant.getString("id");
        ContentValues values = new ContentValues();
        values.put("variant_id", variantId);
        values.put("recipe_id", recipeId);
        values.put("label", variant.getString("label"));
        values.put("yield_text", variant.getString("yield"));
        database.insertOrThrow("recipe_variants", null, values);

        JSONArray requirements = variant.getJSONArray("ingredients");
        for (int index = 0; index < requirements.length(); index++) {
            JSONObject requirement = requirements.getJSONObject(index);
            double quantity = requirement.getDouble("quantity");
            if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
                throw new SQLiteException("Invalid recipe quantity.");
            }
            ContentValues requirementValues = new ContentValues();
            requirementValues.put("variant_id", variantId);
            requirementValues.put("ingredient_id", requirement.getString("ingredientId"));
            requirementValues.put("quantity", quantity);
            requirementValues.put("unit", requirement.getString("unit"));
            database.insertOrThrow("recipe_requirements", null, requirementValues);
        }

        JSONArray steps = variant.getJSONArray("steps");
        for (int index = 0; index < steps.length(); index++) {
            ContentValues stepValues = new ContentValues();
            stepValues.put("variant_id", variantId);
            stepValues.put("step_number", index + 1);
            stepValues.put("instruction", steps.getString(index));
            database.insertOrThrow("recipe_steps", null, stepValues);
        }
    }
}
