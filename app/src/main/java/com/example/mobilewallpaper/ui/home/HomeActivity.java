package com.example.mobilewallpaper.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.model.Category;
import com.example.mobilewallpaper.data.model.Resource;
import com.example.mobilewallpaper.databinding.ActivityHomeBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.ui.common.GridSpacingItemDecoration;
import com.example.mobilewallpaper.ui.favorite.FavoriteActivity;
import com.example.mobilewallpaper.ui.list.WallpaperListActivity;
import com.example.mobilewallpaper.util.AnalyticsHelper;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.DialogUtils;
import com.example.mobilewallpaper.util.NetworkUtils;
import com.example.mobilewallpaper.util.RemoteConfigManager;

import java.util.List;

/**
 * Home screen: shows active wallpaper categories in a staggered grid. Tapping a
 * category opens the tabbed list screen with that category pre-selected.
 */
public class HomeActivity extends BaseActivity implements CategoryAdapter.OnCategoryClick {

    private ActivityHomeBinding binding;
    private HomeViewModel viewModel;
    private CategoryAdapter adapter;
    private AnalyticsHelper analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyTopInset(binding.toolbar);
        applyBottomInset(binding.rvCategories);

        analytics = new AnalyticsHelper(this);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupToolbar();
        setupRecycler();
        setupBackPress();
        observe();

        viewModel.loadIfNeeded();
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DialogUtils.showExit(HomeActivity.this, HomeActivity.this::finishAffinity);
            }
        });
    }

    private void setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_favorites) {
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            }
            return false;
        });
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
        if (!NetworkUtils.isConnected(this)) {
            binding.swipeRefresh.setRefreshing(false);
            DialogUtils.showNoInternet(this, this::onRefresh);
            return;
        }
        viewModel.refresh();
    }

    private void observe() {
        viewModel.getCategories().observe(this, this::render);
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
        Intent intent = new Intent(this, WallpaperListActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_INDEX, index);
        startActivity(intent);
    }
}
