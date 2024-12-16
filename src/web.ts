import { WebPlugin } from '@capacitor/core'
import { WallpaperPlugin, WallpaperResponse } from './definitions'

// Define the WallpaperWeb class that extends the WebPlugin and implements WallpaperPlugin
export class WallpaperWeb extends WebPlugin implements WallpaperPlugin {
  // Asynchronously set the wallpaper URL (not implemented for web)
  async setFromURL(): Promise<WallpaperResponse> {
    // Log a warning indicating that the plugin is not available on the web
    return Promise.reject('Wallpapers plugin is not implemented on the web.')
  }

  // Asynchronously set the wallpaper base64 (not implemented for web)
  async setFromBase64(): Promise<WallpaperResponse> {
    // Log a warning indicating that the plugin is not available on the web
    return Promise.reject('Wallpapers plugin is not implemented on the web.')
  }

  // Asynchronously request permissions (not implemented for web)
  async requestPermissions(): Promise<WallpaperResponse> {
    // Log a warning indicating that the plugin is not available on the web
    return Promise.reject('Wallpapers plugin is not implemented on the web.')
  }
}