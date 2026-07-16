package com.example.mobilewallpaper.live;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

/**
 * Renders a bundled asset video as a live wallpaper. The clip loops silently and
 * only plays while the wallpaper surface is visible, so it doesn't drain the
 * battery behind other apps.
 */
public class VideoLiveWallpaperService extends WallpaperService {

    private static final String ASSET_LIVE_WALLPAPER = "live_wallpaper.mp4";

    @Override
    public Engine onCreateEngine() {
        return new VideoEngine();
    }

    private class VideoEngine extends Engine {

        private MediaPlayer player;
        private boolean prepared;
        private boolean visible;

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            try {
                player = new MediaPlayer();
                try (AssetFileDescriptor afd = getAssets().openFd(ASSET_LIVE_WALLPAPER)) {
                    player.setDataSource(afd.getFileDescriptor(),
                            afd.getStartOffset(), afd.getLength());
                }
                player.setSurface(holder.getSurface());
                player.setLooping(true);
                player.setVolume(0f, 0f);
                player.setOnPreparedListener(mp -> {
                    prepared = true;
                    if (visible) {
                        mp.start();
                    }
                });
                player.prepareAsync();
            } catch (Exception e) {
                release();
            }
        }

        @Override
        public void onVisibilityChanged(boolean isVisible) {
            visible = isVisible;
            if (player == null || !prepared) return;
            try {
                if (isVisible) {
                    player.start();
                } else {
                    player.pause();
                }
            } catch (IllegalStateException ignored) {
                // Player was released underneath us; nothing to do.
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            release();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            release();
        }

        private void release() {
            prepared = false;
            if (player != null) {
                try {
                    player.release();
                } catch (Exception ignored) {
                    // already released
                }
                player = null;
            }
        }
    }
}
