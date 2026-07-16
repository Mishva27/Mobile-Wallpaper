package com.example.mobilewallpaper.live;

import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.File;

/**
 * Renders the clip chosen in {@code LiveWallpaperDetailActivity} as a live
 * wallpaper. The video loops silently and only plays while the wallpaper surface
 * is visible, so it doesn't drain the battery behind other apps.
 *
 * <p>The clip to play comes from {@link LiveWallpaperStore}, which the picker flow
 * writes before handing off to the system live-wallpaper chooser.</p>
 */
public class VideoLiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new VideoEngine();
    }

    private class VideoEngine extends Engine {

        private MediaPlayer player;
        private SurfaceHolder holder;
        private boolean prepared;
        private boolean visible;
        /** Store version this engine last loaded; -1 until a clip is loaded. */
        private int loadedVersion = -1;

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            this.holder = holder;
            load();
        }

        @Override
        public void onVisibilityChanged(boolean isVisible) {
            visible = isVisible;
            // The user may have picked a different clip while this engine sat idle
            // in the background, in which case reload rather than resume the old one.
            if (isVisible && holder != null
                    && loadedVersion != LiveWallpaperStore.version(VideoLiveWallpaperService.this)) {
                load();
                return;
            }
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
            this.holder = null;
            release();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            release();
        }

        private void load() {
            release();
            String path = LiveWallpaperStore.videoPath(VideoLiveWallpaperService.this);
            if (path == null || !new File(path).exists()) return;

            loadedVersion = LiveWallpaperStore.version(VideoLiveWallpaperService.this);
            try {
                player = new MediaPlayer();
                player.setDataSource(path);
                player.setSurface(holder.getSurface());
                player.setLooping(true);
                player.setVolume(0f, 0f);
                player.setOnPreparedListener(mp -> {
                    prepared = true;
                    // Fill the screen without letterboxing; only valid once prepared.
                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    if (visible) {
                        mp.start();
                    }
                });
                player.prepareAsync();
            } catch (Exception e) {
                release();
            }
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
