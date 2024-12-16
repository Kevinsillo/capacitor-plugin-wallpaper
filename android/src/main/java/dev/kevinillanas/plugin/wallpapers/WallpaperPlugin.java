package dev.kevinillanas.plugin.wallpaper;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.PermissionState;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@CapacitorPlugin(
    name = "Wallpaper",
    permissions = {
        @Permission(
            strings = { 
                Manifest.permission.SET_WALLPAPER, 
                Manifest.permission.SET_WALLPAPER_HINTS
            },
            alias = "WallpaperPermissions"
        )
    }
)
public class WallpaperPlugin extends Plugin {

    // Wallpaper instance for handling wallpaper operations
    private Wallpaper implementation;

    // Constants for permissions alias
    static String WALLPAPER_PERMISSIONS = "WallpaperPermissions";

    @Override
    public void load() {
        implementation = new Wallpaper(getContext());
    }

    @PluginMethod
    public void setFromBase64(PluginCall call) {
        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", implementation.TARGET_BOTH);
        String display = call.getString("display", implementation.DISPLAY_FILL);

        // Validate URL input
        if (input == null || input.isEmpty()) {
            call.reject("Base64 string is required!");
        }

        // Decode the base64 string into a Bitmap
        byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
        Bitmap bitmapDecoded = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        try {
            // Apply the processed Bitmap as wallpaper
            implementation.applyWallpaper(bitmapDecoded, target, display);

            // Return success message
            JSObject result = new JSObject();
            result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
            call.success(result);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void setFromURL(PluginCall call) throws IOException {
        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", implementation.TARGET_BOTH);
        String display = call.getString("display", implementation.DISPLAY_FILL);

        // Validate URL input
        if (input == null || input.isEmpty()) {
            call.reject("URL is required!");
        }

        // Load the Bitmap from the URL
        InputStream inputStream = new URL(input).openStream();
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

        // Close the input stream
        if (inputStream != null) inputStream.close();

        try {
            // Apply the processed Bitmap as wallpaper
            implementation.applyWallpaper(originalBitmap, target, display);

            // Return success message
            JSObject result = new JSObject();
            result.put("message", "Wallpaper (" + display + ") updated successfully for " + target + " screen!");
            call.success(result);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (isStoragePermissionGranted()) {
            JSObject permissionsResultJSON = new JSObject();
            permissionsResultJSON.put("message", "Permissions already granted");
            call.resolve(permissionsResultJSON);
        } else {
            super.requestPermissions(call);
        }
    }

    /**
     * Checks the the given permission is granted or not
     * @return Returns true if the app is running on Android 30 or newer or if the permission is already granted
     * or false if it is denied.
     */
    private boolean isStoragePermissionGranted() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || getPermissionState(WALLPAPER_PERMISSIONS) == PermissionState.GRANTED;
    }
}