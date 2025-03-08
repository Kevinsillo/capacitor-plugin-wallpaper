package dev.kevinillanas.plugin.wallpaper;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.PermissionState;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Base64;
import android.os.Build;
import android.os.StrictMode;

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
    private Context context;
    static String WALLPAPER_PERMISSIONS = "WallpaperPermissions";

    // Constants for wallpaper targets
    public static final String TARGET_HOME = "home"; // Home screen wallpaper
    public static final String TARGET_LOCK = "lock"; // Lock screen wallpaper
    public static final String TARGET_BOTH = "both"; // Both screens wallpaper

    // Constants for display modes
    public static final String DISPLAY_FILL = "fill"; // Scale and crop to fill the screen
    public static final String DISPLAY_FIT = "fit"; // Fit without distortion
    public static final String DISPLAY_STRETCH = "stretch"; // Stretch to fit, may distort
    public static final String DISPLAY_CENTER = "center"; // Center without scaling

    @Override
    public void load() {
        context = this.getContext().getApplicationContext();
    }

    @PluginMethod
    public void setFromURL(PluginCall call) throws IOException {

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().penaltyLog().build());
        


        // Extract parameters from the plugin call
        String input = call.getString("input");
        String target = call.getString("target", TARGET_BOTH);
        String display = call.getString("display", DISPLAY_FILL);

        // Validate URL input
        if (input == null || input.isEmpty()) {
            call.reject("URL is required!");
        }

        try {
            // Load the Bitmap from the URL
            Bitmap bitmap;
            try (InputStream inputStream = new URL(input).openStream()) {
                bitmap = BitmapFactory.decodeStream(inputStream);
                if (inputStream != null) inputStream.close();
            }
            
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
                    if (scaledFill != null) scaledFill.recycle();
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
                    if (scaledFit != null) scaledFit.recycle();
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

            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            wallpaperManager.clearWallpaper();

            switch (target) {
                case TARGET_HOME:
                    wallpaperManager.setBitmap(bitmapAdapted, null, true, WallpaperManager.FLAG_SYSTEM);
                    break;
                case TARGET_LOCK:
                    wallpaperManager.setBitmap(bitmapAdapted, null, true, WallpaperManager.FLAG_LOCK);
                    break;
                case TARGET_BOTH:
                    wallpaperManager.setBitmap(bitmapAdapted, null, true, 0);
                    break;
                default:
                    throw new IOException("Invalid target");
            }

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
        if (isWallpaperPermissionsGranted()) {
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
    private boolean isWallpaperPermissionsGranted() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || getPermissionState(WALLPAPER_PERMISSIONS) == PermissionState.GRANTED;
    }
}