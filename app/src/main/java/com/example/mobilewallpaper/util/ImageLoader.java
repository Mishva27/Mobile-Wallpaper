package com.example.mobilewallpaper.util;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.mobilewallpaper.R;

/**
 * Centralized Glide configuration so every image is loaded with the same
 * caching, placeholder and crossfade behavior.
 */
public final class ImageLoader {

    private ImageLoader() {
    }

    /** Loads a grid-sized image (Google Drive links are normalized to decodable bytes). */
    public static void load(@NonNull ImageView target, String url) {
        loadSized(target, url, DriveUrl.WIDTH_GRID, null);
    }

    /**
     * Loads a grid-sized image while showing {@code loadingView} until the image
     * is ready (or fails), then hides it. Used by the staggered grids so each item
     * shows its own spinner + "Loading…" label.
     */
    public static void loadWithProgress(@NonNull ImageView target, String url,
                                        @Nullable View loadingView) {
        if (loadingView != null) {
            loadingView.setVisibility(View.VISIBLE);
        }
        loadSized(target, url, DriveUrl.WIDTH_GRID, loadingView);
    }

    /** Loads a full-resolution image for full-screen previews. */
    public static void loadFull(@NonNull ImageView target, String url) {
        loadSized(target, url, DriveUrl.WIDTH_FULL, null);
    }

    private static void loadSized(@NonNull ImageView target, String url, int width,
                                  @Nullable View loadingView) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.bg_placeholder)
                .error(R.drawable.bg_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

        Glide.with(target.getContext())
                .load(DriveUrl.normalize(url, width))
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade(250))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> t, boolean isFirstResource) {
                        hide(loadingView);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> t, DataSource dataSource,
                                                   boolean isFirstResource) {
                        hide(loadingView);
                        return false;
                    }
                })
                .into(target);
    }

    private static void hide(@Nullable View loadingView) {
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
    }
}
