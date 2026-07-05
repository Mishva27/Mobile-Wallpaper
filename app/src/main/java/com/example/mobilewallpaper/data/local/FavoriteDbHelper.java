package com.example.mobilewallpaper.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.example.mobilewallpaper.data.model.Wallpaper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite persistence for favorite wallpapers. Exposed as a process-wide singleton
 * so a single writable database is shared across screens.
 */
public class FavoriteDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "favorites.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE = "favorites";
    private static final String COL_ID = "_id";
    private static final String COL_URL = "image_url";
    private static final String COL_CATEGORY = "category_name";
    private static final String COL_IS_PRO = "is_pro";
    private static final String COL_CREATED = "created_at";

    private static volatile FavoriteDbHelper instance;

    public static synchronized FavoriteDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FavoriteDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    private FavoriteDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_URL + " TEXT NOT NULL UNIQUE, "
                + COL_CATEGORY + " TEXT, "
                + COL_IS_PRO + " INTEGER NOT NULL DEFAULT 0, "
                + COL_CREATED + " INTEGER NOT NULL DEFAULT 0"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple migration strategy for v1; extend when schema evolves.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    /** Inserts (or replaces) a wallpaper as a favorite. Returns true on success. */
    public boolean addFavorite(@NonNull Wallpaper wallpaper) {
        if (wallpaper.getImageUrl() == null) return false;
        ContentValues values = new ContentValues();
        values.put(COL_URL, wallpaper.getImageUrl());
        values.put(COL_CATEGORY, wallpaper.getCategoryName());
        values.put(COL_IS_PRO, wallpaper.isPro() ? 1 : 0);
        values.put(COL_CREATED, System.currentTimeMillis());
        long id = getWritableDatabase()
                .insertWithOnConflict(TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return id != -1;
    }

    public void removeFavorite(String imageUrl) {
        if (imageUrl == null) return;
        getWritableDatabase().delete(TABLE, COL_URL + " = ?", new String[]{imageUrl});
    }

    public boolean isFavorite(String imageUrl) {
        if (imageUrl == null) return false;
        try (Cursor cursor = getReadableDatabase().query(
                TABLE, new String[]{COL_ID}, COL_URL + " = ?",
                new String[]{imageUrl}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    /** Toggles favorite state and returns the new state (true = now favorite). */
    public boolean toggleFavorite(@NonNull Wallpaper wallpaper) {
        if (isFavorite(wallpaper.getImageUrl())) {
            removeFavorite(wallpaper.getImageUrl());
            return false;
        }
        addFavorite(wallpaper);
        return true;
    }

    /** Returns all favorites, newest first. */
    @NonNull
    public List<Wallpaper> getAllFavorites() {
        List<Wallpaper> result = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(
                TABLE, null, null, null, null, null, COL_CREATED + " DESC")) {
            int urlIdx = c.getColumnIndexOrThrow(COL_URL);
            int catIdx = c.getColumnIndexOrThrow(COL_CATEGORY);
            int proIdx = c.getColumnIndexOrThrow(COL_IS_PRO);
            while (c.moveToNext()) {
                result.add(new Wallpaper(
                        c.getString(urlIdx),
                        c.getInt(proIdx) == 1,
                        c.getString(catIdx)));
            }
        }
        return result;
    }
}
