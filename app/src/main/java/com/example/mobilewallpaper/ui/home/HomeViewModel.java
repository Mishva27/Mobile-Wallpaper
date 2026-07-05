package com.example.mobilewallpaper.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.mobilewallpaper.data.model.Category;
import com.example.mobilewallpaper.data.model.Resource;
import com.example.mobilewallpaper.data.repository.WallpaperRepository;
import com.example.mobilewallpaper.util.RemoteConfigManager;

import java.util.List;

/**
 * Owns the category load lifecycle for the Home screen and survives configuration
 * changes so rotation doesn't re-trigger a network fetch.
 */
public class HomeViewModel extends ViewModel {

    private final WallpaperRepository repository = WallpaperRepository.getInstance();
    private boolean hasLoaded;

    public LiveData<Resource<List<Category>>> getCategories() {
        return repository.getCategories();
    }

    /** Loads categories once automatically; use {@link #refresh()} for pull-to-refresh. */
    public void loadIfNeeded() {
        if (!hasLoaded) {
            hasLoaded = true;
            refresh();
        }
    }

    public void refresh() {
        String rootPath = RemoteConfigManager.getInstance().getDbRootPath();
        repository.load(rootPath);
    }

    @NonNull
    public List<Category> getCachedCategories() {
        return repository.getCachedCategories();
    }
}
