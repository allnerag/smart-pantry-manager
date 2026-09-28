package com.example.smartpantrymanager;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PantryDatabaseHelperTest {
    private static final String TEST_DATABASE_NAME = "pantry_storage_test.db";
    private Context context;
    private PantryDatabaseHelper databaseHelper;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE_NAME);
        databaseHelper = new PantryDatabaseHelper(context, TEST_DATABASE_NAME);
    }

    @After
    public void tearDown() {
        databaseHelper.close();
        context.deleteDatabase(TEST_DATABASE_NAME);
    }

    @Test
    public void newPantryIsEmpty() {
        assertTrue(databaseHelper.getAllIngredients().isEmpty());
    }

    @Test
    public void ingredientSurvivesClosingAndReopeningDatabase() {
        long ingredientId = databaseHelper.addIngredient(" Rice ", 1.5, "kg");
        databaseHelper.close();
        databaseHelper = new PantryDatabaseHelper(context, TEST_DATABASE_NAME);

        PantryItem ingredient = databaseHelper.getIngredient(ingredientId);
        assertNotNull(ingredient);
        assertEquals(ingredientId, ingredient.getId());
        assertEquals("Rice", ingredient.getName());
        assertEquals(1.5, ingredient.getQuantity(), 0.000001);
        assertEquals("kg", ingredient.getUnit());
    }

    @Test
    public void updateAndDeleteOnlyAffectSelectedIngredient() {
        long firstId = databaseHelper.addIngredient("Rice", 500, "g");
        long secondId = databaseHelper.addIngredient("Rice", 1, "kg");

        assertTrue(databaseHelper.updateIngredient(firstId, "Brown rice", 250, "g"));
        PantryItem updatedIngredient = databaseHelper.getIngredient(firstId);
        assertEquals("Brown rice", updatedIngredient.getName());
        assertEquals(250, updatedIngredient.getQuantity(), 0.000001);
        assertEquals("g", updatedIngredient.getUnit());
        assertEquals(1, databaseHelper.getIngredient(secondId).getQuantity(), 0.000001);

        assertTrue(databaseHelper.deleteIngredient(firstId));
        assertNull(databaseHelper.getIngredient(firstId));
        assertNotNull(databaseHelper.getIngredient(secondId));
        assertFalse(databaseHelper.deleteIngredient(firstId));
        assertFalse(databaseHelper.updateIngredient(firstId, "Rice", 100, "g"));
        assertEquals(1, databaseHelper.getAllIngredients().size());
    }

    @Test
    public void namesWithApostrophesAreStoredAndSorted() {
        databaseHelper.addIngredient("Tomatoes", 2, "count");
        databaseHelper.addIngredient("baker's flour", 500, "g");

        ArrayList<PantryItem> ingredients = databaseHelper.getAllIngredients();
        assertEquals("baker's flour", ingredients.get(0).getName());
        assertEquals("Tomatoes", ingredients.get(1).getName());
    }

    @Test
    public void invalidInputDoesNotAddRows() {
        assertInvalidIngredient(null, 1, "g");
        assertInvalidIngredient("   ", 1, "g");
        assertInvalidIngredient("Rice", 0, "g");
        assertInvalidIngredient("Rice", -1, "g");
        assertInvalidIngredient("Rice", Double.NaN, "g");
        assertInvalidIngredient("Rice", Double.POSITIVE_INFINITY, "g");
        assertInvalidIngredient("Rice", 1, "cups");
        assertInvalidIngredient("Rice", 1, null);
        assertTrue(databaseHelper.getAllIngredients().isEmpty());
    }

    @Test
    public void invalidUpdatePreservesSavedIngredient() {
        long ingredientId = databaseHelper.addIngredient("Milk", 500, "ml");
        try {
            databaseHelper.updateIngredient(ingredientId, "Milk", -10, "ml");
            fail("An invalid quantity should be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals(500, databaseHelper.getIngredient(ingredientId).getQuantity(), 0.000001);
        }
    }

    private void assertInvalidIngredient(String name, double quantity, String unit) {
        try {
            databaseHelper.addIngredient(name, quantity, unit);
            fail("Invalid ingredient data should be rejected.");
        } catch (IllegalArgumentException expected) {
            // The invalid input was rejected before a row was inserted.
        }
    }
}
