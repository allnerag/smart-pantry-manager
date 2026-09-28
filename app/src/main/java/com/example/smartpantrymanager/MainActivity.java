package com.example.smartpantrymanager;

import android.os.Bundle;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private PantryDatabaseHelper databaseHelper;
    private PantryAdapter pantryAdapter;
    private TextView ingredientCountView;
    private View emptyPantryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        addSystemBarSpacing();
        setUpPantryList();
    }

    private void setUpPantryList() {
        databaseHelper = new PantryDatabaseHelper(this);
        ListView pantryList = findViewById(R.id.main);
        View header = getLayoutInflater().inflate(R.layout.pantry_list_header, pantryList, false);
        View footer = getLayoutInflater().inflate(R.layout.pantry_list_footer, pantryList, false);
        pantryList.addHeaderView(header, null, false);
        pantryList.addFooterView(footer, null, false);

        ingredientCountView = header.findViewById(R.id.textIngredientCount);
        emptyPantryView = header.findViewById(R.id.layoutEmptyPantry);
        pantryAdapter = new PantryAdapter(this);
        pantryList.setAdapter(pantryAdapter);

        footer.findViewById(R.id.buttonAddIngredient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(MainActivity.this, IngredientActivity.class);
                startActivity(addIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload after returning from the form, including after an edit.
        try {
            ArrayList<PantryItem> ingredients = databaseHelper.getAllIngredients();
            pantryAdapter.setIngredients(ingredients);
            int ingredientCount = ingredients.size();
            ingredientCountView.setText(getResources().getQuantityString(
                    R.plurals.ingredient_count, ingredientCount, ingredientCount));
            if (ingredients.isEmpty()) {
                emptyPantryView.setVisibility(View.VISIBLE);
            } else {
                emptyPantryView.setVisibility(View.GONE);
            }
        } catch (SQLiteException exception) {
            Toast.makeText(this, R.string.error_database, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    private void addSystemBarSpacing() {
        View mainView = findViewById(R.id.main);

        // Keep the screen clear of the phone's status and navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(mainView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                view.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );

                return insets;
            }
        });
    }
}
