package com.example.smartpantrymanager;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;

public class IngredientActivity extends AppCompatActivity {
    public static final String EXTRA_INGREDIENT_ID = "com.example.smartpantrymanager.INGREDIENT_ID";
    private PantryDatabaseHelper databaseHelper;
    private EditText nameInput;
    private EditText quantityInput;
    private Spinner unitSpinner;
    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private View saveButton;
    private long ingredientId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ingredient);
        addSystemBarSpacing();

        databaseHelper = new PantryDatabaseHelper(this);
        nameInput = findViewById(R.id.inputIngredientName);
        quantityInput = findViewById(R.id.inputIngredientQuantity);
        unitSpinner = findViewById(R.id.spinnerIngredientUnit);
        nameLayout = findViewById(R.id.layoutIngredientName);
        quantityLayout = findViewById(R.id.layoutIngredientQuantity);
        saveButton = findViewById(R.id.buttonSaveIngredient);
        ingredientId = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, -1);

        if (ingredientId != -1) {
            TextView heading = findViewById(R.id.textFormHeading);
            heading.setText(R.string.edit_ingredient);
            // Android restores unsaved fields after rotation; do not overwrite them.
            if (savedInstanceState == null) {
                loadIngredient();
            }
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveIngredient();
            }
        });
        findViewById(R.id.buttonCancelIngredient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void loadIngredient() {
        try {
            PantryItem ingredient = databaseHelper.getIngredient(ingredientId);
            if (ingredient == null) {
                Toast.makeText(this, R.string.error_ingredient_missing, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            nameInput.setText(ingredient.getName());
            quantityInput.setText(BigDecimal.valueOf(ingredient.getQuantity())
                    .stripTrailingZeros().toPlainString());
            String[] units = getResources().getStringArray(R.array.ingredient_units);
            for (int position = 0; position < units.length; position++) {
                if (units[position].equals(ingredient.getUnit())) {
                    unitSpinner.setSelection(position);
                    break;
                }
            }
        } catch (SQLiteException exception) {
            Toast.makeText(this, R.string.error_database, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void saveIngredient() {
        nameLayout.setError(null);
        quantityLayout.setError(null);
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_ingredient_name));
            nameInput.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = IngredientInput.parseQuantity(quantityInput.getText().toString());
        } catch (IllegalArgumentException exception) {
            quantityLayout.setError(getString(R.string.error_ingredient_quantity));
            quantityInput.requestFocus();
            return;
        }

        String unit = unitSpinner.getSelectedItem().toString();
        saveButton.setEnabled(false);
        try {
            if (ingredientId == -1) {
                databaseHelper.addIngredient(name, quantity, unit);
            } else {
                boolean updated = databaseHelper.updateIngredient(ingredientId, name, quantity, unit);
                if (!updated) {
                    Toast.makeText(this, R.string.error_ingredient_missing, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            Toast.makeText(this, R.string.ingredient_saved, Toast.LENGTH_SHORT).show();
            finish();
        } catch (SQLiteException exception) {
            saveButton.setEnabled(true);
            Toast.makeText(this, R.string.error_database, Toast.LENGTH_LONG).show();
        }
    }

    private void addSystemBarSpacing() {
        View mainView = findViewById(R.id.ingredientScreen);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                Insets spacing = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
                view.setPadding(spacing.left, spacing.top, spacing.right, spacing.bottom);
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
