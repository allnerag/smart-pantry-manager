package com.example.smartpantrymanager;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RecipeCollectionTest {
    private JSONObject collection;

    @Before
    public void readCollection() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        StringBuilder jsonText = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open("recipes.json"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
        }
        collection = new JSONObject(jsonText.toString());
    }

    @Test
    public void containsTwentyTitlesAndTwentyTwoCompleteVersions() throws Exception {
        JSONArray recipes = collection.getJSONArray("recipes");
        assertEquals(20, recipes.length());
        HashSet<String> recipeIds = new HashSet<>();
        HashSet<String> titles = new HashSet<>();
        HashSet<String> versionIds = new HashSet<>();
        for (int index = 0; index < recipes.length(); index++) {
            JSONObject recipe = recipes.getJSONObject(index);
            assertTrue(recipeIds.add(recipe.getString("id")));
            assertTrue(titles.add(recipe.getString("title")));
            JSONArray versions = recipe.getJSONArray("variants");
            assertTrue(versions.length() > 0);
            for (int versionIndex = 0; versionIndex < versions.length(); versionIndex++) {
                JSONObject version = versions.getJSONObject(versionIndex);
                assertTrue(versionIds.add(version.getString("id")));
                assertFalse(version.getString("label").trim().isEmpty());
                assertFalse(version.getString("yield").trim().isEmpty());
                JSONArray steps = version.getJSONArray("steps");
                assertTrue(steps.length() >= 2);
                for (int stepIndex = 0; stepIndex < steps.length(); stepIndex++) {
                    assertFalse(steps.getString(stepIndex).trim().isEmpty());
                }
            }
        }
        assertEquals(22, versionIds.size());
    }

    @Test
    public void requirementsUseKnownIngredientsAndPositiveQuantities() throws Exception {
        HashSet<String> ingredientIds = new HashSet<>();
        JSONArray ingredients = collection.getJSONArray("ingredients");
        for (int index = 0; index < ingredients.length(); index++) {
            assertTrue(ingredientIds.add(ingredients.getJSONObject(index).getString("id")));
        }
        JSONArray recipes = collection.getJSONArray("recipes");
        for (int index = 0; index < recipes.length(); index++) {
            JSONArray versions = recipes.getJSONObject(index).getJSONArray("variants");
            for (int versionIndex = 0; versionIndex < versions.length(); versionIndex++) {
                JSONArray requirements = versions.getJSONObject(versionIndex).getJSONArray("ingredients");
                assertTrue(requirements.length() > 0);
                HashSet<String> usedIngredients = new HashSet<>();
                for (int requirementIndex = 0; requirementIndex < requirements.length(); requirementIndex++) {
                    JSONObject requirement = requirements.getJSONObject(requirementIndex);
                    String ingredientId = requirement.getString("ingredientId");
                    assertTrue(ingredientIds.contains(ingredientId));
                    assertTrue(usedIngredients.add(ingredientId));
                    double quantity = requirement.getDouble("quantity");
                    assertTrue(quantity > 0 && !Double.isInfinite(quantity) && !Double.isNaN(quantity));
                    String unit = requirement.getString("unit");
                    assertTrue(unit.equals("g") || unit.equals("ml") || unit.equals("count"));
                    if (unit.equals("count")) {
                        assertEquals(Math.floor(quantity), quantity, 0);
                    }
                }
            }
        }
    }

    @Test
    public void ingredientNamesAndAliasesAreUnambiguous() throws Exception {
        HashSet<String> names = new HashSet<>();
        JSONArray ingredients = collection.getJSONArray("ingredients");
        for (int index = 0; index < ingredients.length(); index++) {
            JSONObject ingredient = ingredients.getJSONObject(index);
            assertTrue(names.add(ingredient.getString("name").trim().toLowerCase(Locale.ROOT)));
            JSONArray aliases = ingredient.getJSONArray("aliases");
            for (int aliasIndex = 0; aliasIndex < aliases.length(); aliasIndex++) {
                String alias = aliases.getString(aliasIndex).trim().toLowerCase(Locale.ROOT);
                assertFalse(alias.isEmpty());
                assertTrue("Duplicate alias: " + alias, names.add(alias));
            }
        }
    }

    @Test
    public void papAndMashHaveSeparateRawAndPreparedRequirements() throws Exception {
        assertVersionContains("pap_wors", "pap_raw", "maize_meal", "cooked_pap");
        assertVersionContains("pap_wors", "pap_prepared", "cooked_pap", "maize_meal");
        assertVersionContains("mash_wors", "mash_raw", "potato", "prepared_mash");
        assertVersionContains("mash_wors", "mash_prepared", "prepared_mash", "potato");
    }

    private void assertVersionContains(String recipeId, String versionId,
                                       String requiredId, String excludedId) throws Exception {
        JSONArray recipes = collection.getJSONArray("recipes");
        for (int index = 0; index < recipes.length(); index++) {
            JSONObject recipe = recipes.getJSONObject(index);
            if (recipe.getString("id").equals(recipeId)) {
                JSONArray versions = recipe.getJSONArray("variants");
                for (int versionIndex = 0; versionIndex < versions.length(); versionIndex++) {
                    JSONObject version = versions.getJSONObject(versionIndex);
                    if (version.getString("id").equals(versionId)) {
                        JSONArray requirements = version.getJSONArray("ingredients");
                        HashSet<String> ingredientIds = new HashSet<>();
                        for (int requirementIndex = 0; requirementIndex < requirements.length(); requirementIndex++) {
                            ingredientIds.add(requirements.getJSONObject(requirementIndex).getString("ingredientId"));
                        }
                        assertTrue(ingredientIds.contains(requiredId));
                        assertFalse(ingredientIds.contains(excludedId));
                        return;
                    }
                }
            }
        }
        fail("Missing recipe version: " + versionId);
    }
}
