package com.example.mobilewallpaper.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.local.PrefManager;
import com.example.mobilewallpaper.databinding.ActivityOnboardingBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.ui.home.HomeActivity;

/**
 * First-launch onboarding: three swipeable pages describing the core features.
 * Marks onboarding complete in {@link PrefManager} and routes to Home when finished.
 */
public class OnboardingActivity extends BaseActivity {

    private ActivityOnboardingBinding binding;
    private PrefManager prefManager;
    private View[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyTopInset(binding.onboardingRoot);
        applyBottomInsetMargin(binding.adPlaceholder);

        prefManager = new PrefManager(this);

        binding.pager.setAdapter(new OnboardingAdapter());
        buildDots(OnboardingAdapter.pageCount());
        updateUi(0);

        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateUi(position);
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            int next = binding.pager.getCurrentItem() + 1;
            if (next < OnboardingAdapter.pageCount()) {
                binding.pager.setCurrentItem(next, true);
            }
        });
        binding.btnGetStarted.setOnClickListener(v -> finishOnboarding());
    }

    private void buildDots(int count) {
        dots = new View[count];
        int size = dp(8);
        int margin = dp(4);
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(lp);
            dot.setBackgroundResource(R.drawable.dot_default);
            binding.dotsContainer.addView(dot);
            dots[i] = dot;
        }
    }

    private void updateUi(int position) {
        int lastIndex = OnboardingAdapter.pageCount() - 1;
        boolean isLast = position == lastIndex;
        binding.btnNext.setVisibility(isLast ? View.GONE : View.VISIBLE);
        binding.btnGetStarted.setVisibility(isLast ? View.VISIBLE : View.GONE);

        for (int i = 0; i < dots.length; i++) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dots[i].getLayoutParams();
            lp.width = i == position ? dp(22) : dp(8);
            dots[i].setLayoutParams(lp);
            dots[i].setBackgroundResource(
                    i == position ? R.drawable.dot_selected : R.drawable.dot_default);
        }
    }

    private void finishOnboarding() {
        prefManager.setOnboarded(true);
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
