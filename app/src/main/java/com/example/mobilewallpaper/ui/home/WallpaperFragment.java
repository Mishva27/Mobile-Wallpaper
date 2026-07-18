package com.example.mobilewallpaper.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.model.Category;
import com.example.mobilewallpaper.data.model.Resource;
import com.example.mobilewallpaper.databinding.FragmentWallpaperBinding;
import com.example.mobilewallpaper.ui.common.GridSpacingItemDecoration;
import com.example.mobilewallpaper.ui.list.WallpaperListActivity;
import com.example.mobilewallpaper.util.AnalyticsHelper;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.DialogUtils;
import com.example.mobilewallpaper.util.InsetUtils;
import com.example.mobilewallpaper.util.NetworkUtils;
import com.example.mobilewallpaper.util.RemoteConfigManager;

import java.util.List;

/**
 * Wallpapers tab: shows active wallpaper categories in a staggered grid. Tapping a
 * category opens the tabbed list screen with that category pre-selected. Migrated
 * from the former Home screen so it can live inside the bottom-nav shell.
 */
public class WallpaperFragment extends Fragment implements CategoryAdapter.OnCategoryClick {

    private FragmentWallpaperBinding binding;
    private HomeViewModel viewModel;
    private CategoryAdapter adapter;
    private AnalyticsHelper analytics;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWallpaperBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetUtils.applyTopInset(binding.toolbar);
        InsetUtils.applyBottomInset(binding.rvCategories);

        analytics = new AnalyticsHelper(requireContext());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupRecycler();
        viewModel.getCategories().observe(getViewLifecycleOwner(), this::render);
        viewModel.loadIfNeeded();
    }

    private void setupRecycler() {
        int spanCount = RemoteConfigManager.getInstance().getGridSpanCount();
        adapter = new CategoryAdapter(this);
        binding.rvCategories.setLayoutManager(
                new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
        binding.rvCategories.addItemDecoration(new GridSpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_gap)));
        binding.rvCategories.setHasFixedSize(true);
        binding.rvCategories.setAdapter(adapter);

        binding.swipeRefresh.setColorSchemeResources(R.color.brand_primary, R.color.brand_secondary);
        binding.swipeRefresh.setOnRefreshListener(this::onRefresh);
    }

    private void onRefresh() {
        if (!NetworkUtils.isConnected(requireContext())) {
            binding.swipeRefresh.setRefreshing(false);
            DialogUtils.showNoInternet(requireContext(), this::onRefresh);
            return;
        }
        viewModel.refresh();
    }

    private void render(@NonNull Resource<List<Category>> resource) {
        switch (resource.status) {
            case LOADING:
                boolean hasData = adapter.getItemCount() > 0;
                binding.progress.setVisibility(hasData ? View.GONE : View.VISIBLE);
                binding.stateView.getRoot().setVisibility(View.GONE);
                break;
            case SUCCESS:
                binding.progress.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                List<Category> data = resource.data;
                if (data == null || data.isEmpty()) {
                    showEmpty();
                } else {
                    binding.stateView.getRoot().setVisibility(View.GONE);
                    adapter.submitList(data);
                }
                break;
            case ERROR:
                binding.progress.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                if (adapter.getItemCount() == 0) {
                    showError();
                }
                break;
        }
    }

    private void showEmpty() {
        binding.stateView.getRoot().setVisibility(View.VISIBLE);
        binding.stateView.ivState.setImageResource(R.drawable.ic_wallpaper);
        binding.stateView.tvStateTitle.setText(R.string.error_no_categories);
        binding.stateView.tvStateMessage.setText(R.string.app_tagline);
        binding.stateView.btnStateAction.setText(R.string.action_retry);
        binding.stateView.btnStateAction.setOnClickListener(v -> onRefresh());
    }

    private void showError() {
        binding.stateView.getRoot().setVisibility(View.VISIBLE);
        binding.stateView.ivState.setImageResource(R.drawable.ic_cloud_off);
        binding.stateView.tvStateTitle.setText(R.string.error_generic);
        binding.stateView.tvStateMessage.setText(R.string.error_load_failed);
        binding.stateView.btnStateAction.setText(R.string.action_retry);
        binding.stateView.btnStateAction.setOnClickListener(v -> onRefresh());
    }

    @Override
    public void onCategoryClick(int index, @NonNull Category category) {
        analytics.logCategoryOpen(category.getCategoryName());
        Intent intent = new Intent(requireContext(), WallpaperListActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_INDEX, index);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
