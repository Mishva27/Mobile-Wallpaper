package com.example.mobilewallpaper.ui.base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Base for all activities: enables edge-to-edge (full-screen) drawing and offers
 * shared helpers for applying window insets and showing toasts.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Enable edge-to-edge before content is set. Call from subclass onCreate. */
    protected void enableEdgeToEdge() {
        EdgeToEdge.enable(this);
    }

    /** Pads the given view with the top system-bar inset (e.g. a toolbar). */
    protected void applyTopInset(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
    }

    /** Pads the given view with the bottom system-bar inset (e.g. a scroll container). */
    protected void applyBottomInset(@NonNull View view) {
        final int basePadding = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    basePadding + bars.bottom);
            return insets;
        });
    }

    /** Adds the top system-bar inset to the view's top margin (keeps its size intact). */
    protected void applyTopInsetMargin(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) return;
        final int baseMargin = ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin;
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.topMargin = baseMargin + bars.top;
            v.setLayoutParams(lp);
            return insets;
        });
    }

    /** Adds the bottom system-bar inset to the view's bottom margin (lifts it above the nav bar). */
    protected void applyBottomInsetMargin(@NonNull View view) {
        if (!(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) return;
        final int baseMargin = ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin;
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = baseMargin + bars.bottom;
            v.setLayoutParams(lp);
            return insets;
        });
    }

    protected void toast(@StringRes int messageRes) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
    }
}
