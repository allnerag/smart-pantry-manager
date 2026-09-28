package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RecipesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_section);

        TextView heading = findViewById(R.id.textSectionHeading);
        TextView message = findViewById(R.id.textSectionMessage);
        heading.setText(R.string.recipes_heading);
        message.setText(R.string.recipes_placeholder);
        ScreenNavigation.setUp(this, R.id.navigationRecipes);
    }
}
