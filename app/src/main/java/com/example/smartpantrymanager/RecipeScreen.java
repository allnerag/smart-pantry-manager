package com.example.smartpantrymanager;

import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

final class RecipeScreen {
    private RecipeScreen() {
    }

    static void addSpacing(AppCompatActivity activity, int viewId) {
        EdgeToEdge.enable(activity);
        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(viewId),
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat insets) {
                        Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        Insets keyboard = insets.getInsets(WindowInsetsCompat.Type.ime());
                        view.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard.bottom));
                        return WindowInsetsCompat.CONSUMED;
                    }
                });
    }
}

