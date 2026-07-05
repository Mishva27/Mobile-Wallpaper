package com.example.mobilewallpaper.ui.list;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Pager adapter that maps each tab position to the original repository index of a
 * category, so every tab hosts a {@link WallpaperGridFragment} for that category.
 */
public class CategoryPagerAdapter extends FragmentStateAdapter {

    private final int[] categoryIndices;

    public CategoryPagerAdapter(@NonNull FragmentActivity activity, @NonNull int[] categoryIndices) {
        super(activity);
        this.categoryIndices = categoryIndices;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return WallpaperGridFragment.newInstance(categoryIndices[position]);
    }

    @Override
    public int getItemCount() {
        return categoryIndices.length;
    }

    public int getCategoryIndex(int position) {
        return categoryIndices[position];
    }
}
