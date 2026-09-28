package com.example.smartpantrymanager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PersonalRecipeTest {
    private static final String DATABASE = "personal_recipe_test.db";
    private Context context;
    private PantryDatabaseHelper helper;
    private RecipeRepository recipes;
    private PersonalRecipeRepository personal;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(DATABASE);
        openHelper();
    }

    private void openHelper() {
        helper = new PantryDatabaseHelper(context, DATABASE);
        recipes = new RecipeRepository(helper);
        personal = new PersonalRecipeRepository(helper);
    }

    @After
    public void tearDown() {
        helper.close();
        context.deleteDatabase(DATABASE);
    }

    @Test
    public void collectionIncludesUnavailableRecipesAndBothPapVersions() {
        assertTrue(recipes.getSuggestions().isEmpty());
        assertEquals(20, recipes.getCollection().size());
        int versionCount = 0;
        for (RecipeSuggestion recipe : recipes.getCollection()) {
            versionCount += recipe.getVersions().size();
            if (recipe.getRecipeId().equals("pap_wors")) {
                assertEquals(2, recipe.getVersions().size());
            }
            for (RecipeVariant version : recipe.getVersions()) {
                assertNotNull(recipes.getRecipeDetail(version.getId()));
            }
        }
        assertEquals(22, versionCount);
    }

    @Test
    public void newIngredientRecipePersistsAndMatchesExactQuantity() {
        String variantId = personal.save(null, "My snack", 2,
                ingredients("Test crackers", 0.2, "kg"), steps());
        assertEquals(21, recipes.getCollection().size());
        assertTrue(recipes.getSuggestions().isEmpty());
        long pantryId = helper.addIngredient("test CRACKERS", 199, "g");
        assertTrue(recipes.getSuggestions().isEmpty());
        helper.updateIngredient(pantryId, "test crackers", 200, "g");
        assertEquals(1, recipes.getSuggestions().size());
        helper.close();
        openHelper();
        RecipeDetail detail = recipes.getRecipeDetail(variantId);
        assertEquals("My snack", detail.getVersion().getTitle());
        assertEquals("2 servings", detail.getVersion().getYieldText());
        assertEquals(steps(), detail.getSteps());
        assertTrue(personal.isPersonal(detail.getVersion().getRecipeId()));
        assertTrue(recipes.isVersionAvailable(detail.getVersion()));
        helper.updateIngredient(pantryId, "test crackers", 200, "ml");
        assertFalse(recipes.isVersionAvailable(detail.getVersion()));
    }

    @Test
    public void editReplacesRequirementsAndDeletePreservesPantryAndOtherRecipes() {
        String variantId = personal.save(null, "Wors", 1, ingredients("wors", 300, "g"), steps());
        String recipeId = recipes.getRecipeDetail(variantId).getVersion().getRecipeId();
        long pantryId = helper.addIngredient("Boerewors", 300, "g");
        assertTrue(recipes.isVersionAvailable(recipes.getRecipeDetail(variantId).getVersion()));
        assertEquals(variantId, personal.save(recipeId, "More wors", 3,
                ingredients("Boerewors", 0.6, "kg"), new ArrayList<>(Arrays.asList("New step."))));
        RecipeDetail edited = recipes.getRecipeDetail(variantId);
        assertEquals("More wors", edited.getVersion().getTitle());
        assertEquals(1, edited.getSteps().size());
        assertEquals("New step.", edited.getSteps().get(0));
        assertEquals(600, edited.getVersion().getRequirements().get(0).getQuantity(), 0);
        assertFalse(recipes.isVersionAvailable(edited.getVersion()));
        assertTrue(personal.delete(recipeId));
        assertNull(recipes.getRecipeDetail(variantId));
        assertEquals(20, recipes.getCollection().size());
        assertEquals(300, helper.getIngredient(pantryId).getQuantity(), 0);
        assertNotNull(recipes.getRecipeDetail("pap_raw"));
        assertFalse(personal.delete(recipeId));
        assertNoBrokenLinks();
    }

    @Test
    public void duplicateAliasesRollBackAndWholeRecipeReplacementProtectsIncludedVersions() {
        String variantId = personal.save(null, "Original", 1, ingredients("wors", 300, "g"), steps());
        String recipeId = recipes.getRecipeDetail(variantId).getVersion().getRecipeId();
        ArrayList<PantryItem> duplicates = ingredients("wors", 300, "g");
        duplicates.add(new PantryItem(0, "boerewors", 100, "g"));
        try {
            personal.save(recipeId, "Bad edit", 1, duplicates, steps());
            fail("Duplicate aliases must be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals("Original", recipes.getRecipeDetail(variantId).getVersion().getTitle());
            assertEquals(300, recipes.getRecipeDetail(variantId).getVersion().getRequirements().get(0).getQuantity(), 0);
        }
        assertFalse(personal.delete("pap_wors"));
        try {
            personal.save("pap_wors", "Changed", 1, ingredients("wors", 1, "g"), steps());
            fail("Whole-recipe replacement must not remove included versions.");
        } catch (IllegalArgumentException expected) {
            assertEquals("Pap and wors", recipes.getRecipeDetail("pap_raw").getVersion().getTitle());
        }
        assertNoBrokenLinks();
    }

    @Test
    public void invalidRecipeCannotLeavePartialData() {
        ArrayList<PantryItem> duplicateNewNames = ingredients("New test ingredient", 1, "g");
        duplicateNewNames.add(new PantryItem(0, "NEW TEST INGREDIENT", 2, "g"));
        try {
            personal.save(null, "Invalid", 1, duplicateNewNames, steps());
            fail("Duplicate ingredient must be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals(20, recipes.getCollection().size());
            assertFalse(personal.getIngredientNames().contains("New test ingredient"));
        }
        try {
            personal.save(null, "Missing steps", 1, ingredients("wors", 1, "g"), new ArrayList<String>());
            fail("Steps are required.");
        } catch (IllegalArgumentException expected) {
            assertEquals(20, recipes.getCollection().size());
        }
        assertNoBrokenLinks();
    }

    @Test
    public void versionTwoUpgradePreservesPantryAndStoredRecipeDetails() {
        helper.close();
        try (SQLiteDatabase database = context.openOrCreateDatabase(DATABASE, 0, null)) {
            database.execSQL("CREATE TABLE pantry_items (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL)");
            database.execSQL("INSERT INTO pantry_items VALUES (51, 'Rice', 250, 'g')");
            RecipeDatabaseSeeder.createAndSeed(database, context);
            database.execSQL("UPDATE recipes SET title = 'Stored recipe' WHERE recipe_id = 'pap_wors'");
            database.setVersion(2);
        }
        openHelper();
        assertEquals(3, helper.getReadableDatabase().getVersion());
        assertEquals(250, helper.getIngredient(51).getQuantity(), 0);
        assertEquals("Stored recipe", recipes.getRecipeDetail("pap_raw").getVersion().getTitle());
        assertEquals(20, recipes.getCollection().size());
        assertFalse(personal.isPersonal("pap_wors"));
        assertNotNull(personal.save(null, "New", 1, ingredients("wors", 10, "g"), steps()));
        assertNoBrokenLinks();
    }

    private ArrayList<PantryItem> ingredients(String name, double quantity, String unit) {
        return new ArrayList<>(Arrays.asList(new PantryItem(0, name, quantity, unit)));
    }

    @Test
    public void includedVersionEditPersistsAndPreservesSiblingAndPantry() {
        RecipeDetail sibling = recipes.getRecipeDetail("pap_raw");
        long pantryId = helper.addIngredient("wors", 300, "g");
        personal.updateVersion("pap_prepared", "My pap and wors", "3 servings",
                ingredients("wors", 350, "g"), steps());
        helper.close();
        openHelper();
        RecipeDetail edited = recipes.getRecipeDetail("pap_prepared");
        assertEquals("My pap and wors", edited.getVersion().getTitle());
        assertEquals("Use cooked pap", edited.getVersion().getLabel());
        assertEquals("3 servings", edited.getVersion().getYieldText());
        assertEquals(1, edited.getVersion().getRequirements().size());
        assertEquals(steps(), edited.getSteps());
        assertFalse(recipes.isVersionAvailable(edited.getVersion()));
        assertEquals(sibling.getSteps(), recipes.getRecipeDetail("pap_raw").getSteps());
        assertEquals(4, recipes.getRecipeDetail("pap_raw").getVersion().getRequirements().size());
        assertEquals("2 servings", recipes.getRecipeDetail("pap_raw").getVersion().getYieldText());
        assertEquals(300, helper.getIngredient(pantryId).getQuantity(), 0);
        assertEquals(20, recipes.getCollection().size());
        assertFalse(personal.delete("pap_wors"));
        assertNoBrokenLinks();
    }

    @Test
    public void invalidIncludedVersionEditRollsBackAndYieldKeepsItsUnits() {
        RecipeDetail original = recipes.getRecipeDetail("pap_prepared");
        ArrayList<PantryItem> duplicate = ingredients("wors", 300, "g");
        duplicate.add(new PantryItem(0, "boerewors", 100, "g"));
        try {
            personal.updateVersion("pap_prepared", "Bad", "3 servings", duplicate, steps());
            fail("Duplicate ingredients must be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals(original.getVersion().getTitle(), recipes.getRecipeDetail("pap_prepared").getVersion().getTitle());
            assertEquals(original.getSteps(), recipes.getRecipeDetail("pap_prepared").getSteps());
            assertEquals(3, recipes.getRecipeDetail("pap_prepared").getVersion().getRequirements().size());
        }
        String id = personal.saveWithYield(null, "Muffins", "12 muffins", ingredients("eggs", 2, "count"), steps());
        personal.updateVersion(id, "Muffins", "6 muffins", ingredients("eggs", 1, "count"), steps());
        assertEquals("6 muffins", recipes.getRecipeDetail(id).getVersion().getYieldText());
        assertNoBrokenLinks();
    }

    private ArrayList<String> steps() {
        return new ArrayList<>(Arrays.asList("Prepare ingredients.", "Serve."));
    }

    private void assertNoBrokenLinks() {
        try (Cursor cursor = helper.getReadableDatabase().rawQuery("PRAGMA foreign_key_check", null)) {
            assertFalse(cursor.moveToFirst());
        }
    }
}
