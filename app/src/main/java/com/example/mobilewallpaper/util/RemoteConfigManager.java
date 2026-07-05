package com.example.mobilewallpaper.util;

import androidx.annotation.NonNull;

import com.example.mobilewallpaper.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

/**
 * Wraps Firebase Remote Config: registers XML defaults, fetches/activates values,
 * and exposes typed getters used across the app (e.g. DB root path, grid spans).
 */
public final class RemoteConfigManager {

    private static volatile RemoteConfigManager instance;
    private final FirebaseRemoteConfig remoteConfig;

    private RemoteConfigManager() {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                // Fetch fresh values at most once per hour in production.
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        remoteConfig.setConfigSettingsAsync(settings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
    }

    public static RemoteConfigManager getInstance() {
        if (instance == null) {
            synchronized (RemoteConfigManager.class) {
                if (instance == null) {
                    instance = new RemoteConfigManager();
                }
            }
        }
        return instance;
    }

    public interface FetchCallback {
        void onComplete();
    }

    /** Fetches and activates remote values; always calls back (even on failure). */
    public void fetch(@NonNull FetchCallback callback) {
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> callback.onComplete());
    }

    public String getDbRootPath() {
        String path = remoteConfig.getString(Constants.RC_DB_ROOT_PATH);
        return (path == null || path.trim().isEmpty()) ? Constants.DEFAULT_DB_ROOT : path.trim();
    }

    public int getGridSpanCount() {
        long span = remoteConfig.getLong(Constants.RC_GRID_SPAN_COUNT);
        return span >= 2 && span <= 4 ? (int) span : 2;
    }

    public boolean showProBadge() {
        return remoteConfig.getBoolean(Constants.RC_SHOW_PRO_BADGE);
    }

    public long getSplashMinDuration() {
        long duration = remoteConfig.getLong(Constants.RC_SPLASH_MIN_DURATION);
        return duration > 0 ? duration : 1200L;
    }
}
