package com.example.mobilewallpaper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites Google Drive share/download links into Drive's direct image endpoint.
 *
 * <p>Links like {@code drive.usercontent.google.com/uc?id=FILE_ID&export=download}
 * (and {@code drive.google.com/uc?export=download&id=...}) often serve an HTML
 * "confirm download / virus scan" interstitial rather than raw image bytes, which
 * fails to decode as a bitmap. The {@code drive.google.com/thumbnail?id=...&sz=w<width>}
 * endpoint returns actual (resized) image bytes for publicly shared files.</p>
 */
public final class DriveUrl {

    /** Width used for grid thumbnails. */
    public static final int WIDTH_GRID = 1080;
    /** Width used for full-screen preview and when applying/sharing a wallpaper. */
    public static final int WIDTH_FULL = 2048;

    private static final Pattern ID_QUERY = Pattern.compile("[?&]id=([a-zA-Z0-9_-]+)");
    private static final Pattern ID_PATH = Pattern.compile("/d/([a-zA-Z0-9_-]+)");

    private DriveUrl() {
    }

    /**
     * Returns a directly-decodable image URL. Google Drive links are rewritten to the
     * thumbnail endpoint at the requested width; all other URLs are returned unchanged.
     */
    public static String normalize(String url, int width) {
        if (url == null) return null;
        if (!url.contains("drive.google.com") && !url.contains("drive.usercontent.google.com")) {
            return url;
        }
        String id = extractId(url);
        if (id == null) return url;
        return "https://drive.google.com/thumbnail?id=" + id + "&sz=w" + width;
    }

    private static String extractId(String url) {
        Matcher query = ID_QUERY.matcher(url);
        if (query.find()) return query.group(1);
        Matcher path = ID_PATH.matcher(url);
        if (path.find()) return path.group(1);
        return null;
    }
}
