package com.example.mobilewallpaper.util;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Static window-inset helpers for use from Fragments (the instance helpers on
 * {@code BaseActivity} aren't reachable there). Each helper leaves the insets
 * unconsumed so sibling views still receive them.
 */
public final class InsetUtils {

    private InsetUtils() {
    }

    /** Pads the view with the top system-bar inset (e.g. a fragment toolbar). */
    public static void applyTopInset(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /** Adds the bottom system-bar inset on top of the view's existing bottom padding. */
    public static void applyBottomInset(@NonNull View view) {
        final int basePadding = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    basePadding + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }

    /** Adds the bottom system-bar inset to the view's bottom margin (lifts it above the nav bar). */
    public static void applyBottomInsetMargin(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) return;
        final int baseMargin = ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin;
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = baseMargin + bars.bottom;
            v.setLayoutParams(lp);
            return insets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}
