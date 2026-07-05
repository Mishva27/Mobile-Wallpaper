package com.example.mobilewallpaper.ui.list;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.model.Category;
import com.example.mobilewallpaper.data.model.Resource;
import com.example.mobilewallpaper.data.repository.WallpaperRepository;
import com.example.mobilewallpaper.databinding.ActivityWallpaperListBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.RemoteConfigManager;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

/**
 * Tabbed list screen. Shows one tab per active category with the tapped category
 * moved to the front (index 0) and pre-selected, per the product requirement.
 * Rebuilds from a fresh fetch if the in-memory cache was cleared (process death).
 */
public class WallpaperListActivity extends BaseActivity {

    private ActivityWallpaperListBinding binding;
    private final WallpaperRepository repository = WallpaperRepository.getInstance();
    private int selectedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityWallpaperListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyTopInset(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        selectedIndex = getIntent().getIntExtra(Constants.EXTRA_CATEGORY_INDEX, 0);

        if (repository.getCachedCategories().isEmpty()) {
            reloadThenBuild();
        } else {
            buildTabs(repository.getCachedCategories());
        }
    }

    private void reloadThenBuild() {
        binding.progress.setVisibility(View.VISIBLE);
        repository.getCategories().observe(this, new androidx.lifecycle.Observer<Resource<List<Category>>>() {
            @Override
            public void onChanged(Resource<List<Category>> resource) {
                if (resource.status == Resource.Status.SUCCESS) {
                    binding.progress.setVisibility(View.GONE);
                    repository.getCategories().removeObserver(this);
                    if (!repository.getCachedCategories().isEmpty()) {
                        buildTabs(repository.getCachedCategories());
                    } else {
                        finish();
                    }
                } else if (resource.status == Resource.Status.ERROR) {
                    binding.progress.setVisibility(View.GONE);
                    repository.getCategories().removeObserver(this);
                    finish();
                }
            }
        });
        repository.load(RemoteConfigManager.getInstance().getDbRootPath());
    }

    private void buildTabs(@NonNull List<Category> categories) {
        if (selectedIndex < 0 || selectedIndex >= categories.size()) {
            selectedIndex = 0;
        }

        // Ordered indices: selected category first, then the rest in original order.
        int[] order = new int[categories.size()];
        order[0] = selectedIndex;
        int cursor = 1;
        for (int i = 0; i < categories.size(); i++) {
            if (i != selectedIndex) {
                order[cursor++] = i;
            }
        }

        binding.toolbar.setTitle(categories.get(selectedIndex).getCategoryName());

        CategoryPagerAdapter adapter = new CategoryPagerAdapter(this, order);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(1);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) ->
                tab.setText(categories.get(adapter.getCategoryIndex(position)).getCategoryName())
        ).attach();

        binding.viewPager.setCurrentItem(0, false);
    }
}
