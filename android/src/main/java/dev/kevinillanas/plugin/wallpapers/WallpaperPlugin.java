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

        // Set the wallpaper based on the target screen
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
    }

    /**
     * Resizes an image according to the desired display type to fit the screen as a wallpaper.
     * @param bitmap The original image to be resized.
     * @param display The display type: "DISPLAY_FILL", "DISPLAY_FIT", "DISPLAY_STRETCH", or "DISPLAY_CENTER".
     * @return The resized image adjusted to the specified display type.
     */
    private Bitmap adaptBitmapForDisplay(Bitmap bitmap, String display) {
        // Get screen dimensions (width and height) once
        int targetWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        int targetHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        // Get the dimensions of the original image
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        // Calculate scaling factors based on screen dimensions
        float scaleX = (float) targetWidth / bitmapWidth;
        float scaleY = (float) targetHeight / bitmapHeight;

        // Switch between different display types
        Bitmap resultBitmap = null;
        switch (display) {
            case DISPLAY_FILL:
                // Scale the image to fill the screen (may crop the image)
                float scaleFill = Math.max(scaleX, scaleY);
                int fillWidth = Math.round(bitmapWidth * scaleFill);
                int fillHeight = Math.round(bitmapHeight * scaleFill);
                Bitmap scaledFill = Bitmap.createScaledBitmap(bitmap, fillWidth, fillHeight, true);
                // Crop the image to the exact screen size
                float cropX = (fillWidth - targetWidth) / 2;
                float cropY = (fillHeight - targetHeight) / 2;
                resultBitmap = Bitmap.createBitmap(scaledFill, cropY, cropY, targetWidth, targetHeight);
                break;

            case DISPLAY_FIT:
                // Scale the image to fit within the screen without cropping
                float scaleFit = Math.min(scaleX, scaleY);
                int fitWidth = Math.round(bitmapWidth * scaleFit);
                int fitHeight = Math.round(bitmapHeight * scaleFit);
                Bitmap scaledFit = Bitmap.createScaledBitmap(bitmap, fitWidth, fitHeight, true);
                // Create a new bitmap of the exact screen size
                resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                Canvas canvasFit = new Canvas(resultBitmap);
                // Calculate offsets to center the image
                int fitOffsetX = (targetWidth - fitWidth) / 2;
                int fitOffsetY = (targetHeight - fitHeight) / 2;
                // Draw the centered scaled image
                canvasFit.drawBitmap(scaledFit, fitOffsetX, fitOffsetY, null);
                break;

            case DISPLAY_STRETCH:
                // Stretch the image to fill the entire screen
                resultBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                break;

            case DISPLAY_CENTER:
                // Center the image without resizing
                resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                Canvas canvasCenter = new Canvas(resultBitmap);
                int offsetX = (targetWidth - bitmapWidth) / 2;
                int offsetY = (targetHeight - bitmapHeight) / 2;
                // Draw the image centered on the canvas
                canvasCenter.drawBitmap(bitmap, offsetX, offsetY, null);
                break;

            default:
                // Default case: scale the image to fill the screen
                resultBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
                break;
        }

        return resultBitmap;
    }
}