# Add project specific ProGuard rules here.

# Keep line numbers for readable Crashlytics stack traces.
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# --- Firebase Realtime Database POJOs ---
# These are populated via reflection; keep their members and no-arg constructors.
-keepclassmembers class com.example.mobilewallpaper.data.model.** {
    <init>();
    <fields>;
    <methods>;
}
-keep class com.example.mobilewallpaper.data.model.** { *; }

# Firebase database generic type indicators
-keepnames class com.google.firebase.database.** { *; }
-keepclassmembers class com.google.firebase.** { *; }

# --- Glide ---
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
