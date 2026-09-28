package com.example.smartpantrymanager;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.math.BigDecimal;
import java.util.ArrayList;

public class RecipeEditorActivity extends AppCompatActivity {
    private PantryDatabaseHelper helper;
    private PersonalRecipeRepository personalRecipes;
    private LinearLayout ingredientRows;
    private EditText nameField;
    private EditText servingsField;
    private EditText stepsField;
    private TextView errorView;
    private String variantId;
    private ArrayList<String> ingredientNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_recipe_editor);
        RecipeScreen.addSpacing(this, R.id.recipeEditorScreen);
        helper = new PantryDatabaseHelper(this);
        personalRecipes = new PersonalRecipeRepository(helper);
        ingredientRows = findViewById(R.id.layoutEditorIngredients);
        nameField = findViewById(R.id.editRecipeName);
        servingsField = findViewById(R.id.editRecipeServings);
        stepsField = findViewById(R.id.editRecipeSteps);
        errorView = findViewById(R.id.textRecipeEditorError);
        setUpButtons();
        try {
            ingredientNames = personalRecipes.getIngredientNames();
            variantId = getIntent().getStringExtra(RecipeDetailActivity.EXTRA_VARIANT_ID);
            if (variantId != null) {
                RecipeDetail detail = new RecipeRepository(helper).getRecipeDetail(variantId);
                if (detail == null) {
                    errorView.setText(R.string.personal_recipe_missing);
                    findViewById(R.id.buttonSaveRecipe).setEnabled(false);
                    return;
                }
                ((TextView) findViewById(R.id.textEditingVersion)).setText(
                        getString(R.string.editing_version_note, detail.getVersion().getLabel()));
                ((TextView) findViewById(R.id.textRecipeEditorTitle)).setText(R.string.edit_recipe);
                if (state == null) {
                    loadRecipe(detail);
                }
            } else if (state == null) {
                servingsField.setText("2 servings");
                addIngredientRow("", "", new AppSettings(this).getDefaultUnit());
            }
            if (state != null) {
                restoreRows(state);
            }
        } catch (SQLiteException exception) {
            errorView.setText(R.string.error_database);
            findViewById(R.id.buttonSaveRecipe).setEnabled(false);
        }
    }

    private void setUpButtons() {
        findViewById(R.id.buttonRecipeIngredient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addIngredientRow("", "", new AppSettings(RecipeEditorActivity.this).getDefaultUnit());
            }
        });
        findViewById(R.id.buttonSaveRecipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveRecipe();
            }
        });
        findViewById(R.id.buttonCancelRecipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadRecipe(RecipeDetail detail) {
        RecipeVariant version = detail.getVersion();
        nameField.setText(version.getTitle());
        servingsField.setText(version.getYieldText());
        for (RecipeRequirement requirement : version.getRequirements()) {
            addIngredientRow(requirement.getIngredientName(),
                    BigDecimal.valueOf(requirement.getQuantity()).stripTrailingZeros().toPlainString(),
                    requirement.getUnit());
        }
        StringBuilder steps = new StringBuilder();
        for (String step : detail.getSteps()) {
            if (steps.length() > 0) {
                steps.append("\n");
            }
            steps.append(step);
        }
        stepsField.setText(steps.toString());
    }

    private void addIngredientRow(String name, String quantity, String unit) {
        View row = getLayoutInflater().inflate(R.layout.item_recipe_ingredient_editor, ingredientRows, false);
        // Rows share layout IDs; save their values explicitly instead of Android's ID-based state.
        row.setSaveFromParentEnabled(false);
        AutoCompleteTextView nameView = row.findViewById(R.id.editRecipeIngredientName);
        nameView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ingredientNames));
        nameView.setText(name);
        ((EditText) row.findViewById(R.id.editRecipeIngredientQuantity)).setText(quantity);
        Spinner unitView = row.findViewById(R.id.spinnerRecipeIngredientUnit);
        String[] units = getResources().getStringArray(R.array.ingredient_units);
        for (int index = 0; index < units.length; index++) {
            if (units[index].equals(unit)) {
                unitView.setSelection(index);
                break;
            }
        }
        row.findViewById(R.id.buttonRemoveRecipeIngredient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingredientRows.removeView(row);
            }
        });
        ingredientRows.addView(row);
    }

    private void saveRecipe() {
        errorView.setText("");
        String title = nameField.getText().toString().trim();
        if (title.isEmpty()) {
            nameField.setError(getString(R.string.recipe_name_required));
            nameField.requestFocus();
            return;
        }
        String yieldText = servingsField.getText().toString().trim();
        if (!yieldText.matches("[1-9][0-9]*\\s+\\S.*")) {
            servingsField.setError(getString(R.string.recipe_yield_required));
            servingsField.requestFocus();
            return;
        }
        ArrayList<PantryItem> ingredients = new ArrayList<>();
        for (int index = 0; index < ingredientRows.getChildCount(); index++) {
            View row = ingredientRows.getChildAt(index);
            EditText ingredientName = row.findViewById(R.id.editRecipeIngredientName);
            EditText ingredientQuantity = row.findViewById(R.id.editRecipeIngredientQuantity);
            Spinner unit = row.findViewById(R.id.spinnerRecipeIngredientUnit);
            if (RecipeMatcher.normalizeName(ingredientName.getText().toString()).isEmpty()) {
                ingredientName.setError(getString(R.string.error_ingredient_name));
                ingredientName.requestFocus();
                return;
            }
            try {
                double quantity = IngredientInput.parseQuantity(ingredientQuantity.getText().toString());
                ingredients.add(new PantryItem(0, ingredientName.getText().toString(),
                        quantity, unit.getSelectedItem().toString()));
            } catch (IllegalArgumentException exception) {
                ingredientQuantity.setError(getString(R.string.error_ingredient_quantity));
                ingredientQuantity.requestFocus();
                return;
            }
        }
        ArrayList<String> steps = new ArrayList<>();
        for (String line : stepsField.getText().toString().split("\\r?\\n")) {
            if (!line.trim().isEmpty()) {
                steps.add(line.trim());
            }
        }
        try {
            if (variantId == null) {
                personalRecipes.saveWithYield(null, title, yieldText, ingredients, steps);
            } else {
                personalRecipes.updateVersion(variantId, title, yieldText, ingredients, steps);
            }
            Toast.makeText(this, R.string.recipe_saved, Toast.LENGTH_SHORT).show();
            finish();
        } catch (IllegalArgumentException exception) {
            errorView.setText(exception.getMessage());
        } catch (SQLiteException exception) {
            errorView.setText(R.string.error_database);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> quantities = new ArrayList<>();
        ArrayList<String> units = new ArrayList<>();
        for (int index = 0; index < ingredientRows.getChildCount(); index++) {
            View row = ingredientRows.getChildAt(index);
            names.add(((EditText) row.findViewById(R.id.editRecipeIngredientName)).getText().toString());
            quantities.add(((EditText) row.findViewById(R.id.editRecipeIngredientQuantity)).getText().toString());
            units.add(((Spinner) row.findViewById(R.id.spinnerRecipeIngredientUnit)).getSelectedItem().toString());
        }
        state.putStringArrayList("ingredient_names", names);
        state.putStringArrayList("ingredient_quantities", quantities);
        state.putStringArrayList("ingredient_units", units);
        super.onSaveInstanceState(state);
    }

    private void restoreRows(Bundle state) {
        ArrayList<String> names = state.getStringArrayList("ingredient_names");
        ArrayList<String> quantities = state.getStringArrayList("ingredient_quantities");
        ArrayList<String> units = state.getStringArrayList("ingredient_units");
        if (names != null && quantities != null && units != null) {
            for (int index = 0; index < names.size(); index++) {
                addIngredientRow(names.get(index), quantities.get(index), units.get(index));
            }
        }
    }

    @Override
    protected void onDestroy() {
        helper.close();
        super.onDestroy();
    }
}
