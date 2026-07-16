package com.example.mobilewallpaper.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.io.File;

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

    /**
     * How the wallpaper should behave on the home screen.
     *
     * <p>STATIC pins the wallpaper to the exact screen size so the launcher has no
     * off-screen area to pan — it stays fixed while swiping between home pages.
     * MOVING keeps a wider canvas so the launcher can scroll/parallax the wallpaper
     * as pages change (the platform default).</p>
     */
    public enum Mode {
        STATIC,
        MOVING
    }

    /** Horizontal canvas multiplier that gives a moving wallpaper room to scroll. */
    private static final int MOVING_WIDTH_FACTOR = 2;

    public interface Callback {
        void onSuccess(@NonNull Target target);

        void onError();
    }

    private WallpaperSetter() {
    }

    public static void apply(@NonNull Context context, @NonNull String url,
                             @NonNull Target target, @NonNull Mode mode,
                             @NonNull Callback callback) {
        Context appContext = context.getApplicationContext();
        AppExecutors.get().background().execute(() -> {
            try {
                Bitmap bitmap = Glide.with(appContext)
                        .asBitmap()
                        .load(DriveUrl.normalize(url, DriveUrl.WIDTH_FULL))
                        .submit()
                        .get();

                WallpaperManager manager = WallpaperManager.getInstance(appContext);

                DisplayMetrics dm = appContext.getResources().getDisplayMetrics();
                int screenW = dm.widthPixels;
                int screenH = dm.heightPixels;

                Bitmap toApply;
                Rect cropHint;
                if (mode == Mode.STATIC) {
                    // Pin the wallpaper to the screen so there is nothing to scroll.
                    toApply = cropToCover(bitmap, screenW, screenH);
                    cropHint = new Rect(0, 0, toApply.getWidth(), toApply.getHeight());
                    manager.suggestDesiredDimensions(screenW, screenH);
                } else {
                    // Give the launcher a wider canvas so it can pan the wallpaper.
                    toApply = bitmap;
                    cropHint = null;
                    manager.suggestDesiredDimensions(screenW * MOVING_WIDTH_FACTOR, screenH);
                }

                // Apply each surface in its own call so lock + home both take effect.
                for (int flag : target.flags) {
                    manager.setBitmap(toApply, cropHint, true, flag);
                }

                if (toApply != bitmap && !toApply.isRecycled()) {
                    toApply.recycle();
                }

                AppExecutors.get().mainThread().execute(() -> callback.onSuccess(target));
            } catch (Exception e) {
                AppExecutors.get().mainThread().execute(callback::onError);
            }
        });
    }

    /**
     * Applies a wallpaper taken from a local video file (a representative frame)
     * to the chosen surface, using the same crop / static-vs-moving logic as
     * {@link #apply}. Runs off the main thread.
     */
    public static void applyFromFile(@NonNull Context context, @NonNull File videoFile,
                                     @NonNull Target target, @NonNull Mode mode,
                                     @NonNull Callback callback) {
        Context appContext = context.getApplicationContext();
        AppExecutors.get().background().execute(() -> {
            try {
                Bitmap bitmap = extractFrame(videoFile);
                if (bitmap == null) {
                    throw new IllegalStateException("Unable to read a frame from " + videoFile);
                }

                WallpaperManager manager = WallpaperManager.getInstance(appContext);

                DisplayMetrics dm = appContext.getResources().getDisplayMetrics();
                int screenW = dm.widthPixels;
                int screenH = dm.heightPixels;

                Bitmap toApply;
                Rect cropHint;
                if (mode == Mode.STATIC) {
                    toApply = cropToCover(bitmap, screenW, screenH);
                    cropHint = new Rect(0, 0, toApply.getWidth(), toApply.getHeight());
                    manager.suggestDesiredDimensions(screenW, screenH);
                } else {
                    toApply = bitmap;
                    cropHint = null;
                    manager.suggestDesiredDimensions(screenW * MOVING_WIDTH_FACTOR, screenH);
                }

                for (int flag : target.flags) {
                    manager.setBitmap(toApply, cropHint, true, flag);
                }

                if (toApply != bitmap && !toApply.isRecycled()) {
                    toApply.recycle();
                }

                AppExecutors.get().mainThread().execute(() -> callback.onSuccess(target));
            } catch (Exception e) {
                AppExecutors.get().mainThread().execute(callback::onError);
            }
        });
    }

    /** Reads a representative frame from a local video file, or null on failure. */
    private static Bitmap extractFrame(@NonNull File videoFile) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoFile.getAbsolutePath());
            return retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
                // nothing to do
            }
        }
    }

    /**
     * Scales {@code src} to cover a {@code targetW} x {@code targetH} area and
     * centre-crops it to exactly those dimensions. Returns {@code src} unchanged if
     * it already matches, so callers must not recycle the result blindly.
     */
    private static Bitmap cropToCover(@NonNull Bitmap src, int targetW, int targetH) {
        if (targetW <= 0 || targetH <= 0) return src;
        if (src.getWidth() == targetW && src.getHeight() == targetH) return src;

        float scale = Math.max((float) targetW / src.getWidth(),
                (float) targetH / src.getHeight());
        int scaledW = Math.max(targetW, Math.round(src.getWidth() * scale));
        int scaledH = Math.max(targetH, Math.round(src.getHeight() * scale));

        Bitmap scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true);
        int x = (scaledW - targetW) / 2;
        int y = (scaledH - targetH) / 2;
        Bitmap out = Bitmap.createBitmap(scaled, x, y, targetW, targetH);
        if (scaled != out && scaled != src) {
            scaled.recycle();
        }
        return out;
    }
}
