package dev.kevinillanas.plugin.wallpaper;

import android.content.Context;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

public class Wallpaper {
    // Constants for wallpaper targets
    public static final String TARGET_HOME = "home"; // Home screen wallpaper
    public static final String TARGET_LOCK = "lock"; // Lock screen wallpaper
    public static final String TARGET_BOTH = "both"; // Both screens wallpaper
    
    /**
     * Applies the Bitmap as wallpaper based on the specified target.
     * @param context The context to apply the wallpaper to.
     * @param bitmap The Bitmap to apply as wallpaper.
     * @param target The target screen to apply the wallpaper to.
     */
    public void applyWallpaper(Context context, Bitmap bitmap, String target) throws IllegalArgumentException {
        // Ejecutar en el hilo principal para evitar conflictos
        Bitmap optimizedBitmap = bitmap;
        
        // Optimizar bitmap si es necesario
        if (bitmap.getWidth() > 1920 || bitmap.getHeight() > 1080) {
            float scale = Math.min(1920f / bitmap.getWidth(), 1080f / bitmap.getHeight());
            int newWidth = Math.round(bitmap.getWidth() * scale);
            int newHeight = Math.round(bitmap.getHeight() * scale);
            optimizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }

        // Configurar el WallpaperManager
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

        try {
            switch (target) {
                case TARGET_HOME:
                    wallpaperManager.setBitmap(optimizedBitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                    break;
                case TARGET_LOCK:
                    wallpaperManager.setBitmap(optimizedBitmap, null, true, WallpaperManager.FLAG_LOCK);
                    break;
                case TARGET_BOTH:
                    wallpaperManager.setBitmap(optimizedBitmap, null, true, 0);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid target");
            }
        } catch (IllegalArgumentException e) {
            Log.e("Wallpaper", "Error applying wallpaper: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e("Wallpaper", "Error applying wallpaper (IOException): " + e.getMessage(), e);
        }
    }
}