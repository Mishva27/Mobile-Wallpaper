package com.example.mobilewallpaper.ui.favorite;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.local.FavoriteDbHelper;
import com.example.mobilewallpaper.data.model.Wallpaper;
import com.example.mobilewallpaper.databinding.FragmentFavoriteBinding;
import com.example.mobilewallpaper.ui.common.GridSpacingItemDecoration;
import com.example.mobilewallpaper.ui.detail.WallpaperDetailActivity;
import com.example.mobilewallpaper.ui.list.WallpaperAdapter;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.InsetUtils;
import com.example.mobilewallpaper.util.RemoteConfigManager;

import java.util.List;

/**
 * Favorites tab: wallpapers the user has favorited (persisted in SQLite) in a
 * staggered grid. Reloads on resume so un-favoriting elsewhere is reflected.
 * Mirrors {@link FavoriteActivity} for use inside the bottom-nav shell.
 */
public class FavoriteFragment extends Fragment implements WallpaperAdapter.OnWallpaperClick {

    private FragmentFavoriteBinding binding;
    private WallpaperAdapter adapter;
    private FavoriteDbHelper favoriteDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetUtils.applyTopInset(binding.toolbar);
        InsetUtils.applyBottomInset(binding.rvFavorites);

        favoriteDb = FavoriteDbHelper.getInstance(requireContext());

        int spanCount = RemoteConfigManager.getInstance().getGridSpanCount();
        adapter = new WallpaperAdapter(this);
        binding.rvFavorites.setLayoutManager(
                new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
        binding.rvFavorites.addItemDecoration(new GridSpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_gap)));
        binding.rvFavorites.setAdapter(adapter);
    }

    @Override
    public void onResume() {
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
        Intent intent = new Intent(requireContext(), WallpaperDetailActivity.class);
        intent.putExtra(Constants.EXTRA_WALLPAPER, wallpaper);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
