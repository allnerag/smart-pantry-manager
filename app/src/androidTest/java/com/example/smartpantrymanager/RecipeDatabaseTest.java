package com.example.smartpantrymanager;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RecipeDatabaseTest {
    private static final String TEST_DATABASE = "recipe_database_test.db";
    private Context context;
    private PantryDatabaseHelper helper;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        helper = new PantryDatabaseHelper(context, TEST_DATABASE);
    }

    @After
    public void tearDown() {
        helper.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void freshDatabaseContainsEntireCollectionAndNoPantryItems() {
        SQLiteDatabase database = helper.getReadableDatabase();
        assertCollectionCounts(database);
        assertTrue(helper.getAllIngredients().isEmpty());
        assertEquals(2, database.getVersion());
        try (Cursor errors = database.rawQuery("PRAGMA foreign_key_check", null)) {
            assertFalse(errors.moveToFirst());
        }
        assertEquals("boerewors", DatabaseUtils.stringForQuery(database,
                "SELECT ingredient_id FROM ingredient_aliases WHERE alias = ?", new String[]{"wors"}));
        assertEquals("cooked_pap", DatabaseUtils.stringForQuery(database,
                "SELECT ingredient_id FROM ingredient_aliases WHERE alias = ?", new String[]{"pap"}));
        assertEquals("maize_meal", DatabaseUtils.stringForQuery(database,
                "SELECT ingredient_id FROM ingredient_aliases WHERE alias = ?", new String[]{"maize meal"}));
    }

    @Test
    public void reopeningDoesNotReimportRecipesOrChangePantry() {
        long pantryId = helper.addIngredient("Rice", 250, "g");
        SQLiteDatabase database = helper.getWritableDatabase();
        database.execSQL("UPDATE recipes SET title = ? WHERE recipe_id = ?",
                new Object[]{"Stored title", "pap_wors"});
        helper.close();
        helper = new PantryDatabaseHelper(context, TEST_DATABASE);

        assertCollectionCounts(helper.getReadableDatabase());
        assertEquals("Stored title", DatabaseUtils.stringForQuery(helper.getReadableDatabase(),
                "SELECT title FROM recipes WHERE recipe_id = ?", new String[]{"pap_wors"}));
        assertEquals(250, helper.getIngredient(pantryId).getQuantity(), 0.000001);
    }

    @Test
    public void upgradePreservesOldPantryIdsAndQuantities() {
        createVersionOneDatabase(false);
        SQLiteDatabase upgradedDatabase = helper.getWritableDatabase();
        assertCollectionCounts(upgradedDatabase);
        assertEquals(2, upgradedDatabase.getVersion());
        PantryItem ingredient = helper.getIngredient(91);
        assertNotNull(ingredient);
        assertEquals("Rice", ingredient.getName());
        assertEquals(0.25, ingredient.getQuantity(), 0.000001);
        assertEquals("kg", ingredient.getUnit());
        assertTrue(helper.addIngredient("Eggs", 2, "count") > 91);
    }

    @Test
    public void failedUpgradeRollsBackAndCanRetry() {
        // A conflicting table forces a failure after the first new table is created.
        createVersionOneDatabase(true);
        try {
            helper.getWritableDatabase();
            fail("The conflicting table should stop the upgrade.");
        } catch (SQLiteException expected) {
            helper.close();
        }
        try (SQLiteDatabase database = context.openOrCreateDatabase(TEST_DATABASE, 0, null)) {
            assertEquals(1, database.getVersion());
            assertEquals(1, DatabaseUtils.queryNumEntries(database, "pantry_items"));
            assertEquals(0, DatabaseUtils.longForQuery(database,
                    "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'recipe_ingredients'", null));
            database.execSQL("DROP TABLE ingredient_aliases");
        }
        helper = new PantryDatabaseHelper(context, TEST_DATABASE);
        assertCollectionCounts(helper.getReadableDatabase());
        assertNotNull(helper.getIngredient(91));
    }

    @Test
    public void requirementsAndStepsStayLinkedToTheirVersion() {
        SQLiteDatabase database = helper.getReadableDatabase();
        assertEquals(200, DatabaseUtils.longForQuery(database,
                "SELECT quantity FROM recipe_requirements WHERE variant_id = ? AND ingredient_id = ?",
                new String[]{"pap_raw", "maize_meal"}));
        assertEquals(0, DatabaseUtils.longForQuery(database,
                "SELECT COUNT(*) FROM recipe_requirements WHERE variant_id = ? AND ingredient_id = ?",
                new String[]{"pap_prepared", "maize_meal"}));
        try (Cursor steps = database.rawQuery(
                "SELECT step_number, instruction FROM recipe_steps WHERE variant_id = ? ORDER BY step_number",
                new String[]{"pap_raw"})) {
            int expectedNumber = 1;
            while (steps.moveToNext()) {
                assertEquals(expectedNumber, steps.getInt(0));
                assertFalse(steps.getString(1).isEmpty());
                expectedNumber++;
            }
            assertEquals(4, expectedNumber);
        }
    }

    private void assertCollectionCounts(SQLiteDatabase database) {
        assertEquals(20, DatabaseUtils.queryNumEntries(database, "recipes"));
        assertEquals(22, DatabaseUtils.queryNumEntries(database, "recipe_variants"));
        assertEquals(43, DatabaseUtils.queryNumEntries(database, "recipe_ingredients"));
        assertEquals(115, DatabaseUtils.queryNumEntries(database, "ingredient_aliases"));
        assertEquals(136, DatabaseUtils.queryNumEntries(database, "recipe_requirements"));
        assertEquals(77, DatabaseUtils.queryNumEntries(database, "recipe_steps"));
        assertEquals(1, DatabaseUtils.queryNumEntries(database, "recipe_collection"));
    }

    private void createVersionOneDatabase(boolean addConflictingTable) {
        helper.close();
        try (SQLiteDatabase database = context.openOrCreateDatabase(TEST_DATABASE, 0, null)) {
            database.execSQL("CREATE TABLE pantry_items ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL CHECK(length(trim(name)) > 0), "
                    + "quantity REAL NOT NULL CHECK(quantity > 0), "
                    + "unit TEXT NOT NULL CHECK(unit IN ('g', 'kg', 'ml', 'l', 'count')))");
            database.execSQL("INSERT INTO pantry_items (_id, name, quantity, unit) VALUES (91, 'Rice', 0.25, 'kg')");
            if (addConflictingTable) {
                database.execSQL("CREATE TABLE ingredient_aliases (test_value TEXT)");
            }
            database.setVersion(1);
        }
        helper = new PantryDatabaseHelper(context, TEST_DATABASE);
    }
}
