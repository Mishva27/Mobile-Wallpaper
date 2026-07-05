package com.example.mobilewallpaper.ui.onboarding;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.databinding.ItemOnboardingBinding;

/**
 * Static three-page adapter for the onboarding pager: image + title + subtitle.
 */
public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageViewHolder> {

    private static final int[] IMAGES = {
            R.drawable.onboarding_1, R.drawable.onboarding_2, R.drawable.onboarding_3
    };
    private static final int[] TITLES = {
            R.string.onboard_title_1, R.string.onboard_title_2, R.string.onboard_title_3
    };
    private static final int[] SUBTITLES = {
            R.string.onboard_subtitle_1, R.string.onboard_subtitle_2, R.string.onboard_subtitle_3
    };

    public static int pageCount() {
        return IMAGES.length;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOnboardingBinding binding = ItemOnboardingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return IMAGES.length;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        private final ItemOnboardingBinding binding;

        PageViewHolder(@NonNull ItemOnboardingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(int position) {
            binding.ivOnboard.setImageResource(IMAGES[position]);
            binding.tvTitle.setText(TITLES[position]);
            binding.tvSubtitle.setText(SUBTITLES[position]);
        }
    }
}
