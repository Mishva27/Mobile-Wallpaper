package com.example.mobilewallpaper.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.mobilewallpaper.R;

import java.io.OutputStream;

/**
 * Downloads a wallpaper and saves it into the device gallery via MediaStore,
 * under a "Pictures/&lt;App name&gt;" album. Runs off the main thread.
 */
public final class DownloadHelper {

    public interface Callback {
        void onSuccess();

        void onError();
    }

    private DownloadHelper() {
    }

    public static void download(@NonNull Context context, @NonNull String url,
                                @NonNull Callback callback) {
        Context appContext = context.getApplicationContext();
        AppExecutors.get().background().execute(() -> {
            try {
                Bitmap bitmap = Glide.with(appContext)
                        .asBitmap()
                        .load(DriveUrl.normalize(url, DriveUrl.WIDTH_FULL))
                        .submit()
                        .get();
                boolean saved = saveToGallery(appContext, bitmap);
                deliver(callback, saved);
            } catch (Exception e) {
                deliver(callback, false);
            }
        });
    }

    private static boolean saveToGallery(@NonNull Context context, @NonNull Bitmap bitmap)
            throws Exception {
        String album = context.getString(R.string.app_name);
        String fileName = "Wallpaper_" + System.currentTimeMillis() + ".jpg";

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/" + album);
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) return false;

        try (OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) return false;
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
        }
        return true;
    }

    private static void deliver(@NonNull Callback callback, boolean success) {
        AppExecutors.get().mainThread().execute(
                () -> {
                    if (success) callback.onSuccess();
                    else callback.onError();
                });
    }
}
