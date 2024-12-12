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
    public void setWallpaperBase64(PluginCall call) {
        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", TARGET_BOTH);
        String display = call.getString("display", DISPLAY_FILL);

        // Validate input
        if (input == null || input.isEmpty()) {
            this.result.put("status", "error");
            this.result.put("message", "Base64 string is required!");
            call.resolve(this.result);
            return;
        }

        // Decode the base64 string into a Bitmap
        Bitmap originalBitmap = base64ToBitmap(input);

        // Adapt the Bitmap based on the display mode
        Bitmap processedBitmap = adaptBitmapForDisplay(originalBitmap, display);

        if (processedBitmap == null) {
            this.result.put("status", "error");
            this.result.put("message", "Failed to process the image!");
            call.resolve(this.result);
        }

        try {
            // Apply the processed Bitmap as wallpaper
            this.applyWallpaper(processedBitmap, target);

            // Return success message
            this.result.put("status", "success");
            this.result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
            call.resolve(this.result);
        } catch (Exception e) {
            // Handle errors and return failure response
            this.result.put("status", "error");
            this.result.put("message", e.getMessage());
            call.resolve(this.result);
        }
    }

    @PluginMethod
    public void setWallpaperURL(PluginCall call) {
        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", TARGET_BOTH);
        String display = call.getString("display", DISPLAY_FILL);

        // Validate URL input
        if (input == null || input.isEmpty()) {
            this.result.put("status", "error");
            this.result.put("message", "URL is required!");
            call.resolve(this.result);
            return;
        }

        // Load the image from the URL
        InputStream inputStream = null;
        Bitmap originalBitmap = null;
        try {
            inputStream = new URL(input).openStream();
            originalBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            this.result.put("status", "error");
            this.result.put("message", "Failed to load the image from the URL!");
            call.resolve(this.result);
            return;
        }

        // Adapt the Bitmap based on the display mode
        Bitmap processedBitmap = adaptBitmapForDisplay(originalBitmap, display);
        if (processedBitmap == null) {
            this.result.put("status", "error");
            this.result.put("message", "Failed to process the image!");
            call.resolve(this.result);
        }

        try {
            // Apply the processed Bitmap as wallpaper
            this.applyWallpaper(processedBitmap, target);

            // Return success message
            this.result.put("status", "success");
            this.result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
            call.resolve(this.result);
        } catch (Exception e) {
            // Handle errors and return failure response
            this.result.put("status", "error");
            this.result.put("message", e.getMessage());
            call.resolve(this.result);
        }
    }

    /**
     * Converts a base64 string into a Bitmap object.
     * @param base64 The base64 string to convert.
     * @return The Bitmap object converted from the base64 string.
     */
    private Bitmap base64ToBitmap(String base64) {
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
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

        Bitmap resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        Matrix matrix = new Matrix();

        float scaleX = (float) targetWidth / bitmap.getWidth();
        float scaleY = (float) targetHeight / bitmap.getHeight();

        switch (display) {
            case DISPLAY_FILL:
                float fillScale = Math.max(scaleX, scaleY);
                matrix.setScale(fillScale, fillScale);
                float fillTranslateX = (targetWidth - bitmap.getWidth() * fillScale) / 2;
                float fillTranslateY = (targetHeight - bitmap.getHeight() * fillScale) / 2;
                matrix.postTranslate(fillTranslateX, fillTranslateY);
                break;
                
            case DISPLAY_FIT:
                float fitScale = Math.min(scaleX, scaleY);
                matrix.setScale(fitScale, fitScale);
                float fitTranslateX = (targetWidth - bitmap.getWidth() * fitScale) / 2;
                float fitTranslateY = (targetHeight - bitmap.getHeight() * fitScale) / 2;
                matrix.postTranslate(fitTranslateX, fitTranslateY);
                break;
                
            case DISPLAY_STRETCH:
                matrix.setScale(scaleX, scaleY);
                break;
                
            case DISPLAY_CENTER:
                float centerTranslateX = (targetWidth - bitmap.getWidth()) / 2f;
                float centerTranslateY = (targetHeight - bitmap.getHeight()) / 2f;
                matrix.postTranslate(centerTranslateX, centerTranslateY);
                break;
                
            default:
                throw new IllegalArgumentException("Unknown mode: " + display);
        }

        canvas.drawBitmap(bitmap, matrix, null);
        return resultBitmap;
    }

    /**
     * Applies the Bitmap as wallpaper based on the specified target.
     * @param bitmap The Bitmap to apply as wallpaper.
     * @param target The target screen to apply the wallpaper to.
     */
    private void applyWallpaper(Bitmap bitmap, String target) throws IOException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());

        switch (target) {
            case TARGET_HOME:
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                break;
            case TARGET_LOCK:
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                break;
            case TARGET_BOTH:
                wallpaperManager.setBitmap(bitmap, null, true);
                break;
            default:
                throw new IllegalArgumentException("Unknown target: " + target);
        }
    }
}