package com.example.mobilewallpaper.ui.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mobilewallpaper.BuildConfig;
import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.databinding.FragmentSettingsBinding;
import com.example.mobilewallpaper.util.InsetUtils;

/**
 * Settings tab: lets the user rate the app on the Play Store, share it, and see the
 * installed version. Purely local actions — no network or persisted state.
 */
public class SettingsFragment extends Fragment {

    private static final String MARKET_URI = "market://details?id=";
    private static final String WEB_URI = "https://play.google.com/store/apps/details?id=";

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetUtils.applyTopInset(binding.toolbar);
        InsetUtils.applyBottomInset(binding.settingsScroll);

        binding.tvVersion.setText(BuildConfig.VERSION_NAME);

        binding.rowRate.setOnClickListener(v -> openPlayStore());
        binding.rowShare.setOnClickListener(v -> shareApp());
    }

    /** Opens the app's Play Store listing, falling back to the web URL. */
    private void openPlayStore() {
        String pkg = requireContext().getPackageName();
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI + pkg));
        market.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(market);
        } catch (ActivityNotFoundException notInstalled) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(WEB_URI + pkg)));
            } catch (ActivityNotFoundException noBrowser) {
                Toast.makeText(requireContext(), R.string.error_no_store, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Fires the system share sheet with a link to the app's Play Store listing. */
    private void shareApp() {
        String pkg = requireContext().getPackageName();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_subject));
        send.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text, pkg));
        startActivity(Intent.createChooser(send, getString(R.string.settings_share)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
