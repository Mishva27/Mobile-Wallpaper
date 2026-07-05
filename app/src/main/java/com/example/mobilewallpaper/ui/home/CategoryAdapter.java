package com.example.mobilewallpaper.ui.home;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilewallpaper.data.model.Category;
import com.example.mobilewallpaper.databinding.ItemCategoryBinding;
import com.example.mobilewallpaper.util.ImageLoader;
import com.example.mobilewallpaper.util.RemoteConfigManager;

/**
 * Staggered-grid adapter for wallpaper categories. Uses ListAdapter + DiffUtil for
 * efficient updates and exposes the clicked category's index to the host.
 */
public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClick {
        void onCategoryClick(int index, @NonNull Category category);
    }

    private final OnCategoryClick clickListener;

    // Repeating height multipliers (relative to column width) that give the grid
    // its staggered mosaic look even when every source image shares one aspect ratio.
    private static final float[] HEIGHT_RATIOS = {1.35f, 1.75f, 1.5f, 1.9f, 1.45f, 1.65f};

    private final int columnWidthPx;

    public CategoryAdapter(@NonNull OnCategoryClick clickListener) {
        super(DIFF);
        this.clickListener = clickListener;
        int spanCount = Math.max(1, RemoteConfigManager.getInstance().getGridSpanCount());
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        this.columnWidthPx = screenWidth / spanCount;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF =
            new DiffUtil.ItemCallback<Category>() {
                @Override
                public boolean areItemsTheSame(@NonNull Category o, @NonNull Category n) {
                    return o.getCategoryName() != null
                            && o.getCategoryName().equals(n.getCategoryName());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Category o, @NonNull Category n) {
                    return o.getWallpaperCount() == n.getWallpaperCount()
                            && equalsSafe(o.getCategoryImage(), n.getCategoryImage());
                }

                private boolean equalsSafe(String a, String b) {
                    return a == null ? b == null : a.equals(b);
                }
            };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(@NonNull ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    clickListener.onCategoryClick(pos, getItem(pos));
                }
            });
        }

        void bind(@NonNull Category category, int position) {
            // Vary each card's height so the staggered grid forms a mosaic.
            int height = Math.round(columnWidthPx * HEIGHT_RATIOS[position % HEIGHT_RATIOS.length]);
            ViewGroup.LayoutParams lp = binding.ivCategory.getLayoutParams();
            if (lp.height != height) {
                lp.height = height;
                binding.ivCategory.setLayoutParams(lp);
            }

            binding.tvCategoryName.setText(category.getCategoryName());
            binding.tvCategoryCount.setText(
                    binding.getRoot().getResources().getQuantityString(
                            com.example.mobilewallpaper.R.plurals.wallpaper_count,
                            category.getWallpaperCount(), category.getWallpaperCount()));
            ImageLoader.loadWithProgress(
                    binding.ivCategory, category.getCategoryImage(), binding.categoryLoading);
        }
    }
}
