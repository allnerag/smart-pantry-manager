package com.example.smartpantrymanager;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class RecipeMatcherTest {
    private RecipeMatcher matcher;

    @Before
    public void setUp() {
        HashMap<String, String> aliases = new HashMap<>();
        aliases.put("wors", "boerewors");
        aliases.put("boerewors", "boerewors");
        aliases.put("pap", "cooked_pap");
        aliases.put("cooked pap", "cooked_pap");
        aliases.put("maize meal", "maize_meal");
        aliases.put("water", "water");
        aliases.put("egg", "egg");
        aliases.put("eggs", "egg");
        matcher = new RecipeMatcher(aliases);
    }

    @Test
    public void emptyPantryCannotMatch() {
        assertTrue(matches(new ArrayList<PantryItem>(), preparedPap()).isEmpty());
    }

    @Test
    public void exactQuantitiesWithCaseAndWhitespaceAliasesMatch() {
        assertEquals(1, matches(items(
                item("  COOKED\u00a0  PAP  ", 500, "g"), item("WoRs", 300, "g"),
                item("water", 50, "ml")), preparedPap()).size());
    }

    @Test
    public void missingWaterIsNotAssumedAvailable() {
        assertTrue(matches(items(item("pap", 500, "g"), item("wors", 300, "g")),
                preparedPap()).isEmpty());
    }

    @Test
    public void anyShortfallPreventsAMatch() {
        assertTrue(matches(items(item("pap", 499.999999, "g"), item("wors", 300, "g"),
                item("water", 50, "ml")), preparedPap()).isEmpty());
    }

    @Test
    public void duplicatesAndCompatibleUnitsAreCombined() {
        assertEquals(1, matches(items(item("pap", 0.25, "kg"), item("cooked pap", 250, "g"),
                item("wors", 0.1, "kg"), item("boerewors", 0.2, "kg"),
                item("water", 0.025, "l"), item("water", 25, "ml")), preparedPap()).size());
    }

    @Test
    public void volumeDoesNotCountAsMass() {
        assertTrue(matches(items(item("pap", 500, "ml"), item("wors", 300, "g"),
                item("water", 50, "ml")), preparedPap()).isEmpty());
    }

    @Test
    public void countDoesNotCountAsMass() {
        assertTrue(matches(items(item("pap", 500, "count"), item("wors", 300, "g"),
                item("water", 50, "ml")), preparedPap()).isEmpty());
    }

    @Test
    public void singularAndPluralCountsCanBeCombined() {
        RecipeVariant eggs = version("eggs", "egg_recipe", requirement("egg", 4, "count"));
        assertEquals(1, matches(items(item("egg", 1, "count"), item("eggs", 3, "count")), eggs).size());
        assertTrue(matches(items(item("eggs", 4, "g")), eggs).isEmpty());
    }

    @Test
    public void unknownNamesDoNotMatchBySubstring() {
        assertTrue(matches(items(item("pap flour", 500, "g"), item("wors", 300, "g"),
                item("water", 50, "ml")), preparedPap()).isEmpty());
    }

    @Test
    public void rawIngredientsCannotSubstituteForPreparedFood() {
        assertTrue(matches(items(item("maize meal", 500, "g"), item("wors", 300, "g"),
                item("water", 50, "ml")), preparedPap()).isEmpty());
    }

    @Test
    public void partialVersionsCannotBeMixedTogether() {
        ArrayList<PantryItem> pantry = items(item("pap", 250, "g"), item("maize meal", 200, "g"),
                item("wors", 300, "g"), item("water", 50, "ml"));
        assertTrue(matches(pantry, preparedPap(), rawPap()).isEmpty());
    }

    @Test
    public void twoCompleteVersionsProduceOneTitle() {
        ArrayList<RecipeSuggestion> suggestions = matches(items(item("pap", 500, "g"),
                item("maize meal", 200, "g"), item("wors", 300, "g"), item("water", 800, "ml")),
                preparedPap(), rawPap());
        assertEquals(1, suggestions.size());
        assertEquals(2, suggestions.get(0).getVersions().size());
    }

    @Test
    public void quantitiesAreNotConsumedWhenCheckingOtherRecipes() {
        RecipeVariant first = version("first", "recipe_a", requirement("egg", 2, "count"));
        RecipeVariant second = version("second", "recipe_b", requirement("egg", 2, "count"));
        assertEquals(2, matches(items(item("eggs", 2, "count")), first, second).size());
    }

    @Test
    public void repeatedRequirementsAreTotalled() {
        RecipeVariant version = version("double", "recipe", requirement("egg", 2, "count"),
                requirement("egg", 2, "count"));
        assertTrue(matches(items(item("eggs", 3, "count")), version).isEmpty());
        assertEquals(1, matches(items(item("eggs", 4, "count")), version).size());
    }

    @Test
    public void invalidQuantitiesAndEmptyRequirementsNeverQualify() {
        RecipeVariant eggs = version("eggs", "recipe", requirement("egg", 2, "count"));
        double[] invalid = {Double.NaN, Double.POSITIVE_INFINITY, -1, 0};
        for (double quantity : invalid) {
            assertTrue(matches(items(item("eggs", quantity, "count")), eggs).isEmpty());
            assertTrue(matches(items(item("eggs", 10, "count")),
                    version("bad", "bad_recipe", requirement("egg", quantity, "count"))).isEmpty());
        }
        assertTrue(matches(items(item("eggs", 10, "count")), version("empty", "empty_recipe")).isEmpty());
    }

    private ArrayList<RecipeSuggestion> matches(ArrayList<PantryItem> pantry, RecipeVariant... versions) {
        return matcher.findMatches(pantry, Arrays.asList(versions));
    }

    private ArrayList<PantryItem> items(PantryItem... pantryItems) {
        return new ArrayList<>(Arrays.asList(pantryItems));
    }

    private PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(1, name, quantity, unit);
    }

    private RecipeRequirement requirement(String id, double quantity, String unit) {
        return new RecipeRequirement(id, quantity, unit);
    }

    private RecipeVariant version(String id, String recipeId, RecipeRequirement... requirements) {
        return new RecipeVariant(id, recipeId, "Test recipe", id, "2 servings",
                new ArrayList<>(Arrays.asList(requirements)));
    }

    private RecipeVariant preparedPap() {
        return version("prepared", "pap_wors", requirement("cooked_pap", 500, "g"),
                requirement("boerewors", 300, "g"), requirement("water", 50, "ml"));
    }

    private RecipeVariant rawPap() {
        return version("raw", "pap_wors", requirement("maize_meal", 200, "g"),
                requirement("boerewors", 300, "g"), requirement("water", 800, "ml"));
    }
}

