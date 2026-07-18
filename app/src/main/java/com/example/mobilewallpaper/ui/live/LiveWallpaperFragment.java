package com.example.mobilewallpaper.ui.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.mobilewallpaper.R;
import com.example.mobilewallpaper.data.model.LiveWallpaper;
import com.example.mobilewallpaper.data.model.Resource;
import com.example.mobilewallpaper.data.repository.LiveWallpaperRepository;
import com.example.mobilewallpaper.databinding.FragmentLiveWallpaperBinding;
import com.example.mobilewallpaper.ui.common.GridSpacingItemDecoration;
import com.example.mobilewallpaper.util.Constants;
import com.example.mobilewallpaper.util.InsetUtils;
import com.example.mobilewallpaper.util.NetworkUtils;

import java.util.List;

/**
 * Live wallpapers tab: grid of live wallpapers loaded from the Firebase Realtime
 * Database. Tapping one opens the live-wallpaper detail screen. Mirrors the logic
 * of {@link LiveWallpaperListActivity} for use inside the bottom-nav shell.
 */
public class LiveWallpaperFragment extends Fragment
        implements LiveWallpaperAdapter.OnLiveWallpaperClick {

    private static final int SPAN_COUNT = 2;

    private FragmentLiveWallpaperBinding binding;
    private LiveWallpaperAdapter adapter;
    private LiveWallpaperRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLiveWallpaperBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        InsetUtils.applyTopInset(binding.toolbar);
        InsetUtils.applyBottomInset(binding.rvLive);

        adapter = new LiveWallpaperAdapter(this);
        binding.rvLive.setLayoutManager(
                new StaggeredGridLayoutManager(SPAN_COUNT, StaggeredGridLayoutManager.VERTICAL));
        binding.rvLive.addItemDecoration(new GridSpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_gap)));
        binding.rvLive.setAdapter(adapter);

        repository = LiveWallpaperRepository.getInstance();
        repository.getLiveWallpapers().observe(getViewLifecycleOwner(), this::render);
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
            if (!NetworkUtils.isConnected(requireContext())) {
                return;
            }
            repository.load();
        });
    }

    @Override
    public void onLiveWallpaperClick(@NonNull LiveWallpaper item) {
        Intent intent = new Intent(requireContext(), LiveWallpaperDetailActivity.class);
        intent.putExtra(Constants.EXTRA_LIVE_WALLPAPER, item);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
