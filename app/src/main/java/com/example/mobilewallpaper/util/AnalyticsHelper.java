package com.example.mobilewallpaper.util;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Thin wrapper over Firebase Analytics to keep event logging consistent and
 * out of the UI classes.
 */
public final class AnalyticsHelper {

    private final FirebaseAnalytics analytics;

    public AnalyticsHelper(@NonNull Context context) {
        this.analytics = FirebaseAnalytics.getInstance(context.getApplicationContext());
    }

    public void logEvent(@NonNull String event, @Nullable String paramKey, @Nullable String paramValue) {
        Bundle bundle = new Bundle();
        if (paramKey != null && paramValue != null) {
            bundle.putString(paramKey, paramValue);
        }
        analytics.logEvent(event, bundle);
    }

    public void logCategoryOpen(String categoryName) {
        logEvent(Constants.EVENT_CATEGORY_OPEN, Constants.PARAM_CATEGORY, categoryName);
    }

    public void logWallpaperOpen(String categoryName) {
        logEvent(Constants.EVENT_WALLPAPER_OPEN, Constants.PARAM_CATEGORY, categoryName);
    }

    public void logWallpaperSet(String target) {
        logEvent(Constants.EVENT_WALLPAPER_SET, Constants.PARAM_TARGET, target);
    }

    public void logShare(String categoryName) {
        logEvent(Constants.EVENT_WALLPAPER_SHARE, Constants.PARAM_CATEGORY, categoryName);
    }

    public void logFavoriteToggle(boolean isFavorite) {
        logEvent(Constants.EVENT_FAVORITE_TOGGLE, Constants.PARAM_IS_FAVORITE, String.valueOf(isFavorite));
    }
}
