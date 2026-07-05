package com.example.mobilewallpaper.data.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * A wallpaper category node from Firebase Realtime Database.
 * Not parcelled between screens; the loaded list is cached in the repository
 * and referenced by index to avoid large intent payloads.
 */
@IgnoreExtraProperties
public class Category {

    private String category_name;
    private String category_image;
    private boolean is_active;
    private List<Wallpaper> wallpapers = new ArrayList<>();

    public Category() {
    }

    @PropertyName("category_name")
    public String getCategoryName() {
        return category_name;
    }

    @PropertyName("category_name")
    public void setCategoryName(String categoryName) {
        this.category_name = categoryName;
    }

    @PropertyName("category_image")
    public String getCategoryImage() {
        return category_image;
    }

    @PropertyName("category_image")
    public void setCategoryImage(String categoryImage) {
        this.category_image = categoryImage;
    }

    @PropertyName("is_active")
    public boolean isActive() {
        return is_active;
    }

    @PropertyName("is_active")
    public void setActive(boolean active) {
        this.is_active = active;
    }

    @PropertyName("wallpapers")
    public List<Wallpaper> getWallpapers() {
        return wallpapers;
    }

    @PropertyName("wallpapers")
    public void setWallpapers(List<Wallpaper> wallpapers) {
        this.wallpapers = wallpapers;
    }

    /** Number of usable wallpapers, guarding against null entries. */
    @Exclude
    public int getWallpaperCount() {
        return wallpapers == null ? 0 : wallpapers.size();
    }

    @NonNull
    @Override
    public String toString() {
        return category_name == null ? "" : category_name;
    }
}
