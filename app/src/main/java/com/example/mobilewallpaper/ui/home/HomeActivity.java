package com.example.mobilewallpaper.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.databinding.ActivityHomeBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.ui.favorite.FavoriteFragment;
import com.example.mobilewallpaper.ui.live.LiveWallpaperFragment;
import com.example.mobilewallpaper.ui.settings.SettingsFragment;
import com.example.mobilewallpaper.util.DialogUtils;

/**
 * Bottom-nav shell hosting the four top-level tabs (Wallpapers, Live, Favorites,
 * Settings) as fragments. Tabs are added once and toggled with show/hide so each
 * keeps its scroll position and state across switches.
 */
public class HomeActivity extends BaseActivity {

    private static final String TAG_WALLPAPERS = "tab_wallpapers";
    private static final String TAG_LIVE = "tab_live";
    private static final String TAG_FAVORITES = "tab_favorites";
    private static final String TAG_SETTINGS = "tab_settings";
    private static final String STATE_SELECTED = "selected_tab";

    private ActivityHomeBinding binding;

    private Fragment wallpapersFragment;
    private Fragment liveFragment;
    private Fragment favoritesFragment;
    private Fragment settingsFragment;
    private Fragment activeFragment;

    private View[] navItems;
    private int selectedTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyBottomInsetMargin(binding.bottomNav.getRoot());

        navItems = new View[]{
                binding.bottomNav.navWallpapers,
                binding.bottomNav.navLive,
                binding.bottomNav.navFavorites,
                binding.bottomNav.navSettings
        };

        setupFragments(savedInstanceState);
        setupNav();
        setupBackPress();

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(STATE_SELECTED, 0);
        }
        selectTab(selectedTab);
    }

    private void setupFragments(Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();

        wallpapersFragment = fm.findFragmentByTag(TAG_WALLPAPERS);
        liveFragment = fm.findFragmentByTag(TAG_LIVE);
        favoritesFragment = fm.findFragmentByTag(TAG_FAVORITES);
        settingsFragment = fm.findFragmentByTag(TAG_SETTINGS);

        if (savedInstanceState == null) {
            wallpapersFragment = new WallpaperFragment();
            liveFragment = new LiveWallpaperFragment();
            favoritesFragment = new FavoriteFragment();
            settingsFragment = new SettingsFragment();

            fm.beginTransaction()
                    .add(R.id.fragmentContainer, wallpapersFragment, TAG_WALLPAPERS)
                    .add(R.id.fragmentContainer, liveFragment, TAG_LIVE)
                    .add(R.id.fragmentContainer, favoritesFragment, TAG_FAVORITES)
                    .add(R.id.fragmentContainer, settingsFragment, TAG_SETTINGS)
                    .hide(liveFragment)
                    .hide(favoritesFragment)
                    .hide(settingsFragment)
                    .commit();
        }
        activeFragment = wallpapersFragment;
    }

    private void setupNav() {
        binding.bottomNav.navWallpapers.setOnClickListener(v -> selectTab(0));
        binding.bottomNav.navLive.setOnClickListener(v -> selectTab(1));
        binding.bottomNav.navFavorites.setOnClickListener(v -> selectTab(2));
        binding.bottomNav.navSettings.setOnClickListener(v -> selectTab(3));
    }

    private void selectTab(int index) {
        Fragment target = fragmentForIndex(index);
        if (target == null) return;

        if (activeFragment != null && activeFragment != target) {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            tx.hide(activeFragment).show(target).commit();
        }
        activeFragment = target;
        selectedTab = index;

        for (int i = 0; i < navItems.length; i++) {
            navItems[i].setSelected(i == index);
        }
    }

    private Fragment fragmentForIndex(int index) {
        switch (index) {
            case 1:
                return liveFragment;
            case 2:
                return favoritesFragment;
            case 3:
                return settingsFragment;
            case 0:
            default:
                return wallpapersFragment;
        }
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (selectedTab != 0) {
                    selectTab(0);
                } else {
                    DialogUtils.showExit(HomeActivity.this, HomeActivity.this::finishAffinity);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED, selectedTab);
    }
}
