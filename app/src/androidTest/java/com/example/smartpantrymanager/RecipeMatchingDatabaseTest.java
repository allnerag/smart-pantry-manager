package com.example.smartpantrymanager;

import android.content.Context;
import android.database.Cursor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RecipeMatchingDatabaseTest {
    private static final String TEST_DATABASE = "recipe_matching_test.db";
    private Context context;
    private PantryDatabaseHelper helper;
    private RecipeRepository repository;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.deleteDatabase(TEST_DATABASE);
        helper = new PantryDatabaseHelper(context, TEST_DATABASE);
        repository = new RecipeRepository(helper);
    }

    @After
    public void tearDown() {
        helper.close();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void preparedPapNeedsWaterAndEnoughWorsAfterEveryEdit() {
        helper.addIngredient("pap", 1, "kg");
        long worsId = helper.addIngredient("WORS", 0.3, "kg");
        assertFalse(hasVersion("pap_prepared"));
        long waterId = helper.addIngredient("water", 0.05, "l");
        assertTrue(hasVersion("pap_prepared"));
        helper.updateIngredient(worsId, "wors", 299, "g");
        assertFalse(hasVersion("pap_prepared"));
        helper.updateIngredient(worsId, "wors", 300, "g");
        assertTrue(hasVersion("pap_prepared"));
        helper.deleteIngredient(waterId);
        assertFalse(hasVersion("pap_prepared"));
    }

    @Test
    public void rawPapRequiresSaltAndDoesNotCountAsCookedPap() {
        helper.addIngredient("maize meal", 200, "g");
        helper.addIngredient("wors", 300, "g");
        helper.addIngredient("water", 800, "ml");
        assertFalse(hasVersion("pap_raw"));
        helper.addIngredient("salt", 2, "g");
        assertTrue(hasVersion("pap_raw"));
        assertFalse(hasVersion("pap_prepared"));
    }

    @Test
    public void bothPapVersionsShareOneSuggestionAndDuplicatesAreSummed() {
        helper.addIngredient("maize meal", 200, "g");
        helper.addIngredient("cooked pap", 250, "g");
        helper.addIngredient("pap", 0.25, "kg");
        helper.addIngredient("wors", 300, "g");
        helper.addIngredient("water", 800, "ml");
        helper.addIngredient("salt", 2, "g");
        ArrayList<RecipeSuggestion> suggestions = repository.getSuggestions();
        assertEquals(1, suggestions.size());
        assertEquals("pap_wors", suggestions.get(0).getRecipeId());
        assertEquals(2, suggestions.get(0).getVersions().size());
    }

    @Test
    public void everySeededVersionCanMatchItsOwnCompleteIngredientList() {
        ArrayList<String> variantIds = new ArrayList<>();
        try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                "SELECT variant_id FROM recipe_variants", null)) {
            while (cursor.moveToNext()) {
                variantIds.add(cursor.getString(0));
            }
        }
        assertEquals(22, variantIds.size());
        for (String variantId : variantIds) {
            // Only clear the separate test pantry, never the user's database.
            helper.getWritableDatabase().delete("pantry_items", null, null);
            try (Cursor cursor = helper.getReadableDatabase().rawQuery(
                    "SELECT i.name, q.quantity, q.unit FROM recipe_requirements q "
                            + "JOIN recipe_ingredients i ON i.ingredient_id = q.ingredient_id "
                            + "WHERE q.variant_id = ?", new String[]{variantId})) {
                while (cursor.moveToNext()) {
                    helper.addIngredient(cursor.getString(0), cursor.getDouble(1), cursor.getString(2));
                }
            }
            assertTrue("Version did not match: " + variantId, hasVersion(variantId));
            PantryItem firstItem = helper.getAllIngredients().get(0);
            helper.deleteIngredient(firstItem.getId());
            assertFalse("Incomplete version matched: " + variantId, hasVersion(variantId));
        }
    }

    private boolean hasVersion(String variantId) {
        for (RecipeSuggestion suggestion : repository.getSuggestions()) {
            for (RecipeVariant version : suggestion.getVersions()) {
                if (version.getId().equals(variantId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
