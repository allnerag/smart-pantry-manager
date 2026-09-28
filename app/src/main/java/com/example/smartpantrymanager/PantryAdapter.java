package com.example.smartpantrymanager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PantryAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<PantryItem> ingredients = new ArrayList<>();

    public PantryAdapter(Context context) {
        this.context = context;
    }

    public void setIngredients(ArrayList<PantryItem> savedIngredients) {
        ingredients.clear();
        ingredients.addAll(savedIngredients);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return ingredients.size();
    }

    @Override
    public PantryItem getItem(int position) {
        return ingredients.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View ingredientView = convertView;
        if (ingredientView == null) {
            ingredientView = LayoutInflater.from(context).inflate(R.layout.item_pantry, parent, false);
        }

        PantryItem ingredient = getItem(position);
        TextView nameView = ingredientView.findViewById(R.id.textIngredientName);
        TextView quantityView = ingredientView.findViewById(R.id.textIngredientQuantity);
        View editButton = ingredientView.findViewById(R.id.buttonEditIngredient);

        nameView.setText(ingredient.getName());
        String quantity = BigDecimal.valueOf(ingredient.getQuantity()).stripTrailingZeros().toPlainString();
        quantityView.setText(context.getString(
                R.string.ingredient_quantity_display, quantity, ingredient.getUnit()));
        editButton.setContentDescription(context.getString(
                R.string.edit_ingredient_description, ingredient.getName()));

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editIntent = new Intent(context, IngredientActivity.class);
                // Pass the saved ID so the form updates this row instead of adding another.
                editIntent.putExtra(IngredientActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
                context.startActivity(editIntent);
            }
        });
        return ingredientView;
    }
}
