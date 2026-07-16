package com.example.mobilewallpaper.ui.live;

import android.Manifest;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.model.LiveWallpaper;
import com.example.mobilewallpaper.databinding.ActivityLiveWallpaperDetailBinding;
import com.example.mobilewallpaper.databinding.SheetSetWallpaperBinding;
import com.example.mobilewallpaper.live.LiveWallpaperStore;
import com.example.mobilewallpaper.live.VideoLiveWallpaperService;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.util.AppExecutors;
import com.example.mobilewallpaper.util.AssetVideoPlayer;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.LiveVideoLoader;
import com.example.mobilewallpaper.util.LiveWallpaperFiles;
import com.example.mobilewallpaper.util.LoadingDialog;
import com.example.mobilewallpaper.util.NetworkUtils;
import com.example.mobilewallpaper.util.WallpaperSetter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;

/**
 * Live-wallpaper detail: downloads the video from its URL, loops it full-screen
 * (centre-cropped) and offers the same action bar as WallpaperDetailActivity
 * (share, download, set) plus a preview button.
 */
public class LiveWallpaperDetailActivity extends BaseActivity
        implements TextureView.SurfaceTextureListener {

    /**
     * Horizontal canvas multiplier for a moving wallpaper. Mirrors the value in
     * WallpaperSetter so live and still wallpapers pan by the same amount.
     */
    private static final int MOVING_WIDTH_FACTOR = 2;

    private ActivityLiveWallpaperDetailBinding binding;
    private LiveWallpaper liveWallpaper;
    private LoadingDialog loadingDialog;
    private MediaPlayer player;
    private SurfaceTexture surfaceTexture;
    private File videoFile;
    private int videoWidth;
    private int videoHeight;

    // Pre-Android 10 devices need storage permission to save to the gallery.
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startDownload();
                } else {
                    toast(R.string.error_permission_needed);
                }
            });

    // Several ROMs return RESULT_CANCELED even after the wallpaper was applied, so
    // confirm with WallpaperManager rather than trusting the result code.
    private final ActivityResultLauncher<Intent> livePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (isOurLiveWallpaperActive()) {
                            toast(R.string.msg_wallpaper_set);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityLiveWallpaperDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        liveWallpaper = getIntent().getParcelableExtra(Constants.EXTRA_LIVE_WALLPAPER);
        if (liveWallpaper == null || liveWallpaper.getVideoUrl() == null) {
            finish();
            return;
        }

        loadingDialog = new LoadingDialog(this);

        applyTopInsetMargin(binding.btnBack);
        applyTopInsetMargin(binding.btnPreview);
        applyTopInsetMargin(binding.liveBadge);
        applyBottomInsetMargin(binding.actionBar);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPreview.setOnClickListener(v -> onPreview());
        binding.actionShare.setOnClickListener(v -> onShare());
        binding.actionDownload.setOnClickListener(v -> onDownload());
        binding.btnSetLive.setOnClickListener(v -> onSetLive());

        binding.textureView.setSurfaceTextureListener(this);
        downloadVideo();
    }

    private void downloadVideo() {
        if (!NetworkUtils.isConnected(this) && videoFile == null) {
            binding.videoProgress.setVisibility(View.GONE);
            toast(R.string.no_internet_message);
            return;
        }
        binding.videoProgress.setVisibility(View.VISIBLE);
        LiveVideoLoader.download(this, liveWallpaper.getVideoUrl(), new LiveVideoLoader.Callback() {
            @Override
            public void onReady(@NonNull File file) {
                videoFile = file;
                startPlaybackIfReady();
            }

            @Override
            public void onError() {
                binding.videoProgress.setVisibility(View.GONE);
                toast(R.string.error_generic);
            }
        });
    }

    private void onPreview() {
        Intent intent = new Intent(this, LiveWallpaperPreviewActivity.class);
        intent.putExtra(Constants.EXTRA_LIVE_WALLPAPER, liveWallpaper);
        startActivity(intent);
    }

    private void onShare() {
        if (!NetworkUtils.isConnected(this)) {
            toast(R.string.no_internet_message);
            return;
        }
        loadingDialog.show(R.string.progress_preparing_share);
        LiveWallpaperFiles.share(this, liveWallpaper.getVideoUrl(),
                new LiveWallpaperFiles.ShareCallback() {
                    @Override
                    public void onReady(@NonNull Intent chooser) {
                        loadingDialog.dismiss();
                        startActivity(chooser);
                    }

                    @Override
                    public void onError() {
                        loadingDialog.dismiss();
                        toast(R.string.error_share_failed);
                    }
                });
    }

    private void onDownload() {
        if (!NetworkUtils.isConnected(this)) {
            toast(R.string.no_internet_message);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasStoragePermission()) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }
        startDownload();
    }

    private boolean hasStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startDownload() {
        loadingDialog.show(R.string.progress_downloading);
        LiveWallpaperFiles.download(this, liveWallpaper.getVideoUrl(),
                new LiveWallpaperFiles.DownloadCallback() {
                    @Override
                    public void onSuccess() {
                        loadingDialog.dismiss();
                        toast(R.string.msg_downloaded);
                    }

                    @Override
                    public void onError() {
                        loadingDialog.dismiss();
                        toast(R.string.error_download_failed);
                    }
                });
    }

    private void onSetLive() {
        // Reuse the exact same sheet as WallpaperDetailActivity.
        SheetSetWallpaperBinding sheetBinding =
                SheetSetWallpaperBinding.inflate(LayoutInflater.from(this));
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(sheetBinding.getRoot());

        sheetBinding.optionStatic.setSelected(true);
        sheetBinding.optionMoving.setSelected(false);
        sheetBinding.optionStatic.setOnClickListener(v -> {
            sheetBinding.optionStatic.setSelected(true);
            sheetBinding.optionMoving.setSelected(false);
        });
        sheetBinding.optionMoving.setOnClickListener(v -> {
            sheetBinding.optionMoving.setSelected(true);
            sheetBinding.optionStatic.setSelected(false);
        });

        sheetBinding.rowHome.setOnClickListener(v -> {
            dialog.dismiss();
            applyLiveWallpaper(WallpaperSetter.Target.HOME, selectedMode(sheetBinding));
        });
        sheetBinding.rowLock.setOnClickListener(v -> {
            dialog.dismiss();
            applyLiveWallpaper(WallpaperSetter.Target.LOCK, selectedMode(sheetBinding));
        });
        sheetBinding.rowBoth.setOnClickListener(v -> {
            dialog.dismiss();
            applyLiveWallpaper(WallpaperSetter.Target.BOTH, selectedMode(sheetBinding));
        });
        dialog.show();
    }

    private WallpaperSetter.Mode selectedMode(SheetSetWallpaperBinding sheetBinding) {
        return sheetBinding.optionMoving.isSelected()
                ? WallpaperSetter.Mode.MOVING
                : WallpaperSetter.Mode.STATIC;
    }

    private void applyLiveWallpaper(WallpaperSetter.Target target, WallpaperSetter.Mode mode) {
        if (!NetworkUtils.isConnected(this) && videoFile == null) {
            toast(R.string.no_internet_message);
            return;
        }
        loadingDialog.show(R.string.progress_applying_wallpaper);
        if (videoFile != null) {
            applyFromVideoFile(videoFile, target, mode);
        } else {
            LiveVideoLoader.download(this, liveWallpaper.getVideoUrl(),
                    new LiveVideoLoader.Callback() {
                        @Override
                        public void onReady(@NonNull File file) {
                            videoFile = file;
                            applyFromVideoFile(file, target, mode);
                        }

                        @Override
                        public void onError() {
                            loadingDialog.dismiss();
                            toast(R.string.error_wallpaper_set);
                        }
                    });
        }
    }

    /**
     * Hands the clip to {@link VideoLiveWallpaperService} and opens the system
     * live-wallpaper picker to confirm. Applying the video as a still frame here
     * would defeat the point — the whole screen is about an animated wallpaper.
     *
     * <p>{@code target} cannot be honoured: Android applies a live wallpaper
     * system-wide and its picker exposes no home/lock choice.</p>
     */
    private void applyFromVideoFile(@NonNull File file, WallpaperSetter.Target target,
                                    WallpaperSetter.Mode mode) {
        // Same rule as WallpaperSetter: a screen-sized canvas leaves the launcher
        // nothing to pan, a wider one lets it scroll the wallpaper between pages.
        DisplayMetrics dm = getResources().getDisplayMetrics();
        WallpaperManager manager = WallpaperManager.getInstance(this);
        if (mode == WallpaperSetter.Mode.STATIC) {
            manager.suggestDesiredDimensions(dm.widthPixels, dm.heightPixels);
        } else {
            manager.suggestDesiredDimensions(dm.widthPixels * MOVING_WIDTH_FACTOR, dm.heightPixels);
        }

        AppExecutors.get().background().execute(() -> {
            try {
                LiveWallpaperStore.install(this, file);
                AppExecutors.get().mainThread().execute(this::openLivePicker);
            } catch (Exception e) {
                AppExecutors.get().mainThread().execute(() -> {
                    loadingDialog.dismiss();
                    toast(R.string.error_wallpaper_set);
                });
            }
        });
    }

    private void openLivePicker() {
        loadingDialog.dismiss();
        if (isOurLiveWallpaperActive()) {
            // Already our engine — it reloads the new clip on its own, so sending the
            // user through the picker again would just be a pointless detour.
            toast(R.string.msg_wallpaper_set);
            return;
        }
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                .putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(this, VideoLiveWallpaperService.class));
        try {
            livePickerLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            // A few ROMs ship no per-component chooser; fall back to the full list.
            try {
                livePickerLauncher.launch(
                        new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
            } catch (ActivityNotFoundException e2) {
                toast(R.string.error_wallpaper_set);
            }
        }
    }

    private boolean isOurLiveWallpaperActive() {
        WallpaperInfo info = WallpaperManager.getInstance(this).getWallpaperInfo();
        return info != null
                && getPackageName().equals(info.getPackageName())
                && VideoLiveWallpaperService.class.getName().equals(info.getServiceName());
    }

    // ---- TextureView.SurfaceTextureListener ----

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
        if (binding.videoProgress.getVisibility() == View.VISIBLE) {
            binding.videoProgress.setVisibility(View.GONE);
        }
    }

    /** Starts the video once both the file is downloaded and the surface exists. */
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
            binding.videoProgress.setVisibility(View.GONE);
            toast(R.string.error_generic);
        }
    }

    private void applyCenterCrop() {
        int viewW = binding.textureView.getWidth();
        int viewH = binding.textureView.getHeight();
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
        binding.textureView.setTransform(matrix);
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

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        releasePlayer();
        super.onDestroy();
    }
}
