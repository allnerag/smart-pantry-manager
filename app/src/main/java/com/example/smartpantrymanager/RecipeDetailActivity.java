package com.example.smartpantrymanager;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
    private PantryDatabaseHelper databaseHelper;
    private RecipeRepository repository;
    private TextView titleView;
    private TextView statusView;
    private View contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        addSystemBarSpacing();

        databaseHelper = new PantryDatabaseHelper(this);
        repository = new RecipeRepository(databaseHelper);
        titleView = findViewById(R.id.textDetailTitle);
        statusView = findViewById(R.id.textDetailStatus);
        contentView = findViewById(R.id.layoutRecipeDetailContent);
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
            if (!repository.isVersionAvailable(version)) {
                statusView.setText(R.string.recipe_unavailable);
                return;
            }
            showRecipe(detail);
        } catch (SQLiteException exception) {
            statusView.setText(R.string.error_database);
        }
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
