package com.example.mobilewallpaper.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.splashscreen.SplashScreen;

import com.example.mobilewallpaper.data.local.PrefManager;
import com.example.mobilewallpaper.databinding.ActivitySplashBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.ui.home.HomeActivity;
import com.example.mobilewallpaper.ui.onboarding.OnboardingActivity;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.DialogUtils;
import com.example.mobilewallpaper.util.NetworkUtils;
import com.example.mobilewallpaper.util.RemoteConfigManager;

/**
 * Entry screen. Shows the system splash, then a branded gradient screen while it
 * verifies connectivity and warms up Remote Config, guaranteeing a minimum
 * on-screen duration before handing off to {@link HomeActivity}.
 */
public class SplashActivity extends BaseActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startTime;
    private boolean navigated;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Modern splash screen; must be installed before super.onCreate().
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        animateEntrance(binding);

        startTime = SystemClock.elapsedRealtime();
        begin();
    }

    /** Layered entrance: glow fades up, icon pops, and the wordmark drifts in. */
    private void animateEntrance(@NonNull ActivitySplashBinding binding) {
        binding.splashGlow.setAlpha(0f);
        binding.splashGlow.animate().alpha(1f).setDuration(900).start();

        binding.ivSplashLogo.setScaleX(0.7f);
        binding.ivSplashLogo.setScaleY(0.7f);
        binding.ivSplashLogo.setAlpha(0f);
        binding.ivSplashLogo.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(650)
                .setInterpolator(new OvershootInterpolator(0.9f))
                .start();

        binding.tvAppName.setAlpha(0f);
        binding.tvAppName.setTranslationY(30f);
        binding.tvAppName.animate()
                .alpha(1f).translationY(0f)
                .setStartDelay(220)
                .setDuration(520)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        binding.tvTagline.setAlpha(0f);
        binding.tvTagline.setTranslationY(24f);
        binding.tvTagline.animate()
                .alpha(0.8f).translationY(0f)
                .setStartDelay(340)
                .setDuration(520)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void begin() {
        if (!NetworkUtils.isConnected(this)) {
            DialogUtils.showNoInternet(this, this::begin);
            return;
        }
        RemoteConfigManager.getInstance().fetch(this::navigateWhenReady);
    }

    /** Navigates to Home once Remote Config is ready and the splash duration elapses. */
    private void navigateWhenReady() {
        long elapsed = SystemClock.elapsedRealtime() - startTime;
        long remaining = Math.max(0, Constants.SPLASH_DURATION_MS - elapsed);
        handler.postDelayed(this::goHome, remaining);
    }

    private void goHome() {
        if (navigated || isFinishing()) return;
        navigated = true;
        // First launch shows onboarding; afterwards go straight to Home.
        Class<?> destination = new PrefManager(this).isOnboarded()
                ? HomeActivity.class : OnboardingActivity.class;
        startActivity(new Intent(this, destination));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
