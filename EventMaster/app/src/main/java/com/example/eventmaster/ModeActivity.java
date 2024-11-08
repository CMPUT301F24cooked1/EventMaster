package com.example.eventmaster;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * The ModeActivity class manages the app's theme settings (light or dark mode) based on user preferences.
 * <p>
 * It checks saved preferences to determine if dark mode is enabled and applies the theme accordingly.
 * </p>
 */
public class ModeActivity {
    private static final String PREF_NAME = "themePrefs";
    private static final String DARK_MODE_KEY = "darkMode";

    // Apply the theme based on saved preference
    public static void applyTheme(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

}
