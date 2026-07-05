package com.example.mobilewallpaper.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Minimal executor holder: a small background pool for disk/network/bitmap work
 * and a main-thread executor for delivering results back to the UI.
 */
public final class AppExecutors {

    private static final AppExecutors INSTANCE = new AppExecutors();

    private final ExecutorService background = Executors.newFixedThreadPool(3);
    private final Executor mainThread = new MainThreadExecutor();

    private AppExecutors() {
    }

    public static AppExecutors get() {
        return INSTANCE;
    }

    public ExecutorService background() {
        return background;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }
}
