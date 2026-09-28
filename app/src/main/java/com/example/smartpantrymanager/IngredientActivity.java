package com.example.smartpantrymanager;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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
    private AlertDialog deleteDialog;
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

        // Apply the preference only to a new form, not an edit or restored draft.
        if (ingredientId == -1 && savedInstanceState == null) {
            String defaultUnit = new AppSettings(this).getDefaultUnit();
            String[] units = getResources().getStringArray(R.array.ingredient_units);
            for (int position = 0; position < units.length; position++) {
                if (units[position].equals(defaultUnit)) {
                    unitSpinner.setSelection(position);
                    break;
                }
            }
        }

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

        View deleteButton = findViewById(R.id.buttonDeleteIngredient);
        if (ingredientId != -1) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmDeleteIngredient();
                }
            });
        }
    }

    private void confirmDeleteIngredient() {
        if (deleteDialog != null && deleteDialog.isShowing()) {
            return;
        }

        try {
            // Use the saved name, even if the form contains an unsaved name change.
            PantryItem ingredient = databaseHelper.getIngredient(ingredientId);
            if (ingredient == null) {
                Toast.makeText(this, R.string.error_ingredient_missing, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            deleteDialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_ingredient)
                    .setMessage(getString(R.string.delete_ingredient_confirmation, ingredient.getName()))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteIngredient();
                        }
                    })
                    .create();
            deleteDialog.show();
        } catch (SQLiteException exception) {
            Toast.makeText(this, R.string.error_database, Toast.LENGTH_LONG).show();
        }
    }

    private void deleteIngredient() {
        try {
            boolean deleted = databaseHelper.deleteIngredient(ingredientId);
            if (deleted) {
                Toast.makeText(this, R.string.ingredient_deleted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.error_ingredient_missing, Toast.LENGTH_LONG).show();
            }
            // Returning to the Pantry reloads its list, count and empty-state card.
            finish();
        } catch (SQLiteException exception) {
            Toast.makeText(this, R.string.error_database, Toast.LENGTH_LONG).show();
        }
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
        if (deleteDialog != null) {
            deleteDialog.dismiss();
        }
        databaseHelper.close();
        super.onDestroy();
    }
}
