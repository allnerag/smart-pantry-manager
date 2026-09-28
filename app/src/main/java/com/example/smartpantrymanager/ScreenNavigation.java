package com.example.smartpantrymanager;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ScreenNavigation {
    private ScreenNavigation() {
    }

    public static void setUp(AppCompatActivity activity, int selectedTab) {
        EdgeToEdge.enable(activity);
        View mainView = activity.findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                // The root handles spacing, so the bottom bar must not add it a second time.
                return WindowInsetsCompat.CONSUMED;
            }
        });

        BottomNavigationView navigation = activity.findViewById(R.id.bottomNavigation);
        navigation.setSelectedItemId(selectedTab);
        navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == selectedTab) {
                    return true;
                }

                Intent destination;
                if (item.getItemId() == R.id.navigationPantry) {
                    destination = new Intent(activity, MainActivity.class);
                } else if (item.getItemId() == R.id.navigationRecipes) {
                    destination = new Intent(activity, RecipesActivity.class);
                } else if (item.getItemId() == R.id.navigationSettings) {
                    destination = new Intent(activity, SettingsActivity.class);
                } else {
                    return false;
                }

                destination.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(destination);
                // Keep Pantry underneath the other tabs; do not build a history of tab taps.
                if (!(activity instanceof MainActivity)) {
                    activity.finish();
                }
                return false;
            }
        });
    }
}
