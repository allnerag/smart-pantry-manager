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
public class AppSettingsTest {
    private static final String TEST_PREFERENCES = "settings_test";
    private static final String TEST_DATABASE = "settings_sort_test.db";
    private Context context;
    private AppSettings settings;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences(TEST_PREFERENCES, Context.MODE_PRIVATE).edit().clear().commit();
        context.deleteDatabase(TEST_DATABASE);
        settings = new AppSettings(context, TEST_PREFERENCES);
    }

    @After
    public void tearDown() {
        context.getSharedPreferences(TEST_PREFERENCES, Context.MODE_PRIVATE).edit().clear().commit();
        context.deleteDatabase(TEST_DATABASE);
    }

    @Test
    public void defaultsKeepExistingPantryBehaviour() {
        assertEquals("g", settings.getDefaultUnit());
        assertFalse(settings.isNewestFirst());
        assertTrue(settings.isTipVisible());
    }

    @Test
    public void savedChoicesAreReadByAnotherSettingsInstance() {
        settings.save("kg", true, false);
        AppSettings reopened = new AppSettings(context, TEST_PREFERENCES);
        assertEquals("kg", reopened.getDefaultUnit());
        assertTrue(reopened.isNewestFirst());
        assertFalse(reopened.isTipVisible());
        reopened.save("ml", false, true);
        assertEquals("ml", settings.getDefaultUnit());
        assertFalse(settings.isNewestFirst());
        assertTrue(settings.isTipVisible());
    }

    @Test
    public void invalidUnitDoesNotOverwritePreferences() {
        settings.save("count", true, false);
        try {
            settings.save("cups", false, true);
            fail("Unsupported units must be rejected.");
        } catch (IllegalArgumentException expected) {
            assertEquals("count", settings.getDefaultUnit());
            assertTrue(settings.isNewestFirst());
            assertFalse(settings.isTipVisible());
        }
    }

    @Test
    public void newestFirstUsesAdditionOrderAndDoesNotChangeSavedUnits() {
        try (PantryDatabaseHelper helper = new PantryDatabaseHelper(context, TEST_DATABASE)) {
            long appleId = helper.addIngredient("Apples", 2, "count");
            long riceId = helper.addIngredient("Rice", 500, "g");
            helper.updateIngredient(appleId, "Apples", 3, "count");
            settings.save("kg", true, false);
            ArrayList<PantryItem> newest = helper.getAllIngredients(settings.isNewestFirst());
            assertEquals(riceId, newest.get(0).getId());
            assertEquals(appleId, newest.get(1).getId());
            assertEquals("g", newest.get(0).getUnit());
            assertEquals("count", newest.get(1).getUnit());
            assertEquals(appleId, helper.getAllIngredients(false).get(0).getId());
            assertEquals(2, helper.getAllIngredients().size());
        }
    }
}
