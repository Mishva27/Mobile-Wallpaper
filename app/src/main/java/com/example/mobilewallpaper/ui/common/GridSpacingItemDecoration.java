package com.example.mobilewallpaper.ui.common;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Uniform spacing for staggered/grid recyclers. Applies an equal gap on all four
 * sides of every item so columns line up regardless of variable item heights.
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int half;

    public GridSpacingItemDecoration(int spacingPx) {
        this.half = spacingPx / 2;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.set(half, half, half, half);
    }
}
