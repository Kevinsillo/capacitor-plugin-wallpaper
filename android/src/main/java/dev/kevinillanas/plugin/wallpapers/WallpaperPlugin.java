package dev.kevinillanas.plugin.wallpaper;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.PermissionState;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.WindowManager;
import android.app.Activity;
import android.content.Context;
import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.os.Build;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
    private Wallpaper implementation;
    static String WALLPAPER_PERMISSIONS = "WallpaperPermissions";

    @Override
    public void load() {
        implementation = new Wallpaper();
        context = getActivity();
    }

    @PluginMethod
    public void setFromURL(PluginCall call) {
        saveCall(call);

        // Extract parameters from the plugin call
        String urlString = call.getString("urlString");
        String target = call.getString("target", implementation.TARGET_BOTH);

        // Validate URL
        if (urlString == null || urlString.isEmpty()) {
            call.reject("URL is required!");
            return;
        }

        try {
            // Solicitar menos memoria configurando un tamaño máximo
            int maxWidth = 1920;
            int maxHeight = 1080;

            Glide.with(context)
                .asBitmap()
                .load(urlString)
                .override(maxWidth, maxHeight)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            // Aplicar el fondo
                            implementation.applyWallpaper(getContext(), bitmap, target);
                            
                            getActivity().runOnUiThread(() -> {
                                JSObject result = new JSObject();
                                result.put("message", "Wallpaper updated successfully");
                                call.resolve(result);
                            });
                        } catch (Exception e) {
                            call.reject("Error applying wallpaper: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        call.reject("Failed to load image from URL!");
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // No necesitamos hacer nada aquí
                    }
                });
        } catch (Exception e) {
            call.reject("Error loading image: " + e.getMessage());
            return;
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