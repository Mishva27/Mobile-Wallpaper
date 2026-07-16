package com.example.mobilewallpaper.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * Small helper that plays a video bundled in {@code assets} on a {@link Surface},
 * looping and muted. Used both by the in-app preview and the live wallpaper engine.
 */
public final class AssetVideoPlayer {

    private AssetVideoPlayer() {
    }

    /**
     * Creates and starts a looping, muted {@link MediaPlayer} that renders the
     * given asset video onto {@code surface}. The caller owns the returned player
     * and must {@link MediaPlayer#release() release} it.
     */
    @NonNull
    public static MediaPlayer play(@NonNull Context context, @NonNull String assetName,
                                   @NonNull Surface surface) throws IOException {
        MediaPlayer player = new MediaPlayer();
        try (AssetFileDescriptor afd = context.getAssets().openFd(assetName)) {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        }
        player.setSurface(surface);
        player.setLooping(true);
        player.setVolume(0f, 0f);
        player.setOnPreparedListener(MediaPlayer::start);
        player.prepareAsync();
        return player;
    }

    /**
     * Creates and starts a looping, muted {@link MediaPlayer} that renders a local
     * video file onto {@code surface}. The caller must release the returned player.
     */
    @NonNull
    public static MediaPlayer playFile(@NonNull String filePath, @NonNull Surface surface)
            throws IOException {
        MediaPlayer player = new MediaPlayer();
        player.setDataSource(filePath);
        player.setSurface(surface);
        player.setLooping(true);
        player.setVolume(0f, 0f);
        player.setOnPreparedListener(MediaPlayer::start);
        player.prepareAsync();
        return player;
    }
}
