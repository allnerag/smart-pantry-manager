package com.example.smartpantrymanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IngredientInputTest {
    @Test
    public void acceptsWholeAndDecimalQuantities() {
        assertEquals(2, IngredientInput.parseQuantity("2"), 0.000001);
        assertEquals(0.5, IngredientInput.parseQuantity(" .5 "), 0.000001);
        assertEquals(1.25, IngredientInput.parseQuantity("1.25"), 0.000001);
        assertEquals(1.25, IngredientInput.parseQuantity("1,25"), 0.000001);
    }

    @Test
    public void rejectsMissingOrNonPositiveQuantities() {
        String[] invalidValues = {"", " ", "0", "0.0", "-1", null};
        for (String value : invalidValues) {
            assertInvalidQuantity(value);
        }
    }

    @Test
    public void rejectsMalformedAndSpecialNumbers() {
        String[] invalidValues = {"NaN", "Infinity", "1e3", "1.2.3", "1,000.5", "one", "."};
        for (String value : invalidValues) {
            assertInvalidQuantity(value);
        }
    }

    private void assertInvalidQuantity(String value) {
        try {
            IngredientInput.parseQuantity(value);
            fail("The quantity should have been rejected: " + value);
        } catch (IllegalArgumentException expected) {
            // Invalid text must never become a saved quantity.
        }
    }
}
