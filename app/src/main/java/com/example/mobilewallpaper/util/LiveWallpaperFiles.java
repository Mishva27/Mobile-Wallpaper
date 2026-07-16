package com.example.mobilewallpaper.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.example.mobilewallpaper.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Share / save helpers for live-wallpaper videos. The clip is downloaded from its
 * URL (via {@link LiveVideoLoader}, which handles Google Drive) and then either
 * shared through the app's {@link FileProvider} or saved to the gallery.
 */
public final class LiveWallpaperFiles {

    private static final String SHARE_DIR = "shared";
    private static final String MIME_MP4 = "video/mp4";

    public interface ShareCallback {
        void onReady(@NonNull Intent chooser);

        void onError();
    }

    public interface DownloadCallback {
        void onSuccess();

        void onError();
    }

    private LiveWallpaperFiles() {
    }

    /** Downloads the video and builds a share chooser intent for it. */
    public static void share(@NonNull Context context, @NonNull String url,
                             @NonNull ShareCallback callback) {
        Context appContext = context.getApplicationContext();
        AppExecutors.get().background().execute(() -> {
            try {
                File source = LiveVideoLoader.downloadSync(appContext, url);

                File dir = new File(appContext.getCacheDir(), SHARE_DIR);
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IllegalStateException("Unable to create share cache dir");
                }
                File file = new File(dir, "live_wallpaper_" + Math.abs(url.hashCode()) + ".mp4");
                copyFile(source, file);

                Uri uri = FileProvider.getUriForFile(
                        appContext, appContext.getPackageName() + ".fileprovider", file);

                Intent send = new Intent(Intent.ACTION_SEND);
                send.setType(MIME_MP4);
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

    /** Downloads the video and saves it into "Movies/&lt;App name&gt;". */
    public static void download(@NonNull Context context, @NonNull String url,
                                @NonNull DownloadCallback callback) {
        Context appContext = context.getApplicationContext();
        AppExecutors.get().background().execute(() -> {
            try {
                File source = LiveVideoLoader.downloadSync(appContext, url);
                boolean saved = saveToGallery(appContext, source);
                AppExecutors.get().mainThread().execute(() -> {
                    if (saved) callback.onSuccess();
                    else callback.onError();
                });
            } catch (Exception e) {
                AppExecutors.get().mainThread().execute(callback::onError);
            }
        });
    }

    private static boolean saveToGallery(@NonNull Context context, @NonNull File source)
            throws Exception {
        String album = context.getString(R.string.app_name);
        String fileName = "LiveWallpaper_" + System.currentTimeMillis() + ".mp4";

        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Video.Media.MIME_TYPE, MIME_MP4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Video.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_MOVIES + "/" + album);
            values.put(MediaStore.Video.Media.IS_PENDING, 1);
        }

        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) return false;

        try (InputStream in = new FileInputStream(source);
             OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) return false;
            copy(in, out);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.Video.Media.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
        }
        return true;
    }

    private static void copyFile(@NonNull File src, @NonNull File dst) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            copy(in, out);
        }
    }

    private static void copy(@NonNull InputStream in, @NonNull OutputStream out) throws Exception {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }
}
