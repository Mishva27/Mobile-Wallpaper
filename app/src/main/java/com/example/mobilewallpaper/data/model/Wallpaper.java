package com.example.mobilewallpaper.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * A single wallpaper entry as stored in Firebase Realtime Database.
 * Uses {@link PropertyName} to bridge the DB's snake_case keys to Java fields,
 * and implements {@link Parcelable} so it can be handed to the detail screen.
 */
@IgnoreExtraProperties
public class Wallpaper implements Parcelable {

    private String image_url;
    private boolean is_pro;

    /** The category this wallpaper belongs to. Not stored in the DB node itself. */
    @Exclude
    private String categoryName;

    /** Required no-arg constructor for Firebase deserialization. */
    public Wallpaper() {
    }

    public Wallpaper(String imageUrl, boolean isPro, String categoryName) {
        this.image_url = imageUrl;
        this.is_pro = isPro;
        this.categoryName = categoryName;
    }

    @PropertyName("image_url")
    public String getImageUrl() {
        return image_url;
    }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) {
        this.image_url = imageUrl;
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
    public String getCategoryName() {
        return categoryName;
    }

    @Exclude
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    // ---- Parcelable ----

    protected Wallpaper(Parcel in) {
        image_url = in.readString();
        is_pro = in.readByte() != 0;
        categoryName = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(image_url);
        dest.writeByte((byte) (is_pro ? 1 : 0));
        dest.writeString(categoryName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Wallpaper> CREATOR = new Creator<Wallpaper>() {
        @Override
        public Wallpaper createFromParcel(Parcel in) {
            return new Wallpaper(in);
        }

        @Override
        public Wallpaper[] newArray(int size) {
            return new Wallpaper[size];
        }
    };
}
