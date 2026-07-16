package com.example.mobilewallpaper.ui.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.model.LiveWallpaper;
import com.example.mobilewallpaper.data.model.Resource;
import com.example.mobilewallpaper.data.repository.LiveWallpaperRepository;
import com.example.mobilewallpaper.databinding.ActivityLiveWallpaperListBinding;
import com.example.mobilewallpaper.ui.base.BaseActivity;
import com.example.mobilewallpaper.ui.common.GridSpacingItemDecoration;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.NetworkUtils;

import java.util.List;

/**
 * Grid of live wallpapers loaded from the Firebase Realtime Database. Tapping one
 * opens the live-wallpaper detail screen with a looping video preview.
 */
public class LiveWallpaperListActivity extends BaseActivity
        implements LiveWallpaperAdapter.OnLiveWallpaperClick {

    private static final int SPAN_COUNT = 2;

    private ActivityLiveWallpaperListBinding binding;
    private LiveWallpaperAdapter adapter;
    private LiveWallpaperRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        enableEdgeToEdge();
        super.onCreate(savedInstanceState);
        binding = ActivityLiveWallpaperListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyTopInset(binding.toolbar);
        applyBottomInset(binding.rvLive);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new LiveWallpaperAdapter(this);
        binding.rvLive.setLayoutManager(
                new StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL));
        binding.rvLive.addItemDecoration(new GridSpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_gap)));
        binding.rvLive.setAdapter(adapter);

        repository = LiveWallpaperRepository.getInstance();
        repository.getLiveWallpapers().observe(this, this::render);
        repository.load();
    }

    private void render(@NonNull Resource<List<LiveWallpaper>> resource) {
        switch (resource.status) {
            case LOADING:
                binding.progress.setVisibility(
                        adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                binding.stateView.getRoot().setVisibility(View.GONE);
                break;
            case SUCCESS:
                binding.progress.setVisibility(View.GONE);
                List<LiveWallpaper> data = resource.data;
                if (data == null || data.isEmpty()) {
                    showEmpty();
                } else {
                    binding.stateView.getRoot().setVisibility(View.GONE);
                    adapter.submit(data);
                }
                break;
            case ERROR:
                binding.progress.setVisibility(View.GONE);
                if (adapter.getItemCount() == 0) {
                    showError();
                }
                break;
        }
    }

    private void showEmpty() {
        binding.stateView.getRoot().setVisibility(View.VISIBLE);
        binding.stateView.ivState.setImageResource(R.drawable.ic_live);
        binding.stateView.tvStateTitle.setText(R.string.empty_live_title);
        binding.stateView.tvStateMessage.setText(R.string.empty_live_message);
        binding.stateView.btnStateAction.setVisibility(View.GONE);
    }

    private void showError() {
        binding.stateView.getRoot().setVisibility(View.VISIBLE);
        binding.stateView.ivState.setImageResource(R.drawable.ic_cloud_off);
        binding.stateView.tvStateTitle.setText(R.string.error_generic);
        binding.stateView.tvStateMessage.setText(R.string.error_load_failed);
        binding.stateView.btnStateAction.setVisibility(View.VISIBLE);
        binding.stateView.btnStateAction.setText(R.string.action_retry);
        binding.stateView.btnStateAction.setOnClickListener(v -> {
            if (!NetworkUtils.isConnected(this)) {
                return;
            }
            repository.load();
        });
    }

    @Override
    public void onLiveWallpaperClick(@NonNull LiveWallpaper item) {
        Intent intent = new Intent(this, LiveWallpaperDetailActivity.class);
        intent.putExtra(Constants.EXTRA_LIVE_WALLPAPER, item);
        startActivity(intent);
    }
}
