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

        // Recycle the original Bitmap
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }

        try {
            // Apply the processed Bitmap as wallpaper
            this.applyWallpaper(processedBitmap, target);

            // Return success message
            this.result.put("status", "success");
            this.result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
        } catch (Exception e) {
            // Handle errors and return failure response
            this.result.put("status", "error");
            this.result.put("message", e.getMessage());
        }

        // Recycle the processed Bitmap
        if (processedBitmap != null && !processedBitmap.isRecycled()) {
            processedBitmap.recycle();
        }

        Log.d("WallpaperPlugin", "Wallpaper applied successfully.");
        call.success(this.result);
    }

    @PluginMethod
    public void setWallpaperURL(PluginCall call) throws IOException {
        // Log.d("WallpaperPlugin", "WebView status: " + (bridge.getWebView() != null));

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
            this.result.put("status", "error");
            this.result.put("message", "URL is required!");
            call.success(this.result);
        }

        InputStream inputStream = null;
        Bitmap originalBitmap = null;
        try {
            inputStream = new URL(input).openStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            originalBitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            this.result.put("status", "error");
            this.result.put("message", "Failed to load the image from the URL!");
            call.success(this.result);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            return;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Adapt the Bitmap based on the display mode
        Bitmap processedBitmap = adaptBitmapForDisplay(originalBitmap, display);

        // Recycle the original Bitmap
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }

        try {
            // Apply the processed Bitmap as wallpaper
            this.applyWallpaper(processedBitmap, target);

            // Return success message
            this.result.put("status", "success");
            this.result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
        } catch (Exception e) {
            // Handle errors and return failure response
            this.result.put("status", "error");
            this.result.put("message", e.getMessage());
        }

        // Recycle the processed Bitmap
        if (processedBitmap != null && !processedBitmap.isRecycled()) {
            processedBitmap.recycle();
        }

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
            throw new IllegalArgumentException(e.getMessage());
        }

        Canvas canvas = new Canvas(resultBitmap);
        Matrix matrix = new Matrix();

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float scaleX = (float) targetWidth / bitmapWidth;
        float scaleY = (float) targetHeight / bitmapHeight;

        float scale = 0;
        float translateX = 0;
        float translateY = 0;
        switch (display) {
            case DISPLAY_FILL:
                scale = Math.max(scaleX, scaleY);
                matrix.setScale(scale, scale);
                translateX = (float) Math.round((targetWidth - bitmapWidth * scale) / 2f);
                translateY = (float) Math.round((targetHeight - bitmapHeight * scale) / 2f);
                matrix.postTranslate(translateX, translateY);
                break;
            case DISPLAY_FIT:
                scale = Math.min(scaleX, scaleY);
                matrix.setScale(scale, scale);
                translateX = (float) Math.round((targetWidth - bitmapWidth * scale) / 2f);
                translateY = (float) Math.round((targetHeight - bitmapHeight * scale) / 2f);
                matrix.postTranslate(translateX, translateY);
                break;
            case DISPLAY_STRETCH:
                matrix.setScale(scaleX, scaleY);
                break;
            case DISPLAY_CENTER:
                translateX = (float) Math.round((targetWidth - bitmapWidth) / 2f);
                translateY = (float) Math.round((targetHeight - bitmapHeight) / 2f);
                matrix.postTranslate(translateX, translateY);
                break;
            default:
                matrix.setScale(scaleX, scaleY);
                break;
        }

        try {
            // matrix.setScale(scaleX, scaleY);
            canvas.drawBitmap(bitmap, matrix, null);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
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