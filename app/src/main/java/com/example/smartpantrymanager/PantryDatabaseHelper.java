package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class PantryDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 3;
    private final Context appContext;
    private static final String TABLE_PANTRY = "pantry_items";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_UNIT = "unit";

    public PantryDatabaseHelper(Context context) {
        this(context, DATABASE_NAME);
    }

    // Tests use a separate database so they cannot change the user's pantry.
    PantryDatabaseHelper(Context context, String databaseName) {
        super(context.getApplicationContext(), databaseName, null, DATABASE_VERSION);
        appContext = context.getApplicationContext();
    }

    @Override
    public void onConfigure(SQLiteDatabase database) {
        super.onConfigure(database);
        database.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL CHECK(length(trim(name)) > 0), "
                + COLUMN_QUANTITY + " REAL NOT NULL CHECK(quantity > 0), "
                + COLUMN_UNIT + " TEXT NOT NULL CHECK(unit IN ('g', 'kg', 'ml', 'l', 'count')))";

        database.execSQL(createPantryTable);
        RecipeDatabaseSeeder.createAndSeed(database, appContext);
        addRecipeOwnership(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add recipe tables without dropping or changing the user's pantry table.
            RecipeDatabaseSeeder.createAndSeed(database, appContext);
        }
        if (oldVersion < 3) {
            addRecipeOwnership(database);
        }
    }

    private void addRecipeOwnership(SQLiteDatabase database) {
        database.execSQL("ALTER TABLE recipes ADD COLUMN user_created INTEGER NOT NULL DEFAULT 0 "
                + "CHECK(user_created IN (0, 1))");
    }

    public long addIngredient(String name, double quantity, String unit) {
        ContentValues ingredientValues = createIngredientValues(name, quantity, unit);
        SQLiteDatabase database = getWritableDatabase();
        return database.insertOrThrow(TABLE_PANTRY, null, ingredientValues);
    }

    public ArrayList<PantryItem> getAllIngredients() {
        return getAllIngredients(false);
    }

    public ArrayList<PantryItem> getAllIngredients(boolean newestFirst) {
        ArrayList<PantryItem> ingredients = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String sortOrder = COLUMN_NAME + " COLLATE NOCASE ASC, " + COLUMN_ID + " ASC";
        if (newestFirst) {
            sortOrder = COLUMN_ID + " DESC";
        }

        try (Cursor cursor = database.query(
                TABLE_PANTRY, null, null, null, null, null,
                sortOrder)) {
            while (cursor.moveToNext()) {
                ingredients.add(readIngredient(cursor));
            }
        }

        return ingredients;
    }

    public PantryItem getIngredient(long ingredientId) {
        SQLiteDatabase database = getReadableDatabase();
        String[] selectionArguments = {String.valueOf(ingredientId)};

        try (Cursor cursor = database.query(
                TABLE_PANTRY, null, COLUMN_ID + " = ?", selectionArguments,
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return readIngredient(cursor);
            }
        }

        return null;
    }

    public boolean updateIngredient(long ingredientId, String name, double quantity, String unit) {
        ContentValues ingredientValues = createIngredientValues(name, quantity, unit);
        SQLiteDatabase database = getWritableDatabase();
        String[] selectionArguments = {String.valueOf(ingredientId)};

        int updatedRows = database.update(
                TABLE_PANTRY, ingredientValues, COLUMN_ID + " = ?", selectionArguments);
        return updatedRows == 1;
    }

    public boolean deleteIngredient(long ingredientId) {
        SQLiteDatabase database = getWritableDatabase();
        String[] selectionArguments = {String.valueOf(ingredientId)};

        int deletedRows = database.delete(TABLE_PANTRY, COLUMN_ID + " = ?", selectionArguments);
        return deletedRows == 1;
    }

    private ContentValues createIngredientValues(String name, double quantity, String unit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter an ingredient name.");
        }

        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException("Enter a quantity greater than zero.");
        }

        if (!isSupportedUnit(unit)) {
            throw new IllegalArgumentException("Choose g, kg, ml, l or count.");
        }

        ContentValues ingredientValues = new ContentValues();
        ingredientValues.put(COLUMN_NAME, name.trim());
        ingredientValues.put(COLUMN_QUANTITY, quantity);
        ingredientValues.put(COLUMN_UNIT, unit);
        return ingredientValues;
    }

    private boolean isSupportedUnit(String unit) {
        return "g".equals(unit) || "kg".equals(unit) || "ml".equals(unit)
                || "l".equals(unit) || "count".equals(unit);
    }

    private PantryItem readIngredient(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
        double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT));
        return new PantryItem(id, name, quantity, unit);
    }
}
