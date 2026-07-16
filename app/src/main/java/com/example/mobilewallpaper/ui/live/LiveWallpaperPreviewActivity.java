package com.example.mobilewallpaper.ui.live;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.model.LiveWallpaper;
import com.example.mobilewallpaper.databinding.ActivityLiveWallpaperPreviewBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.util.AssetVideoPlayer;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.LiveVideoLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Full-screen live preview of a live (video) wallpaper. Mirrors the still-image
 * preview: a Home/Lock segmented toggle switches between a home-screen mock (clock
 * widget + real app dock) and a lock-screen mock, with the looping video playing
 * behind them.
 */
public class LiveWallpaperPreviewActivity extends BaseActivity
        implements TextureView.SurfaceTextureListener {

    private ActivityLiveWallpaperPreviewBinding binding;
    private LiveWallpaper liveWallpaper;
    private MediaPlayer player;
    private SurfaceTexture surfaceTexture;
    private File videoFile;
    private int videoWidth;
    private int videoHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityLiveWallpaperPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        liveWallpaper = getIntent().getParcelableExtra(Constants.EXTRA_LIVE_WALLPAPER);
        if (liveWallpaper == null || liveWallpaper.getVideoUrl() == null) {
            finish();
            return;
        }

        applyTopInsetMargin(binding.btnClose);
        applyBottomInsetMargin(binding.toggleContainer);

        loadDockIcons();

        binding.videoPreview.setSurfaceTextureListener(this);
        binding.btnClose.setOnClickListener(v -> finish());
        binding.segHome.setOnClickListener(v -> showHome());
        binding.segLock.setOnClickListener(v -> showLock());

        showHome();
        downloadVideo();
    }

    // ---- Video ----

    private void downloadVideo() {
        LiveVideoLoader.download(this, liveWallpaper.getVideoUrl(), new LiveVideoLoader.Callback() {
            @Override
            public void onReady(@NonNull File file) {
                videoFile = file;
                startPlaybackIfReady();
            }

            @Override
            public void onError() {
                toast(R.string.error_generic);
            }
        });
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        surfaceTexture = surface;
        startPlaybackIfReady();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        applyCenterCrop();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        surfaceTexture = null;
        releasePlayer();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        // no-op
    }

    private void startPlaybackIfReady() {
        if (player != null || videoFile == null || surfaceTexture == null) return;
        try {
            player = AssetVideoPlayer.playFile(videoFile.getAbsolutePath(),
                    new Surface(surfaceTexture));
            player.setOnVideoSizeChangedListener((mp, w, h) -> {
                videoWidth = w;
                videoHeight = h;
                applyCenterCrop();
            });
        } catch (Exception e) {
            toast(R.string.error_generic);
        }
    }

    private void applyCenterCrop() {
        int viewW = binding.videoPreview.getWidth();
        int viewH = binding.videoPreview.getHeight();
        if (viewW == 0 || viewH == 0 || videoWidth == 0 || videoHeight == 0) return;

        float viewRatio = (float) viewW / viewH;
        float videoRatio = (float) videoWidth / videoHeight;
        float scaleX = 1f;
        float scaleY = 1f;
        if (videoRatio > viewRatio) {
            scaleX = videoRatio / viewRatio;
        } else {
            scaleY = viewRatio / videoRatio;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, viewW / 2f, viewH / 2f);
        binding.videoPreview.setTransform(matrix);
    }

    private void releasePlayer() {
        if (player != null) {
            try {
                player.release();
            } catch (Exception ignored) {
                // already released
            }
            player = null;
        }
    }

    // ---- Home / Lock mock ----

    /** Fills the home-screen dock with real launcher-app icons from this device. */
    private void loadDockIcons() {
        ImageView[] slots = {
                binding.appIcon1, binding.appIcon2, binding.appIcon3,
                binding.appIcon4, binding.appIcon5
        };
        List<Drawable> icons = queryLauncherIcons(slots.length);
        for (int i = 0; i < slots.length; i++) {
            if (i < icons.size()) {
                slots[i].setImageDrawable(icons.get(i));
                slots[i].setVisibility(View.VISIBLE);
            } else {
                slots[i].setVisibility(View.GONE);
            }
        }
        binding.dock.setVisibility(icons.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private List<Drawable> queryLauncherIcons(int max) {
        List<Drawable> result = new ArrayList<>();
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        String self = getPackageName();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo info : activities) {
            if (result.size() >= max) break;
            if (info.activityInfo == null) continue;
            String pkg = info.activityInfo.packageName;
            if (pkg == null || pkg.equals(self) || !seen.add(pkg)) continue;
            try {
                result.add(info.loadIcon(pm));
            } catch (Exception ignored) {
                // Skip any app whose icon can't be loaded.
            }
        }
        return result;
    }

    private void showHome() {
        binding.homeOverlay.setVisibility(View.VISIBLE);
        binding.lockOverlay.setVisibility(View.GONE);
        setSegmentActive(binding.segHome, binding.ivSegHome, binding.tvSegHome, true);
        setSegmentActive(binding.segLock, binding.ivSegLock, binding.tvSegLock, false);
    }

    private void showLock() {
        binding.homeOverlay.setVisibility(View.GONE);
        binding.lockOverlay.setVisibility(View.VISIBLE);
        setSegmentActive(binding.segHome, binding.ivSegHome, binding.tvSegHome, false);
        setSegmentActive(binding.segLock, binding.ivSegLock, binding.tvSegLock, true);
    }

    private void setSegmentActive(View segment, ImageView icon, TextView label, boolean active) {
        segment.setBackgroundResource(active ? R.drawable.bg_segment_active : 0);
        int color = getColor(active ? R.color.white : R.color.text_secondary);
        icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        label.setTextColor(color);
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }
}
