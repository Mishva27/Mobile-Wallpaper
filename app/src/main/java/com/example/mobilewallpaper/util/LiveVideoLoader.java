package com.example.mobilewallpaper.util;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Downloads a live-wallpaper video URL to a local cache file. Uses Glide's
 * (OkHttp-backed) downloader, which already handles Google Drive's redirect
 * chains, and returns the cached source file for playback / applying / sharing.
 */
public final class LiveVideoLoader {

    public interface Callback {
        void onReady(@NonNull File file);

        void onError();
    }

    private LiveVideoLoader() {
    }

    /** Downloads {@code url} off the main thread and delivers the file back on it. */
    public static void download(@NonNull Context context, @NonNull String url,
                                @NonNull Callback callback) {
        Context appContext = context.getApplicationContext();
        AppExecutors.get().background().execute(() -> {
            try {
                File file = downloadSync(appContext, url);
                AppExecutors.get().mainThread().execute(() -> callback.onReady(file));
            } catch (Exception e) {
                AppExecutors.get().mainThread().execute(callback::onError);
            }
        });
    }

    /** Blocking download; must be called off the main thread. */
    @NonNull
    public static File downloadSync(@NonNull Context context, @NonNull String url)
            throws Exception {
        return Glide.with(context.getApplicationContext())
                .asFile()
                .load(url)
                .submit()
                .get();
    }
}
