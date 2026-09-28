package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.materialswitch.MaterialSwitch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private Spinner unitSpinner;
    private Spinner sortSpinner;
    private MaterialSwitch tipSwitch;
    private AppSettings settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ScreenNavigation.setUp(this, R.id.navigationSettings);
        settings = new AppSettings(this);
        unitSpinner = findViewById(R.id.spinnerDefaultUnit);
        sortSpinner = findViewById(R.id.spinnerPantrySort);
        tipSwitch = findViewById(R.id.switchPantryTip);
        findViewById(R.id.buttonRecipeCollection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, RecipeCollectionActivity.class));
            }
        });
        if (savedInstanceState == null) {
            loadSettings();
        }
        findViewById(R.id.buttonSaveSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.save(unitSpinner.getSelectedItem().toString(),
                        sortSpinner.getSelectedItemPosition() == 1, tipSwitch.isChecked());
                Toast.makeText(SettingsActivity.this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSettings() {
        String[] units = getResources().getStringArray(R.array.ingredient_units);
        for (int position = 0; position < units.length; position++) {
            if (units[position].equals(settings.getDefaultUnit())) {
                unitSpinner.setSelection(position);
                break;
            }
        }
        if (settings.isNewestFirst()) {
            sortSpinner.setSelection(1);
        } else {
            sortSpinner.setSelection(0);
        }
        tipSwitch.setChecked(settings.isTipVisible());
    }
}
