package com.example.mobilewallpaper.live;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mobilewallpaper.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Shared state between the picker flow in {@code LiveWallpaperDetailActivity} and
 * {@link VideoLiveWallpaperService}: which clip the engine should loop.
 *
 * <p>The engine is a separate component with its own lifecycle and can be recreated
 * long after the activity is gone, so the selection lives in SharedPreferences
 * rather than a static field.</p>
 */
public final class LiveWallpaperStore {

    /** Name of the active clip inside app-internal storage. */
    private static final String VIDEO_FILE_NAME = "live_wallpaper.mp4";

    private LiveWallpaperStore() {
    }

    private static SharedPreferences prefs(@NonNull Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Copies {@code source} into app-internal storage and records it as the active
     * clip, bumping the version so a running engine reloads instead of keeping the
     * previous video. Blocking — call off the main thread.
     *
     * <p>The download lives in Glide's cache, which the OS may evict at any time;
     * the engine needs a file that outlives it.</p>
     */
    public static void install(@NonNull Context context, @NonNull File source)
            throws IOException {
        Context appContext = context.getApplicationContext();
        File target = new File(appContext.getFilesDir(), VIDEO_FILE_NAME);

        // Stage then rename, so a running engine never reads a half-written file.
        File staged = new File(appContext.getFilesDir(), VIDEO_FILE_NAME + ".tmp");
        copy(source, staged);
        if (!staged.renameTo(target)) {
            copy(staged, target);
            staged.delete();
        }

        prefs(appContext).edit()
                .putString(Constants.KEY_LIVE_VIDEO_PATH, target.getAbsolutePath())
                .putInt(Constants.KEY_LIVE_VERSION, version(appContext) + 1)
                .apply();
    }

    private static void copy(@NonNull File from, @NonNull File to) throws IOException {
        try (InputStream in = new FileInputStream(from);
             OutputStream out = new FileOutputStream(to)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    /** Absolute path of the active clip, or null if nothing has been chosen yet. */
    @Nullable
    public static String videoPath(@NonNull Context context) {
        return prefs(context).getString(Constants.KEY_LIVE_VIDEO_PATH, null);
    }

    /** Incremented on every {@link #install}; the engine compares it to spot a new clip. */
    public static int version(@NonNull Context context) {
        return prefs(context).getInt(Constants.KEY_LIVE_VERSION, 0);
    }
}
