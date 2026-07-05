package com.example.mobilewallpaper.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobilewallpaper.data.model.Category;
import com.example.mobilewallpaper.data.model.Resource;
import com.example.mobilewallpaper.data.model.Wallpaper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Single source of truth for wallpaper categories loaded from Firebase Realtime Database.
 * The successfully loaded list is cached in-memory so downstream screens (list/detail)
 * can reference categories by index instead of passing large payloads through intents.
 */
public class WallpaperRepository {

    private static volatile WallpaperRepository instance;

    private final MutableLiveData<Resource<List<Category>>> categories = new MutableLiveData<>();
    private final List<Category> cache = new ArrayList<>();

    private WallpaperRepository() {
    }

    public static WallpaperRepository getInstance() {
        if (instance == null) {
            synchronized (WallpaperRepository.class) {
                if (instance == null) {
                    instance = new WallpaperRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<Resource<List<Category>>> getCategories() {
        return categories;
    }

    /** Returns the cached categories (may be empty if not yet loaded). */
    @NonNull
    public List<Category> getCachedCategories() {
        return cache;
    }

    @Nullable
    public Category getCategoryAt(int index) {
        if (index < 0 || index >= cache.size()) return null;
        return cache.get(index);
    }

    /**
     * Fetches categories once from the given DB root path. Emits LOADING immediately,
     * then SUCCESS with active categories or ERROR on failure.
     */
    public void load(@NonNull String rootPath) {
        categories.setValue(Resource.loading());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(rootPath);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> parsed = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Category category = child.getValue(Category.class);
                    if (category == null || !category.isActive()) continue;
                    attachCategoryNameToWallpapers(category);
                    if (category.getWallpaperCount() > 0) {
                        parsed.add(category);
                    }
                }
                cache.clear();
                cache.addAll(parsed);
                categories.setValue(Resource.success(parsed));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                categories.setValue(Resource.error(error.getMessage()));
            }
        });
    }

    private void attachCategoryNameToWallpapers(@NonNull Category category) {
        List<Wallpaper> wallpapers = category.getWallpapers();
        if (wallpapers == null) return;
        List<Wallpaper> cleaned = new ArrayList<>(wallpapers.size());
        for (Wallpaper wallpaper : wallpapers) {
            if (wallpaper == null || wallpaper.getImageUrl() == null) continue;
            wallpaper.setCategoryName(category.getCategoryName());
            cleaned.add(wallpaper);
        }
        category.setWallpapers(cleaned);
    }
}
