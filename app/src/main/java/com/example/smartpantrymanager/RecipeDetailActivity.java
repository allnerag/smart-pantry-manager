package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.util.ArrayList;

public class RecipeDetailActivity extends AppCompatActivity {
    public static final String EXTRA_VARIANT_ID = "com.example.smartpantrymanager.VARIANT_ID";
    public static final String EXTRA_COLLECTION_MODE = "com.example.smartpantrymanager.COLLECTION_MODE";
    private PantryDatabaseHelper databaseHelper;
    private RecipeRepository repository;
    private TextView titleView;
    private TextView statusView;
    private View contentView;
    private PersonalRecipeRepository personalRecipes;
    private RecipeDetail currentDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        addSystemBarSpacing();

        databaseHelper = new PantryDatabaseHelper(this);
        repository = new RecipeRepository(databaseHelper);
        personalRecipes = new PersonalRecipeRepository(databaseHelper);
        titleView = findViewById(R.id.textDetailTitle);
        statusView = findViewById(R.id.textDetailStatus);
        contentView = findViewById(R.id.layoutRecipeDetailContent);
        if (getIntent().getBooleanExtra(EXTRA_COLLECTION_MODE, false)) {
            ((TextView) findViewById(R.id.buttonBackToRecipes)).setText(R.string.back_to_collection);
        }
        findViewById(R.id.buttonEditRecipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentDetail != null) {
                    Intent intent = new Intent(RecipeDetailActivity.this, RecipeEditorActivity.class);
                    intent.putExtra(EXTRA_VARIANT_ID, currentDetail.getVersion().getId());
                    startActivity(intent);
                }
            }
        });
        findViewById(R.id.buttonDeleteRecipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete();
            }
        });
        findViewById(R.id.buttonBackToRecipes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        contentView.setVisibility(View.GONE);
        findViewById(R.id.layoutPersonalRecipeActions).setVisibility(View.GONE);
        currentDetail = null;
        titleView.setText(R.string.recipe_detail_heading);
        try {
            String variantId = getIntent().getStringExtra(EXTRA_VARIANT_ID);
            RecipeDetail detail = repository.getRecipeDetail(variantId);
            if (detail == null) {
                statusView.setText(R.string.recipe_missing);
                return;
            }
            RecipeVariant version = detail.getVersion();
            titleView.setText(version.getTitle());
            boolean available = repository.isVersionAvailable(version);
            if (!available && !getIntent().getBooleanExtra(EXTRA_COLLECTION_MODE, false)) {
                statusView.setText(R.string.recipe_unavailable);
                return;
            }
            showRecipe(detail);
            currentDetail = detail;
            if (!available) {
                statusView.setText(R.string.collection_recipe_unavailable);
            }
            findViewById(R.id.layoutPersonalRecipeActions).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonDeleteRecipe).setVisibility(
                    personalRecipes.isPersonal(version.getRecipeId()) ? View.VISIBLE : View.GONE);
        } catch (SQLiteException exception) {
            statusView.setText(R.string.error_database);
        }
    }

    private void confirmDelete() {
        if (currentDetail == null) {
            return;
        }
        RecipeVariant version = currentDetail.getVersion();
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_recipe)
                .setMessage(getString(R.string.delete_recipe_confirmation, version.getTitle()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (personalRecipes.delete(version.getRecipeId())) {
                                Toast.makeText(RecipeDetailActivity.this, R.string.recipe_deleted, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                statusView.setText(R.string.personal_recipe_missing);
                            }
                        } catch (SQLiteException exception) {
                            statusView.setText(R.string.error_database);
                        }
                    }
                }).show();
    }

    private void showRecipe(RecipeDetail detail) {
        RecipeVariant version = detail.getVersion();
        TextView versionView = findViewById(R.id.textDetailVersion);
        TextView yieldView = findViewById(R.id.textDetailYield);
        TextView ingredientsView = findViewById(R.id.textDetailIngredients);
        TextView stepsView = findViewById(R.id.textDetailSteps);
        versionView.setText(version.getLabel());
        yieldView.setText(getString(R.string.recipe_yield_note, version.getYieldText()));

        StringBuilder ingredients = new StringBuilder();
        for (RecipeRequirement requirement : version.getRequirements()) {
            if (ingredients.length() > 0) {
                ingredients.append("\n\n");
            }
            String quantity = BigDecimal.valueOf(requirement.getQuantity()).stripTrailingZeros().toPlainString();
            ingredients.append(getString(R.string.recipe_ingredient_line,
                    requirement.getIngredientName(), quantity, requirement.getUnit()));
        }
        ingredientsView.setText(ingredients.toString());

        StringBuilder instructions = new StringBuilder();
        ArrayList<String> steps = detail.getSteps();
        for (int index = 0; index < steps.size(); index++) {
            if (index > 0) {
                instructions.append("\n\n");
            }
            instructions.append(getString(R.string.recipe_step_line, index + 1, steps.get(index)));
        }
        stepsView.setText(instructions.toString());
        statusView.setText(R.string.recipe_available);
        contentView.setVisibility(View.VISIBLE);
    }

    private void addSystemBarSpacing() {
        View mainView = findViewById(R.id.recipeDetailScreen);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
