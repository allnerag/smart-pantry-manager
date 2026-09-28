package com.example.smartpantrymanager;

import android.os.Bundle;
import android.database.sqlite.SQLiteException;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class RecipesActivity extends AppCompatActivity {
    private PantryDatabaseHelper databaseHelper;
    private RecipeRepository recipeRepository;
    private RecipeAdapter recipeAdapter;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);
        ScreenNavigation.setUp(this, R.id.navigationRecipes);

        databaseHelper = new PantryDatabaseHelper(this);
        recipeRepository = new RecipeRepository(databaseHelper);
        ListView recipeList = findViewById(R.id.recipeList);
        View header = getLayoutInflater().inflate(R.layout.recipe_list_header, recipeList, false);
        recipeList.addHeaderView(header, null, false);
        statusView = header.findViewById(R.id.textRecipeStatus);
        recipeAdapter = new RecipeAdapter(this);
        recipeList.setAdapter(recipeAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            ArrayList<RecipeSuggestion> suggestions = recipeRepository.getSuggestions();
            recipeAdapter.setSuggestions(suggestions);
            if (suggestions.isEmpty()) {
                statusView.setText(R.string.recipes_no_matches);
            } else {
                statusView.setText(getResources().getQuantityString(
                        R.plurals.recipe_match_count, suggestions.size(), suggestions.size()));
            }
        } catch (SQLiteException exception) {
            // Do not leave old suggestions visible if refreshing the pantry fails.
            recipeAdapter.setSuggestions(new ArrayList<RecipeSuggestion>());
            statusView.setText(R.string.error_database);
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }
}
