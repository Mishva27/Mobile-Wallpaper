package com.example.mobilewallpaper.ui.favorite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.local.FavoriteDbHelper;
import com.example.mobilewallpaper.data.model.Wallpaper;
import com.example.mobilewallpaper.databinding.ActivityFavoriteBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.ui.common.GridSpacingItemDecoration;
import com.example.mobilewallpaper.ui.detail.WallpaperDetailActivity;
import com.example.mobilewallpaper.ui.list.WallpaperAdapter;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.RemoteConfigManager;

import java.util.List;

/**
 * Displays wallpapers the user has favorited (persisted in SQLite) in a staggered
 * grid. Refreshes on resume so un-favoriting from the detail screen is reflected.
 */
public class FavoriteActivity extends BaseActivity implements WallpaperAdapter.OnWallpaperClick {

    private ActivityFavoriteBinding binding;
    private WallpaperAdapter adapter;
    private FavoriteDbHelper favoriteDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyTopInset(binding.toolbar);
        applyBottomInset(binding.rvFavorites);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        favoriteDb = FavoriteDbHelper.getInstance(this);

        int spanCount = RemoteConfigManager.getInstance().getGridSpanCount();
        adapter = new WallpaperAdapter(this);
        binding.rvFavorites.setLayoutManager(
                new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
        binding.rvFavorites.addItemDecoration(new GridSpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_gap)));
        binding.rvFavorites.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        List<Wallpaper> favorites = favoriteDb.getAllFavorites();
        if (favorites.isEmpty()) {
            showEmpty();
        } else {
            binding.stateView.getRoot().setVisibility(View.GONE);
            binding.rvFavorites.setVisibility(View.VISIBLE);
        }
        adapter.submitList(favorites);
    }

    private void showEmpty() {
        binding.rvFavorites.setVisibility(View.GONE);
        binding.stateView.getRoot().setVisibility(View.VISIBLE);
        binding.stateView.ivState.setImageResource(R.drawable.ic_favorite_border);
        binding.stateView.tvStateTitle.setText(R.string.empty_favorites_title);
        binding.stateView.tvStateMessage.setText(R.string.empty_favorites_message);
        binding.stateView.btnStateAction.setVisibility(View.GONE);
    }

    @Override
    public void onWallpaperClick(@NonNull Wallpaper wallpaper) {
        Intent intent = new Intent(this, WallpaperDetailActivity.class);
        intent.putExtra(Constants.EXTRA_WALLPAPER, wallpaper);
        startActivity(intent);
    }
}
