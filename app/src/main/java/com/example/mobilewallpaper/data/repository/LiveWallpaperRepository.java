package com.example.mobilewallpaper.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mobilewallpaper.data.model.LiveWallpaper;
import com.example.mobilewallpaper.data.model.Resource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads live (video) wallpapers from the Firebase Realtime Database
 * {@code live_wallpaper} node. Kept separate from {@link WallpaperRepository} so
 * the still-image flow is unaffected.
 */
public class LiveWallpaperRepository {

    private static final String NODE = "live_wallpaper";

    private static volatile LiveWallpaperRepository instance;

    private final MutableLiveData<Resource<List<LiveWallpaper>>> liveWallpapers =
            new MutableLiveData<>();

    private LiveWallpaperRepository() {
    }

    public static LiveWallpaperRepository getInstance() {
        if (instance == null) {
            synchronized (LiveWallpaperRepository.class) {
                if (instance == null) {
                    instance = new LiveWallpaperRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<Resource<List<LiveWallpaper>>> getLiveWallpapers() {
        return liveWallpapers;
    }

    /** Fetches the live wallpapers once. Emits LOADING, then SUCCESS or ERROR. */
    public void load() {
        liveWallpapers.setValue(Resource.loading());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(NODE);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LiveWallpaper> parsed = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    LiveWallpaper item = child.getValue(LiveWallpaper.class);
                    if (item == null || item.getVideoUrl() == null) continue;
                    item.setName("Live Wallpaper " + (parsed.size() + 1));
                    parsed.add(item);
                }
                liveWallpapers.setValue(Resource.success(parsed));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveWallpapers.setValue(Resource.error(error.getMessage()));
            }
        });
    }
}
