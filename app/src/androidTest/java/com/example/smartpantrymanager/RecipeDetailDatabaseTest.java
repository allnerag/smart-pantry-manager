package com.example.smartpantrymanager;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RecipeDetailDatabaseTest {
    private static final String TEST_DATABASE = "recipe_detail_test.db";
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
    public void preparedPapShowsItsOwnIngredientsAndOrderedSteps() {
        RecipeDetail detail = repository.getRecipeDetail("pap_prepared");
        assertNotNull(detail);
        assertEquals("Pap and wors", detail.getVersion().getTitle());
        assertEquals("Use cooked pap", detail.getVersion().getLabel());
        assertEquals("2 servings", detail.getVersion().getYieldText());
        assertEquals(3, detail.getVersion().getRequirements().size());
        RecipeRequirement pap = findRequirement(detail, "cooked_pap");
        assertNotNull(pap);
        assertEquals("Cooked pap", pap.getIngredientName());
        assertEquals(500, pap.getQuantity(), 0.000001);
        assertEquals("g", pap.getUnit());
        assertNull(findRequirement(detail, "maize_meal"));
        assertEquals(2, detail.getSteps().size());
        assertTrue(detail.getSteps().get(0).startsWith("Break the cooked pap"));
        assertTrue(detail.getSteps().get(1).startsWith("Grill the boerewors"));
    }

    @Test
    public void rawPapAndPreparedMashDoNotUseOtherVersionsInstructions() {
        RecipeDetail rawPap = repository.getRecipeDetail("pap_raw");
        assertEquals(4, rawPap.getVersion().getRequirements().size());
        assertNotNull(findRequirement(rawPap, "maize_meal"));
        assertNull(findRequirement(rawPap, "cooked_pap"));
        assertEquals(3, rawPap.getSteps().size());
        assertTrue(rawPap.getSteps().get(0).startsWith("Bring the water"));

        RecipeDetail mash = repository.getRecipeDetail("mash_prepared");
        assertEquals("Mash and wors", mash.getVersion().getTitle());
        assertNotNull(findRequirement(mash, "prepared_mash"));
        assertNull(findRequirement(mash, "potato"));
        assertTrue(mash.getSteps().get(0).startsWith("Reheat the prepared mash"));
    }

    @Test
    public void missingOrInvalidIdsReturnNoRecipe() {
        assertNull(repository.getRecipeDetail(null));
        assertNull(repository.getRecipeDetail(" "));
        assertNull(repository.getRecipeDetail("does_not_exist"));
        assertNull(repository.getRecipeDetail("' OR 1=1 --"));
    }

    @Test
    public void availabilityRefreshesWithoutConsumingIngredients() {
        helper.addIngredient("pap", 1, "kg");
        long worsId = helper.addIngredient("wors", 300, "g");
        helper.addIngredient("water", 50, "ml");
        RecipeDetail detail = repository.getRecipeDetail("pap_prepared");
        assertTrue(repository.isVersionAvailable(detail.getVersion()));
        assertEquals(300, helper.getIngredient(worsId).getQuantity(), 0.000001);
        helper.updateIngredient(worsId, "wors", 299, "g");
        assertFalse(repository.isVersionAvailable(detail.getVersion()));
    }

    private RecipeRequirement findRequirement(RecipeDetail detail, String ingredientId) {
        for (RecipeRequirement requirement : detail.getVersion().getRequirements()) {
            if (requirement.getIngredientId().equals(ingredientId)) {
                return requirement;
            }
        }
        return null;
    }
}
