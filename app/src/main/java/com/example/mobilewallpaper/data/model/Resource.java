package com.example.mobilewallpaper.data.model;

import androidx.annotation.Nullable;

/**
 * A lightweight wrapper describing the state of an asynchronous load
 * (loading / success / error) that flows through LiveData to the UI.
 */
public class Resource<T> {

    public enum Status {LOADING, SUCCESS, ERROR}

    public final Status status;
    @Nullable
    public final T data;
    @Nullable
    public final String errorMessageKey;

    private Resource(Status status, @Nullable T data, @Nullable String errorMessageKey) {
        this.status = status;
        this.data = data;
        this.errorMessageKey = errorMessageKey;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(@Nullable String errorMessageKey) {
        return new Resource<>(Status.ERROR, null, errorMessageKey);
    }
}
