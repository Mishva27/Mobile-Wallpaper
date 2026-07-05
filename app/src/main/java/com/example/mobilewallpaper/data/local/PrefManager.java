package com.example.mobilewallpaper.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobilewallpaper.util.Constants;

/**
 * Thin, testable wrapper around {@link SharedPreferences} for small key/value state
 * such as the last selected category and onboarding flags.
 */
public class PrefManager {

    private final SharedPreferences prefs;

    public PrefManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setLastCategory(String categoryName) {
        prefs.edit().putString(Constants.KEY_LAST_CATEGORY, categoryName).apply();
    }

    public String getLastCategory() {
        return prefs.getString(Constants.KEY_LAST_CATEGORY, null);
    }

    public void setOnboarded(boolean onboarded) {
        prefs.edit().putBoolean(Constants.KEY_ONBOARDED, onboarded).apply();
    }

    public boolean isOnboarded() {
        return prefs.getBoolean(Constants.KEY_ONBOARDED, false);
    }
}
