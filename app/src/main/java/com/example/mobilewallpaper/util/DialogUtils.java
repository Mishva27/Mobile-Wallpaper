package com.example.mobilewallpaper.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.databinding.DialogExitBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Centralized, reusable dialogs (no-internet, generic error/info) so no screen
 * hardcodes dialog text or duplicates dialog wiring.
 */
public final class DialogUtils {

    private DialogUtils() {
    }

    /** Simple callback used by the retry dialog. */
    public interface OnRetry {
        void onRetry();
    }

    /**
     * Shows the standard "no internet connection" dialog with Retry / Cancel.
     */
    public static void showNoInternet(@NonNull Context context, @NonNull OnRetry onRetry) {
        new MaterialAlertDialogBuilder(context)
                .setIcon(R.drawable.ic_cloud_off)
                .setTitle(R.string.no_internet_title)
                .setMessage(R.string.no_internet_message)
                .setCancelable(false)
                .setPositiveButton(R.string.action_retry, (d, w) -> onRetry.onRetry())
                .setNegativeButton(R.string.action_cancel, (d, w) -> d.dismiss())
                .show();
    }

    /**
     * Shows a themed exit confirmation dialog (illustration + title + subtitle).
     * Invokes {@code onExit} only when the user confirms.
     */
    public static void showExit(@NonNull Context context, @NonNull Runnable onExit) {
        DialogExitBinding binding = DialogExitBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(binding.getRoot())
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Fix the dialog to 86% of screen width so there is even spacing on both sides.
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            dialog.getWindow().setLayout(
                    (int) (screenWidth * 0.86f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        AnimatorSet pulse = buildImagePulse(binding.ivExit);
        dialog.setOnDismissListener(d -> pulse.cancel());

        binding.btnStay.setOnClickListener(v -> dialog.dismiss());
        binding.btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            onExit.run();
        });
        dialog.show();
    }

    /** Pops the illustration in on show, then keeps it gently pulsing so it feels alive. */
    private static AnimatorSet buildImagePulse(@NonNull ImageView image) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(image, "scaleX", 1f, 1.08f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(image, "scaleY", 1f, 1.08f);
        for (ObjectAnimator anim : new ObjectAnimator[]{scaleX, scaleY}) {
            anim.setDuration(900);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setRepeatMode(ValueAnimator.REVERSE);
        }
        AnimatorSet pulse = new AnimatorSet();
        pulse.playTogether(scaleX, scaleY);
        pulse.setInterpolator(new AccelerateDecelerateInterpolator());

        // Entrance pop, then start the continuous pulse.
        image.setScaleX(0.4f);
        image.setScaleY(0.4f);
        image.setAlpha(0f);
        image.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setStartDelay(80)
                .setDuration(420)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(pulse::start)
                .start();
        return pulse;
    }

    /** Generic error dialog with a single OK button. */
    public static void showError(@NonNull Context context, @StringRes int messageRes) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error_generic)
                .setMessage(messageRes)
                .setPositiveButton(R.string.action_ok, (d, w) -> d.dismiss())
                .show();
    }

    /** Error dialog offering a Retry action. */
    public static void showErrorWithRetry(@NonNull Context context,
                                          @StringRes int messageRes,
                                          @NonNull OnRetry onRetry) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error_generic)
                .setMessage(messageRes)
                .setCancelable(false)
                .setPositiveButton(R.string.action_retry, (d, w) -> onRetry.onRetry())
                .setNegativeButton(R.string.action_cancel, (d, w) -> d.dismiss())
                .show();
    }
}
