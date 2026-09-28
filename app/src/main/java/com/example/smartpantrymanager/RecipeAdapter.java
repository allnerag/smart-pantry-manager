package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class RecipeAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<RecipeSuggestion> suggestions = new ArrayList<>();

    public RecipeAdapter(Context context) {
        this.context = context;
    }

    public void setSuggestions(ArrayList<RecipeSuggestion> updatedSuggestions) {
        suggestions.clear();
        suggestions.addAll(updatedSuggestions);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public RecipeSuggestion getItem(int position) {
        return suggestions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View recipeView = convertView;
        if (recipeView == null) {
            recipeView = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        }
        RecipeSuggestion suggestion = getItem(position);
        TextView titleView = recipeView.findViewById(R.id.textRecipeTitle);
        TextView versionsView = recipeView.findViewById(R.id.textRecipeVersions);
        titleView.setText(suggestion.getTitle());

        StringBuilder descriptions = new StringBuilder();
        for (RecipeVariant version : suggestion.getVersions()) {
            if (descriptions.length() > 0) {
                descriptions.append("\n\n");
            }
            descriptions.append(context.getString(R.string.recipe_version_description,
                    version.getLabel(), version.getYieldText()));
        }
        versionsView.setText(descriptions.toString());
        LinearLayout actions = recipeView.findViewById(R.id.layoutRecipeActions);
        actions.removeAllViews();
        ArrayList<RecipeVariant> versions = suggestion.getVersions();
        for (RecipeVariant version : versions) {
            MaterialButton openButton = new MaterialButton(context);
            openButton.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (versions.size() == 1) {
                openButton.setText(R.string.view_recipe);
            } else {
                openButton.setText(context.getString(R.string.view_recipe_version, version.getLabel()));
            }
            openButton.setContentDescription(context.getString(R.string.open_recipe_description,
                    suggestion.getTitle(), version.getLabel()));
            openButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent detailIntent = new Intent(context, RecipeDetailActivity.class);
                    detailIntent.putExtra(RecipeDetailActivity.EXTRA_VARIANT_ID, version.getId());
                    context.startActivity(detailIntent);
                }
            });
            actions.addView(openButton);
        }
        return recipeView;
    }
}
