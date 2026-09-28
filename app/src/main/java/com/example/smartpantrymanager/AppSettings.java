package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {
    private final SharedPreferences preferences;

    public AppSettings(Context context) {
        this(context, "pantry_settings");
    }

    AppSettings(Context context, String preferenceName) {
        preferences = context.getApplicationContext().getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public String getDefaultUnit() {
        return preferences.getString("default_unit", "g");
    }

    public boolean isNewestFirst() {
        return preferences.getBoolean("newest_first", false);
    }

    public boolean isTipVisible() {
        return preferences.getBoolean("show_tip", true);
    }

    public void save(String defaultUnit, boolean newestFirst, boolean showTip) {
        if (!"g".equals(defaultUnit) && !"kg".equals(defaultUnit) && !"ml".equals(defaultUnit)
                && !"l".equals(defaultUnit) && !"count".equals(defaultUnit)) {
            throw new IllegalArgumentException("Unsupported default unit.");
        }
        preferences.edit()
                .putString("default_unit", defaultUnit)
                .putBoolean("newest_first", newestFirst)
                .putBoolean("show_tip", showTip)
                .apply();
    }
}
