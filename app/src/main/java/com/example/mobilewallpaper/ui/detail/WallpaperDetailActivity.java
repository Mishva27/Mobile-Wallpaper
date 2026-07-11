package com.example.mobilewallpaper.ui.detail;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.local.FavoriteDbHelper;
import com.example.mobilewallpaper.data.model.Wallpaper;
import com.example.mobilewallpaper.databinding.ActivityWallpaperDetailBinding;
import com.example.mobilewallpaper.databinding.SheetSetWallpaperBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.util.AnalyticsHelper;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.DownloadHelper;
import com.example.mobilewallpaper.util.LoadingDialog;
import com.example.mobilewallpaper.util.NetworkUtils;
import com.example.mobilewallpaper.util.ShareHelper;
import com.example.mobilewallpaper.util.WallpaperSetter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * Full-screen wallpaper preview with actions: share, favorite (SQLite-backed),
 * and set as home / lock / both wallpaper.
 */
public class WallpaperDetailActivity extends BaseActivity {

    private ActivityWallpaperDetailBinding binding;
    private Wallpaper wallpaper;
    private FavoriteDbHelper favoriteDb;
    private AnalyticsHelper analytics;
    private LoadingDialog loadingDialog;
    private boolean isFavorite;

    // Storage permission is only needed to save to the gallery on Android 9 and below.
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startDownload();
                } else {
                    toast(R.string.error_permission_needed);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityWallpaperDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        wallpaper = getIntent().getParcelableExtra(Constants.EXTRA_WALLPAPER);
        if (wallpaper == null || wallpaper.getImageUrl() == null) {
            finish();
            return;
        }

        favoriteDb = FavoriteDbHelper.getInstance(this);
        analytics = new AnalyticsHelper(this);
        loadingDialog = new LoadingDialog(this);

        applyTopInsetMargin(binding.btnBack);
        applyTopInsetMargin(binding.btnPreview);
        applyBottomInsetMargin(binding.actionBar);

        loadImage();
        setupActions();
        refreshFavoriteState();
    }

    private void loadImage() {
        binding.imageProgress.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(com.example.mobilewallpaper.util.DriveUrl.normalize(
                        wallpaper.getImageUrl(),
                        com.example.mobilewallpaper.util.DriveUrl.WIDTH_FULL))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        binding.imageProgress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        binding.imageProgress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(binding.ivWallpaper);
    }

    private void setupActions() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnPreview.setOnClickListener(v -> onPreview());
        binding.actionShare.setOnClickListener(v -> onShare());
        binding.actionDownload.setOnClickListener(v -> onDownload());
        binding.actionFavorite.setOnClickListener(v -> onToggleFavorite());
        binding.actionSet.setOnClickListener(v -> onSetWallpaper());
    }

    private void onPreview() {
        Intent intent = new Intent(this, WallpaperPreviewActivity.class);
        intent.putExtra(Constants.EXTRA_WALLPAPER, wallpaper);
        startActivity(intent);
    }

    private void onDownload() {
        if (!NetworkUtils.isConnected(this)) {
            toast(R.string.no_internet_message);
            return;
        }
        // Android 10+ writes via MediaStore without a runtime permission.
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
        DownloadHelper.download(this, wallpaper.getImageUrl(), new DownloadHelper.Callback() {
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

    private void refreshFavoriteState() {
        isFavorite = favoriteDb.isFavorite(wallpaper.getImageUrl());
        updateFavoriteIcon();
    }

    private void updateFavoriteIcon() {
        binding.ivFavorite.setImageResource(
                isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        binding.ivFavorite.setColorFilter(getColor(
                isFavorite ? R.color.favorite_active : R.color.text_primary));
    }

    private void onToggleFavorite() {
        isFavorite = favoriteDb.toggleFavorite(wallpaper);
        updateFavoriteIcon();
        analytics.logFavoriteToggle(isFavorite);
        toast(isFavorite ? R.string.msg_added_favorite : R.string.msg_removed_favorite);
    }

    private void onShare() {
        // Sharing may need to fetch the full image; the disk cache covers the
        // common case, but require connectivity when it isn't cached.
        if (!NetworkUtils.isConnected(this)) {
            toast(R.string.no_internet_message);
            return;
        }
        loadingDialog.show(R.string.progress_preparing_share);
        ShareHelper.share(this, wallpaper.getImageUrl(), new ShareHelper.Callback() {
            @Override
            public void onReady(@NonNull Intent chooser) {
                loadingDialog.dismiss();
                analytics.logShare(wallpaper.getCategoryName());
                startActivity(chooser);
            }

            @Override
            public void onError() {
                loadingDialog.dismiss();
                toast(R.string.error_share_failed);
            }
        });
    }

    private void onSetWallpaper() {
        SheetSetWallpaperBinding sheetBinding =
                SheetSetWallpaperBinding.inflate(LayoutInflater.from(this));
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(sheetBinding.getRoot());

        // Wallpaper style selector — default to a static (non-scrolling) wallpaper.
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
            applyWallpaper(WallpaperSetter.Target.HOME, selectedMode(sheetBinding));
        });
        sheetBinding.rowLock.setOnClickListener(v -> {
            dialog.dismiss();
            applyWallpaper(WallpaperSetter.Target.LOCK, selectedMode(sheetBinding));
        });
        sheetBinding.rowBoth.setOnClickListener(v -> {
            dialog.dismiss();
            applyWallpaper(WallpaperSetter.Target.BOTH, selectedMode(sheetBinding));
        });
        dialog.show();
    }

    private WallpaperSetter.Mode selectedMode(SheetSetWallpaperBinding sheetBinding) {
        return sheetBinding.optionMoving.isSelected()
                ? WallpaperSetter.Mode.MOVING
                : WallpaperSetter.Mode.STATIC;
    }

    private void applyWallpaper(WallpaperSetter.Target target, WallpaperSetter.Mode mode) {
        if (!NetworkUtils.isConnected(this)) {
            toast(R.string.no_internet_message);
            return;
        }
        loadingDialog.show(R.string.progress_applying_wallpaper);
        WallpaperSetter.apply(this, wallpaper.getImageUrl(), target, mode, new WallpaperSetter.Callback() {
            @Override
            public void onSuccess(@NonNull WallpaperSetter.Target target) {
                loadingDialog.dismiss();
                analytics.logWallpaperSet(target.name());
                toast(R.string.msg_wallpaper_set);
            }

            @Override
            public void onError() {
                loadingDialog.dismiss();
                toast(R.string.error_wallpaper_set);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}
