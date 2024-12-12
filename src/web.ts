import { WebPlugin } from '@capacitor/core'
import { WallpapersPlugin } from './definitions'

// Define the WallpapersWeb class that extends the WebPlugin and implements WallpapersPlugin
export class WallpapersWeb extends WebPlugin implements WallpapersPlugin {

  // Asynchronously set the wallpaper base64 (not implemented for web)
  async setWallpaperBase64(): Promise<void> {
    // Log a warning indicating that the plugin is not available on the web
    console.warn('Wallpapers plugin is not implemented on the web.')
  }

  // Asynchronously set the wallpaper URL (not implemented for web)
  async setWallpaperURL(): Promise<void> {
    // Log a warning indicating that the plugin is not available on the web
    console.warn('Wallpapers plugin is not implemented on the web.')
  }
}