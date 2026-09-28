package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class RecipeCollectionActivity extends AppCompatActivity {
    private PantryDatabaseHelper helper;
    private RecipeAdapter adapter;
    private TextView countView;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_recipe_collection);
        RecipeScreen.addSpacing(this, R.id.collectionScreen);
        helper = new PantryDatabaseHelper(this);
        ListView list = findViewById(R.id.collectionList);
        View header = getLayoutInflater().inflate(R.layout.collection_header, list, false);
        list.addHeaderView(header, null, false);
        countView = header.findViewById(R.id.textCollectionCount);
        adapter = new RecipeAdapter(this, true);
        list.setAdapter(adapter);
        header.findViewById(R.id.buttonAddRecipe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RecipeCollectionActivity.this, RecipeEditorActivity.class));
            }
        });
        findViewById(R.id.buttonCollectionBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            ArrayList<RecipeSuggestion> recipes = new RecipeRepository(helper).getCollection();
            int versions = 0;
            for (RecipeSuggestion recipe : recipes) {
                versions += recipe.getVersions().size();
            }
            adapter.setSuggestions(recipes);
            countView.setText(getString(R.string.collection_count, recipes.size(), versions));
        } catch (SQLiteException exception) {
            adapter.setSuggestions(new ArrayList<RecipeSuggestion>());
            countView.setText(R.string.error_database);
        }
    }

    @Override
    protected void onDestroy() {
        helper.close();
        super.onDestroy();
    }
}

