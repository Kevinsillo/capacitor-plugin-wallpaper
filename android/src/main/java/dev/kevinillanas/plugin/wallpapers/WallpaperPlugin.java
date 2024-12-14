package dev.kevinillanas.plugin.wallpaper;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@CapacitorPlugin(name = "Wallpaper")
public class WallpaperPlugin extends Plugin {
    // Result object to return to the caller
    private final JSObject result = new JSObject();

    // Constants for wallpaper targets
    private static final String TARGET_HOME = "home"; // Home screen wallpaper
    private static final String TARGET_LOCK = "lock"; // Lock screen wallpaper
    private static final String TARGET_BOTH = "both"; // Both screens wallpaper

    // Constants for display modes
    private static final String DISPLAY_FILL = "fill"; // Scale and crop to fill the screen
    private static final String DISPLAY_FIT = "fit"; // Fit without distortion
    private static final String DISPLAY_STRETCH = "stretch"; // Stretch to fit, may distort
    private static final String DISPLAY_CENTER = "center"; // Center without scaling

    @PluginMethod
    public void setFromBase64(PluginCall call) {
        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", TARGET_BOTH);
        String display = call.getString("display", DISPLAY_FILL);

        // Validate URL input
        if (input == null || input.isEmpty()) {
            this.result.put("status", "error");
            this.result.put("message", "URL is required!");
            call.success(this.result);
        }

        // Decode the base64 string into a Bitmap
        byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
        Bitmap bitmapDecoded = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        try {
            // Apply the processed Bitmap as wallpaper
            this.applyWallpaper(bitmapDecoded, target, display);

            // Return success message
            this.result.put("status", "success");
            this.result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
        } catch (Exception e) {
            this.result.put("status", "error");
            this.result.put("message", e.getMessage());
        }

        // Return the result to the caller
        call.success(this.result);
    }

    @PluginMethod
    public void setFromURL(PluginCall call) throws IOException {
        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", TARGET_BOTH);
        String display = call.getString("display", DISPLAY_FILL);

        // Validate URL input
        if (input == null || input.isEmpty()) {
            this.result.put("status", "error");
            this.result.put("message", "URL is required!");
            call.success(this.result);
        }

        // Load the Bitmap from the URL
        InputStream inputStream = inputStream = new URL(input).openStream();
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

        try {
            // Apply the processed Bitmap as wallpaper
            this.applyWallpaper(originalBitmap, target, display);

            // Return success message
            this.result.put("status", "success");
            this.result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
        } catch (Exception e) {
            this.result.put("status", "error");
            this.result.put("message", e.getMessage());
        }

        // Close the input stream
        if (inputStream != null) {
            inputStream.close();
        }

        // Return the result to the caller
        call.success(this.result);
    }

    /**
     * Applies the Bitmap as wallpaper based on the specified target.
     * @param bitmap The Bitmap to apply as wallpaper.
     * @param target The target screen to apply the wallpaper to.
     * @param display The display mode to apply.
     */
    private void applyWallpaper(Bitmap bitmap, String target, String display) throws IOException {
        // Adapt the Bitmap based on the display mode
        Bitmap processedBitmap = adaptBitmapForDisplay(bitmap, display);

        // Set the processed Bitmap as wallpaper
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());

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
                wallpaperManager.setBitmap(processedBitmap, null, true);
                break;
        }

        // if (wallpaperManager != null) {
        //     wallpaperManager.forgetLoadedWallpaper();
        // }
    }

    /**
     * Adapts the Bitmap to fit the display based on the specified mode.
     * @param bitmap The Bitmap to adapt.
     * @param display The display mode to apply.
     * @return The adapted Bitmap based on the display mode.
     */
    private Bitmap adaptBitmapForDisplay(Bitmap bitmap, String display) {
        int targetWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int targetHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        float scaleX = (float) targetWidth / bitmapWidth;
        float scaleY = (float) targetHeight / bitmapHeight;

        float scale = 0;
        float translateX = 0;
        float translateY = 0;
        Matrix matrix = new Matrix();
        switch (display) {
            case DISPLAY_FILL:
                scale = Math.max(scaleX, scaleY);
                matrix.setScale(scale, scale);
                translateX = (targetWidth - bitmapWidth * scale) / 2;
                translateY = (targetHeight - bitmapHeight * scale) / 2;
                matrix.postTranslate(translateX, translateY);
                break;
            case DISPLAY_FIT:
                scale = Math.min(scaleX, scaleY);
                matrix.setScale(scale, scale);
                translateX = (targetWidth - bitmapWidth * scale) / 2;
                translateY = (targetHeight - bitmapHeight * scale) / 2;
                matrix.postTranslate(translateX, translateY);
                break;
            case DISPLAY_STRETCH:
                matrix.setScale(scaleX, scaleY);
                break;
            case DISPLAY_CENTER:
                translateX = (targetWidth - bitmapWidth) / 2f;
                translateY = (targetHeight - bitmapHeight) / 2f;
                matrix.postTranslate(translateX, translateY);
                break;
            default:
                matrix.setScale(scaleX, scaleY);
                break;
        }

        Bitmap resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(bitmap, matrix, null);
        return resultBitmap;
    }
}