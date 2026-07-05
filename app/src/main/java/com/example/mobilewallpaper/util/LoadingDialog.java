package com.example.mobilewallpaper.util;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.databinding.DialogProgressBinding;

/**
 * Reusable, non-cancelable progress dialog. One instance per screen; call
 * {@link #show(int)} / {@link #dismiss()} around any blocking async work so
 * progress presentation is consistent app-wide.
 */
public class LoadingDialog {

    private final Dialog dialog;
    private final TextView messageView;

    public LoadingDialog(@NonNull Context context) {
        DialogProgressBinding binding =
                DialogProgressBinding.inflate(LayoutInflater.from(context));
        messageView = binding.tvProgressMessage;

        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    public void show(@StringRes int messageRes) {
        messageView.setText(messageRes);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void show() {
        show(R.string.progress_please_wait);
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }
}
