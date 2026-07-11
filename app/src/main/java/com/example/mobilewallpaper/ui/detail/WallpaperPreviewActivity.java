package com.example.mobilewallpaper.ui.detail;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.model.Wallpaper;
import com.example.mobilewallpaper.databinding.ActivityWallpaperPreviewBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.DriveUrl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Full-screen live preview of a wallpaper. A segmented toggle switches between a
 * home-screen mock (clock widget + a dock of the device's real app icons) and a
 * lock-screen mock (large clock + lock glyph + shortcuts + unlock hint) so the
 * user can see how the image looks in context.
 */
public class WallpaperPreviewActivity extends BaseActivity {

    private ActivityWallpaperPreviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityWallpaperPreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Wallpaper wallpaper = getIntent().getParcelableExtra(Constants.EXTRA_WALLPAPER);
        if (wallpaper == null || wallpaper.getImageUrl() == null) {
            finish();
            return;
        }

        applyTopInsetMargin(binding.btnClose);
        applyBottomInsetMargin(binding.toggleContainer);

        Glide.with(this)
                .load(DriveUrl.normalize(wallpaper.getImageUrl(), DriveUrl.WIDTH_FULL))
                .into(binding.ivPreview);

        loadDockIcons();

        binding.btnClose.setOnClickListener(v -> finish());
        binding.segHome.setOnClickListener(v -> showHome());
        binding.segLock.setOnClickListener(v -> showLock());

        // Default to the home-screen preview.
        showHome();
    }

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
        // With no resolvable launcher apps there is nothing to show — hide the dock.
        binding.dock.setVisibility(icons.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /** Returns up to {@code max} distinct launcher-app icons, excluding this app. */
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
}
