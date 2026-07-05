package com.example.mobilewallpaper;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Application entry point. Forces the app into dark mode, initializes Firebase,
 * and — importantly — restricts Crashlytics collection to production (release) builds.
 */
public class MobileWallpaperApp extends Application {

    private static boolean databasePersistenceConfigured = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // The app is dark-only.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        FirebaseApp.initializeApp(this);

        // Offline cache for Realtime Database. Must be set before any DB reference is used.
        if (!databasePersistenceConfigured) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            databasePersistenceConfigured = true;
        }

        // Crashlytics only reports on production builds; debug/dev crashes are ignored.
        FirebaseCrashlytics.getInstance()
                .setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
    }
}
