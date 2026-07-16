package com.example.mobilewallpaper.util;

/**
 * Centralized keys and constants. Keeps magic strings out of the rest of the code.
 */
public final class Constants {

    private Constants() {
    }

    // SharedPreferences
    public static final String PREFS_NAME = "mobile_wallpaper_prefs";
    public static final String KEY_LAST_CATEGORY = "last_selected_category";
    public static final String KEY_ONBOARDED = "has_onboarded";

    // Remote Config keys (defaults in res/xml/remote_config_defaults.xml)
    public static final String RC_DB_ROOT_PATH = "db_root_path";
    public static final String RC_GRID_SPAN_COUNT = "grid_span_count";
    public static final String RC_SHOW_PRO_BADGE = "show_pro_badge";
    public static final String RC_SPLASH_MIN_DURATION = "splash_min_duration_ms";

    // Fallback DB root if Remote Config is unavailable
    public static final String DEFAULT_DB_ROOT = "categories";

    // How long the splash stays on screen before navigating to Home (milliseconds).
    public static final long SPLASH_DURATION_MS = 4000L;

    // Intent extras
    public static final String EXTRA_CATEGORY_INDEX = "extra_category_index";
    public static final String EXTRA_WALLPAPER = "extra_wallpaper";
    public static final String EXTRA_LIVE_WALLPAPER = "extra_live_wallpaper";

    // Analytics events
    public static final String EVENT_CATEGORY_OPEN = "category_open";
    public static final String EVENT_WALLPAPER_OPEN = "wallpaper_open";
    public static final String EVENT_WALLPAPER_SET = "wallpaper_set";
    public static final String EVENT_WALLPAPER_SHARE = "wallpaper_share";
    public static final String EVENT_FAVORITE_TOGGLE = "favorite_toggle";
    public static final String PARAM_CATEGORY = "category_name";
    public static final String PARAM_TARGET = "target";
    public static final String PARAM_IS_FAVORITE = "is_favorite";
}
