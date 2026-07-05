package com.example.mobilewallpaper.ui.list;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilewallpaper.data.model.Wallpaper;
import com.example.mobilewallpaper.databinding.ItemWallpaperBinding;
import com.example.mobilewallpaper.util.ImageLoader;
import com.example.mobilewallpaper.util.RemoteConfigManager;

/**
 * Staggered-grid adapter for wallpapers. Shows an optional PRO badge (gated by
 * Remote Config) and reports clicks with the tapped {@link Wallpaper}.
 */
public class WallpaperAdapter extends ListAdapter<Wallpaper, WallpaperAdapter.WallpaperViewHolder> {

    public interface OnWallpaperClick {
        void onWallpaperClick(@NonNull Wallpaper wallpaper);
    }

    private final OnWallpaperClick clickListener;
    private final boolean showProBadge;

    // Repeating height multipliers (relative to column width) for the staggered
    // mosaic, since the source wallpapers largely share one aspect ratio.
    private static final float[] HEIGHT_RATIOS = {1.6f, 1.3f, 1.85f, 1.45f, 1.7f, 1.35f};

    private final int columnWidthPx;

    public WallpaperAdapter(@NonNull OnWallpaperClick clickListener) {
        super(DIFF);
        this.clickListener = clickListener;
        this.showProBadge = RemoteConfigManager.getInstance().showProBadge();
        int spanCount = Math.max(1, RemoteConfigManager.getInstance().getGridSpanCount());
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        this.columnWidthPx = screenWidth / spanCount;
    }

    private static final DiffUtil.ItemCallback<Wallpaper> DIFF =
            new DiffUtil.ItemCallback<Wallpaper>() {
                @Override
                public boolean areItemsTheSame(@NonNull Wallpaper o, @NonNull Wallpaper n) {
                    return o.getImageUrl() != null && o.getImageUrl().equals(n.getImageUrl());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Wallpaper o, @NonNull Wallpaper n) {
                    return o.isPro() == n.isPro()
                            && o.getImageUrl() != null && o.getImageUrl().equals(n.getImageUrl());
                }
            };

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWallpaperBinding binding = ItemWallpaperBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WallpaperViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class WallpaperViewHolder extends RecyclerView.ViewHolder {
        private final ItemWallpaperBinding binding;

        WallpaperViewHolder(@NonNull ItemWallpaperBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    clickListener.onWallpaperClick(getItem(pos));
                }
            });
        }

        void bind(@NonNull Wallpaper wallpaper, int position) {
            // Vary each cell's height so the staggered grid forms a mosaic.
            int height = Math.round(columnWidthPx * HEIGHT_RATIOS[position % HEIGHT_RATIOS.length]);
            ViewGroup.LayoutParams lp = binding.ivWallpaper.getLayoutParams();
            if (lp.height != height) {
                lp.height = height;
                binding.ivWallpaper.setLayoutParams(lp);
            }

            binding.tvPro.setVisibility(showProBadge && wallpaper.isPro() ? View.VISIBLE : View.GONE);
            ImageLoader.loadWithProgress(
                    binding.ivWallpaper, wallpaper.getImageUrl(), binding.wallpaperLoading);
        }
    }
}
