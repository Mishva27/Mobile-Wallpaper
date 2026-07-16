package com.example.mobilewallpaper.ui.live;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilewallpaper.data.model.LiveWallpaper;
import com.example.mobilewallpaper.databinding.ItemLiveWallpaperBinding;
import com.example.mobilewallpaper.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Grid adapter for bundled live wallpapers. Each cell shows a poster frame pulled
 * from the asset video plus a LIVE badge.
 */
public class LiveWallpaperAdapter extends RecyclerView.Adapter<LiveWallpaperAdapter.LiveViewHolder> {

    public interface OnLiveWallpaperClick {
        void onLiveWallpaperClick(@NonNull LiveWallpaper item);
    }

    private final List<LiveWallpaper> items = new ArrayList<>();
    private final OnLiveWallpaperClick clickListener;

    public LiveWallpaperAdapter(@NonNull OnLiveWallpaperClick clickListener) {
        this.clickListener = clickListener;
    }

    public void submit(@NonNull List<LiveWallpaper> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLiveWallpaperBinding binding = ItemLiveWallpaperBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LiveViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LiveViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class LiveViewHolder extends RecyclerView.ViewHolder {
        private final ItemLiveWallpaperBinding binding;

        LiveViewHolder(@NonNull ItemLiveWallpaperBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    clickListener.onLiveWallpaperClick(items.get(pos));
                }
            });
        }

        void bind(@NonNull LiveWallpaper item) {
            binding.tvName.setText(item.getName());
            ImageLoader.load(binding.ivThumb, item.getVideoUrl());
        }
    }
}
