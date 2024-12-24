package dev.kevinillanas.plugin.wallpaper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.content.Context;
import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.io.IOException;

public class Wallpaper {
    // Constants for wallpaper targets
    public static final String TARGET_HOME = "home"; // Home screen wallpaper
    public static final String TARGET_LOCK = "lock"; // Lock screen wallpaper
    public static final String TARGET_BOTH = "both"; // Both screens wallpaper

    // Constants for display modes
    public static final String DISPLAY_FILL = "fill"; // Scale and crop to fill the screen
    public static final String DISPLAY_FIT = "fit"; // Fit without distortion
    public static final String DISPLAY_STRETCH = "stretch"; // Stretch to fit, may distort
    public static final String DISPLAY_CENTER = "center"; // Center without scaling
    
    /**
     * Applies the Bitmap as wallpaper based on the specified target.
     * @param context The context to apply the wallpaper to.
     * @param bitmap The Bitmap to apply as wallpaper.
     * @param target The target screen to apply the wallpaper to.
     * @param display The display mode to apply.
     */
    public void applyWallpaper(Context context, Bitmap bitmap, String target, String display) throws IOException {
        // Si el Activity está en un estado válido, ejecutamos el Handler
        try {
            // Adaptar el Bitmap basado en el modo de visualización
            Bitmap processedBitmap = adaptBitmapForDisplay(context, bitmap, display);

            // Configurar el wallpaper
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);

            // Aplicar el wallpaper según el destino
            switch (target) {
                case TARGET_HOME:
                    wallpaperManager.setBitmap(processedBitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                    break;
                case TARGET_LOCK:
                    wallpaperManager.setBitmap(processedBitmap, null, true, WallpaperManager.FLAG_LOCK);
                    break;
                case TARGET_BOTH:
                    wallpaperManager.setBitmap(processedBitmap, null, true, 0);
                    break;
                default:
                    throw new IOException("Invalid target");
            }
        } catch (IOException e) {
            Log.e("Wallpaper", "Error applying wallpaper", e);
        }
    }

    /**
     * Adapts the Bitmap to the display mode specified.
     * @param context The context to apply the wallpaper to.
     * @param bitmap The Bitmap to adapt.
     * @param display The display mode to apply.
     * @return The adapted Bitmap based on the display mode.
     */
    public Bitmap adaptBitmapForDisplay(Context context, Bitmap bitmap, String display) throws IllegalArgumentException {
        // Dimensions of the target screen
        int targetWidth = context.getResources().getSystem().getDisplayMetrics().widthPixels;
        int targetHeight = context.getResources().getSystem().getDisplayMetrics().heightPixels;

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
                int fillWidth = (int) Math.ceil(bitmapWidth * scaleFill);
                int fillHeight = (int) Math.ceil(bitmapHeight * scaleFill);
                Bitmap scaledFill = Bitmap.createScaledBitmap(bitmap, fillWidth, fillHeight, true);
                int cropX = (fillWidth - targetWidth) / 2;
                int cropY = (fillHeight - targetHeight) / 2;
                bitmapAdapted = Bitmap.createBitmap(scaledFill, cropX, cropY, targetWidth, targetHeight);
                break;

            case DISPLAY_FIT: 
                float scaleFit = Math.min(scaleX, scaleY);
                int fitWidth = (int) Math.ceil(bitmapWidth * scaleFit);
                int fitHeight = (int) Math.ceil(bitmapHeight * scaleFit);
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
                int offsetX = (int) (targetWidth - bitmapWidth) / 2;
                int offsetY = (int) (targetHeight - bitmapHeight) / 2;
                canvasCenter.drawBitmap(bitmap, offsetX, offsetY, null);
                break;

            default:
                throw new IllegalArgumentException("Invalid display mode");
        }

        return bitmapAdapted;
    }
}