package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section);

        TextView heading = findViewById(R.id.textSectionHeading);
        TextView message = findViewById(R.id.textSectionMessage);
        heading.setText(R.string.navigation_settings);
        message.setText(R.string.settings_placeholder);
        ScreenNavigation.setUp(this, R.id.navigationSettings);
    }
}
