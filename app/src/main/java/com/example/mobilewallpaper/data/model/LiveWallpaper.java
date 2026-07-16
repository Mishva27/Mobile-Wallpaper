package com.example.mobilewallpaper.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * A live (video) wallpaper entry from the Firebase Realtime Database
 * {@code live_wallpaper} node. The DB stores {@code image_url} (the video URL) and
 * {@code is_pro}; the display name is assigned when the list is loaded.
 */
@IgnoreExtraProperties
public class LiveWallpaper implements Parcelable {

    private String image_url;
    private boolean is_pro;

    /** Display title. Not stored in the DB node itself. */
    @Exclude
    private String name;

    /** Required no-arg constructor for Firebase deserialization. */
    public LiveWallpaper() {
    }

    public LiveWallpaper(String videoUrl, boolean isPro, String name) {
        this.image_url = videoUrl;
        this.is_pro = isPro;
        this.name = name;
    }

    /** The video URL (stored under {@code image_url} in the DB). */
    @PropertyName("image_url")
    public String getVideoUrl() {
        return image_url;
    }

    @PropertyName("image_url")
    public void setVideoUrl(String videoUrl) {
        this.image_url = videoUrl;
    }

    @PropertyName("is_pro")
    public boolean isPro() {
        return is_pro;
    }

    @PropertyName("is_pro")
    public void setPro(boolean pro) {
        this.is_pro = pro;
    }

    @Exclude
    public String getName() {
        return name;
    }

    @Exclude
    public void setName(String name) {
        this.name = name;
    }

    // ---- Parcelable ----

    protected LiveWallpaper(Parcel in) {
        image_url = in.readString();
        is_pro = in.readByte() != 0;
        name = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(image_url);
        dest.writeByte((byte) (is_pro ? 1 : 0));
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LiveWallpaper> CREATOR = new Creator<LiveWallpaper>() {
        @Override
        public LiveWallpaper createFromParcel(Parcel in) {
            return new LiveWallpaper(in);
        }

        @Override
        public LiveWallpaper[] newArray(int size) {
            return new LiveWallpaper[size];
        }
    };
}
