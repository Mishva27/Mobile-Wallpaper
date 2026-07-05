package com.example.mobilewallpaper.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

/**
 * Downloads a wallpaper bitmap off the main thread and applies it to the chosen
 * surface (home, lock, or both) via {@link WallpaperManager}.
 */
public final class WallpaperSetter {

    /**
     * Where a wallpaper should be applied. Each target lists the individual
     * WallpaperManager flags to set. BOTH sets FLAG_SYSTEM and FLAG_LOCK in
     * separate calls, because several OEM ROMs ignore the lock part when the two
     * flags are combined into a single setBitmap() call.
     */
    public enum Target {
        HOME(new int[]{WallpaperManager.FLAG_SYSTEM}),
        LOCK(new int[]{WallpaperManager.FLAG_LOCK}),
        BOTH(new int[]{WallpaperManager.FLAG_SYSTEM, WallpaperManager.FLAG_LOCK});

        final int[] flags;

        Target(int[] flags) {
            this.flags = flags;
        }
    }

    public interface Callback {
        void onSuccess(@NonNull Target target);

        void onError();
    }

    private WallpaperSetter() {
    }

    public static void apply(@NonNull Context context, @NonNull String url,
                             @NonNull Target target, @NonNull Callback callback) {
        Context appContext = context.getApplicationContext();
        AppExecutors.get().background().execute(() -> {
            try {
                Bitmap bitmap = Glide.with(appContext)
                        .asBitmap()
                        .load(DriveUrl.normalize(url, DriveUrl.WIDTH_FULL))
                        .submit()
                        .get();

                WallpaperManager manager = WallpaperManager.getInstance(appContext);
                // Apply each surface in its own call so lock + home both take effect.
                for (int flag : target.flags) {
                    manager.setBitmap(bitmap, null, true, flag);
                }

                AppExecutors.get().mainThread().execute(() -> callback.onSuccess(target));
            } catch (Exception e) {
                AppExecutors.get().mainThread().execute(callback::onError);
            }
        });
    }
}
