package dev.kevinillanas.plugin.wallpaper;

import android.content.Context;
import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.io.IOException;

public class Wallpaper {

    // Context instance for accessing resources
    private Context context;

    // Constants for wallpaper targets
    public static final String TARGET_HOME = "home"; // Home screen wallpaper
    public static final String TARGET_LOCK = "lock"; // Lock screen wallpaper
    public static final String TARGET_BOTH = "both"; // Both screens wallpaper

    // Constants for display modes
    public static final String DISPLAY_FILL = "fill"; // Scale and crop to fill the screen
    public static final String DISPLAY_FIT = "fit"; // Fit without distortion
    public static final String DISPLAY_STRETCH = "stretch"; // Stretch to fit, may distort
    public static final String DISPLAY_CENTER = "center"; // Center without scaling

    // Constructor for the Wallpaper class
    Wallpaper(Context context) {
        this.context = context;
    }
    
    /**
     * Applies the Bitmap as wallpaper based on the specified target.
     * @param bitmap The Bitmap to apply as wallpaper.
     * @param target The target screen to apply the wallpaper to.
     * @param display The display mode to apply.
     */
    public void applyWallpaper(Bitmap bitmap, String target, String display) throws IOException {
        // Adapt the Bitmap based on the display mode
        Bitmap processedBitmap = adaptBitmapForDisplay(bitmap, display);

        // Set the processed Bitmap as wallpaper
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.context);

        // Apply the wallpaper based on the target
        switch (target) {
            case TARGET_HOME:
                wallpaperManager.setBitmap(processedBitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                break;
            case TARGET_LOCK:
                wallpaperManager.setBitmap(processedBitmap, null, true, WallpaperManager.FLAG_LOCK);
                break;
            case TARGET_BOTH:
                wallpaperManager.setBitmap(processedBitmap, null, true);
                break;
            default:
                throw new IOException("Invalid target");
        }

        // Release resources
        if (bitmap != null) bitmap = null;
        if (processedBitmap != null) processedBitmap = null;
        if (wallpaperManager != null) wallpaperManager = null;
    }

    /**
     * Adapts the Bitmap to the display mode specified.
     * @param bitmap The Bitmap to adapt.
     * @param display The display mode to apply.
     * @return The adapted Bitmap based on the display mode.
     */
    public Bitmap adaptBitmapForDisplay(Bitmap bitmap, String display) throws IllegalArgumentException {
        // Dimensions of the target screen
        int targetWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int targetHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        // Dimensions of the Bitmap
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // Scale factors for the Bitmap
        float scaleX = (float) targetWidth / bitmapWidth;
        float scaleY = (float) targetHeight / bitmapHeight;        

        // Adapt the Bitmap based on the display mode
        Bitmap bitmapAdapted;
        switch (display) {
            case DISPLAY_FILL: 
                float scaleFill = Math.max(scaleX, scaleY);
                int fillWidth = Math.round(bitmapWidth * scaleFill);
                int fillHeight = Math.round(bitmapHeight * scaleFill);
                Bitmap scaledFill = Bitmap.createScaledBitmap(bitmap, fillWidth, fillHeight, true);
                int cropX = (fillWidth - targetWidth) / 2;
                int cropY = (fillHeight - targetHeight) / 2;
                bitmapAdapted = Bitmap.createBitmap(scaledFill, cropX, cropY, targetWidth, targetHeight);
                break;

            case DISPLAY_FIT: 
                float scaleFit = Math.min(scaleX, scaleY);
                int fitWidth = Math.round(bitmapWidth * scaleFit);
                int fitHeight = Math.round(bitmapHeight * scaleFit);
                Bitmap scaledFit = Bitmap.createScaledBitmap(bitmap, fitWidth, fitHeight, true);
                bitmapAdapted = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                Canvas canvasFit = new Canvas(bitmapAdapted);
                int fitOffsetX = (targetWidth - fitWidth) / 2;
                int fitOffsetY = (targetHeight - fitHeight) / 2;
                canvasFit.drawBitmap(scaledFit, fitOffsetX, fitOffsetY, null);
                break;

            case DISPLAY_STRETCH: 
                bitmapAdapted = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                break;

            case DISPLAY_CENTER: 
                bitmapAdapted = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                Canvas canvasCenter = new Canvas(bitmapAdapted);
                int offsetX = (targetWidth - bitmapWidth) / 2;
                int offsetY = (targetHeight - bitmapHeight) / 2;
                canvasCenter.drawBitmap(bitmap, offsetX, offsetY, null);
                break;

            default:
                throw new IllegalArgumentException("Invalid display mode");
        }

        // Release resources
        if (bitmap != null) bitmap = null;

        return bitmapAdapted;
    }
}