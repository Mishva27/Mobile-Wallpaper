package com.example.mobilewallpaper.ui.list;

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
import com.example.mobilewallpaper.data.model.Category;
import com.example.mobilewallpaper.data.model.Wallpaper;
import com.example.mobilewallpaper.data.repository.WallpaperRepository;
import com.example.mobilewallpaper.databinding.FragmentWallpaperGridBinding;
import com.example.mobilewallpaper.ui.common.GridSpacingItemDecoration;
import com.example.mobilewallpaper.ui.detail.WallpaperDetailActivity;
import com.example.mobilewallpaper.util.AnalyticsHelper;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.RemoteConfigManager;

import java.util.Collections;
import java.util.List;

/**
 * One tab of the list screen: a staggered grid of the wallpapers in a single
 * category. The category is resolved from the repository cache by its index.
 */
public class WallpaperGridFragment extends Fragment implements WallpaperAdapter.OnWallpaperClick {

    private static final String ARG_CATEGORY_INDEX = "arg_category_index";

    private FragmentWallpaperGridBinding binding;
    private AnalyticsHelper analytics;

    public static WallpaperGridFragment newInstance(int categoryIndex) {
        WallpaperGridFragment fragment = new WallpaperGridFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CATEGORY_INDEX, categoryIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWallpaperGridBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        analytics = new AnalyticsHelper(requireContext());

        int index = getArguments() != null ? getArguments().getInt(ARG_CATEGORY_INDEX, -1) : -1;
        Category category = WallpaperRepository.getInstance().getCategoryAt(index);

        List<Wallpaper> wallpapers = category != null && category.getWallpapers() != null
                ? category.getWallpapers() : Collections.emptyList();

        if (wallpapers.isEmpty()) {
            showEmpty();
            return;
        }

        int spanCount = RemoteConfigManager.getInstance().getGridSpanCount();
        WallpaperAdapter adapter = new WallpaperAdapter(this);
        binding.rvWallpapers.setLayoutManager(
                new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
        binding.rvWallpapers.addItemDecoration(new GridSpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_gap)));
        binding.rvWallpapers.setAdapter(adapter);
        adapter.submitList(wallpapers);
    }

    private void showEmpty() {
        binding.stateView.getRoot().setVisibility(View.VISIBLE);
        binding.stateView.ivState.setImageResource(R.drawable.ic_wallpaper);
        binding.stateView.tvStateTitle.setText(R.string.error_no_categories);
        binding.stateView.tvStateMessage.setText(R.string.app_tagline);
        binding.stateView.btnStateAction.setVisibility(View.GONE);
    }

    @Override
    public void onWallpaperClick(@NonNull Wallpaper wallpaper) {
        analytics.logWallpaperOpen(wallpaper.getCategoryName());
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
