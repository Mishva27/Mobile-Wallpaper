package com.example.mobilewallpaper.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.mobilewallpaper.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Downloads a wallpaper, caches it as a JPEG, and launches the system share sheet
 * through a {@link FileProvider} content URI.
 */
public final class ShareHelper {

    private static final String SHARE_DIR = "shared";

    public interface Callback {
        void onReady(@NonNull Intent chooser);

        void onError();
    }

    private ShareHelper() {
    }

    public static void share(@NonNull Context context, @NonNull String url, @NonNull Callback callback) {
        Context appContext = context.getApplicationContext();
        AppExecutors.get().background().execute(() -> {
            try {
                Bitmap bitmap = Glide.with(appContext)
                        .asBitmap()
                        .load(DriveUrl.normalize(url, DriveUrl.WIDTH_FULL))
                        .submit()
                        .get();

                File dir = new File(appContext.getCacheDir(), SHARE_DIR);
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IllegalStateException("Unable to create share cache dir");
                }
                File file = new File(dir, "wallpaper_" + Math.abs(url.hashCode()) + ".jpg");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                }

                Uri uri = FileProvider.getUriForFile(
                        appContext, appContext.getPackageName() + ".fileprovider", file);

                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("image/jpeg");
                send.putExtra(Intent.EXTRA_STREAM, uri);
                send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent chooser = Intent.createChooser(
                        send, appContext.getString(R.string.msg_share_via));
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                AppExecutors.get().mainThread().execute(() -> callback.onReady(chooser));
            } catch (Exception e) {
                AppExecutors.get().mainThread().execute(callback::onError);
            }
        });
    }
}
