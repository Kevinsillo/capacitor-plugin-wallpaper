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
    public void setWallpaperBase64(PluginCall call) {
        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", TARGET_BOTH);
        String display = call.getString("display", DISPLAY_FILL);

        // Validate input
        if (input == null || input.isEmpty()) {
            // this.result.put("status", "error");
            // this.result.put("message", "Base64 string is required!");
            // call.resolve(this.result);
            call.reject("Base64 string is required!");
        }

        // Decode the base64 string into a Bitmap
        Bitmap originalBitmap = base64ToBitmap(input);

        // Adapt the Bitmap based on the display mode
        Bitmap processedBitmap = adaptBitmapForDisplay(originalBitmap, display);

        if (processedBitmap == null) {
            // this.result.put("status", "error");
            // this.result.put("message", "Failed to process the image!");
            // call.resolve(this.result);
            call.reject("Failed to process the image!");
        }

        try {
            // Apply the processed Bitmap as wallpaper
            this.applyWallpaper(processedBitmap, target);

            // Return success message
            this.result.put("status", "success");
            this.result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
        } catch (Exception e) {
            // Handle errors and return failure response
            // this.result.put("status", "error");
            // this.result.put("message", e.getMessage());
            call.reject(e.getMessage());
        }
        call.success(this.result);
    }

    @PluginMethod
    public void setWallpaperURL(PluginCall call) {
        Log.d("WallpaperPlugin", "WebView status: " + (bridge.getWebView() != null));

        if (getContext() == null) {
            call.reject("Context is null!");
            return;
        }

        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", TARGET_BOTH);
        String display = call.getString("display", DISPLAY_FILL);

        // Validate URL input
        if (input == null || input.isEmpty()) {
            // this.result.put("status", "error");
            // this.result.put("message", "URL is required!");
            // call.resolve(this.result);
            call.reject("URL is required!");
        }

        Log.d("WallpaperPlugin", "WebView is available and context is not null");

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // Memoria máxima asignable
        long usedMemory = runtime.totalMemory() - runtime.freeMemory(); // Memoria usada
        Log.w("MemoryInfo", "Max memory: " + maxMemory + ", Used memory: " + usedMemory);

        InputStream inputStream = null;
        Bitmap originalBitmap = null;
        try {
            inputStream = new URL(input).openStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Reduce el tamaño del Bitmap a la mitad
            originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            Log.d("WallpaperPlugin", "Image loaded from URL successfully. Bitmap dimensions: " + originalBitmap.getWidth() + "x" + originalBitmap.getHeight());
        } catch (Exception e) {
            // this.result.put("status", "error");
            // this.result.put("message", "Failed to load the image from the URL!");
            // call.resolve(this.result);
            call.reject(e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("WallpaperPlugin", "Error closing InputStream", e);
                    call.reject(e.getMessage());
                }
            }
        }

        if (originalBitmap == null) {
            call.reject("Original Bitmap is null!");
            return;
        }

        runtime = Runtime.getRuntime();
        maxMemory = runtime.maxMemory(); // Memoria máxima asignable
        usedMemory = runtime.totalMemory() - runtime.freeMemory(); // Memoria usada
        Log.w("MemoryInfo", "Max memory: " + maxMemory + ", Used memory: " + usedMemory);

        // Adapt the Bitmap based on the display mode
        Bitmap processedBitmap = adaptBitmapForDisplay(originalBitmap, display);
        if (processedBitmap == null) {
            // this.result.put("status", "error");
            // this.result.put("message", "Failed to process the image!");
            // call.resolve(this.result);
            call.reject("Failed to process the image!");
        }

        runtime = Runtime.getRuntime();
        maxMemory = runtime.maxMemory(); // Memoria máxima asignable
        usedMemory = runtime.totalMemory() - runtime.freeMemory(); // Memoria usada
        Log.w("MemoryInfo", "Max memory: " + maxMemory + ", Used memory: " + usedMemory);

        try {
            // Apply the processed Bitmap as wallpaper
            Log.d("WallpaperPlugin", "Applying wallpaper to target...");
            this.applyWallpaper(processedBitmap, target);
            Log.d("WallpaperPlugin", "Wallpaper applied to target: " + target);

            // Return success message
            this.result.put("status", "success");
            this.result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
        } catch (Exception e) {
            // Handle errors and return failure response
            // this.result.put("status", "error");
            // this.result.put("message", e.getMessage());
            call.reject(e.getMessage());
        }

        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }

        if (processedBitmap != null && !processedBitmap.isRecycled()) {
            processedBitmap.recycle();
        }

        Log.d("WallpaperPlugin", "Wallpaper applied successfully.");
        call.success(this.result);
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

        Bitmap resultBitmap;
        try {
            resultBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        } catch (IllegalArgumentException e) {
            Log.e("WallpaperPlugin", "Error creating result Bitmap", e);
            return null;
        }

        Canvas canvas = new Canvas(resultBitmap);
        Matrix matrix = new Matrix();

        float scaleX = (float) targetWidth / bitmap.getWidth();
        float scaleY = (float) targetHeight / bitmap.getHeight();

        Log.d("WallpaperPlugin", "Bitmap dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight());
        Log.d("WallpaperPlugin", "Target Width: " + targetWidth + ", Target Height: " + targetHeight);
        Log.d("WallpaperPlugin", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);

        // switch (display) {
        //     case DISPLAY_FILL:
        //         float fillScale = Math.max(scaleX, scaleY);
        //         matrix.setScale(fillScale, fillScale);
        //         float fillTranslateX = (targetWidth - bitmap.getWidth() * fillScale) / 2;
        //         float fillTranslateY = (targetHeight - bitmap.getHeight() * fillScale) / 2;
        //         matrix.postTranslate(fillTranslateX, fillTranslateY);
        //         break;
        //     case DISPLAY_FIT:
        //         float fitScale = Math.min(scaleX, scaleY);
        //         matrix.setScale(fitScale, fitScale);
        //         float fitTranslateX = (targetWidth - bitmap.getWidth() * fitScale) / 2;
        //         float fitTranslateY = (targetHeight - bitmap.getHeight() * fitScale) / 2;
        //         matrix.postTranslate(fitTranslateX, fitTranslateY);
        //         break;
        //     case DISPLAY_STRETCH:
        //         matrix.setScale(scaleX, scaleY);
        //         break;
        //     case DISPLAY_CENTER:
        //         float centerTranslateX = (targetWidth - bitmap.getWidth()) / 2f;
        //         float centerTranslateY = (targetHeight - bitmap.getHeight()) / 2f;
        //         matrix.postTranslate(centerTranslateX, centerTranslateY);
        //         break;
        //     default:
        //         matrix.setScale(scaleX, scaleY);
        //         break;
        // }

        try {
            matrix.setScale(scaleX, scaleY);
            canvas.drawBitmap(bitmap, matrix, null);
        } catch (Exception e) {
            Log.e("WallpaperPlugin", "Error drawing bitmap", e);
            return null;
        }
        return resultBitmap;
    }

    /**
     * Applies the Bitmap as wallpaper based on the specified target.
     * @param bitmap The Bitmap to apply as wallpaper.
     * @param target The target screen to apply the wallpaper to.
     */
    private void applyWallpaper(Bitmap bitmap, String target) throws IOException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        Log.d("WallpaperPlugin", "WallpaperManager initialized.");

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
                wallpaperManager.setBitmap(bitmap, null, true);
                break;
        }
    }
}