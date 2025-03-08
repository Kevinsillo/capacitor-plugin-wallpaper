/**
 * Defines the different wallpaper targets.
 */
export enum WallpaperTarget {
  HOME = "home",  // Home screen wallpaper
  LOCK = "lock",  // Lock screen wallpaper
  BOTH = "both",  // Both home and lock screen wallpaper
}

/**
 * Defines the options to set the wallpaper.
 */
export interface WallpaperOptions {
  urlString: string  // The image for the wallpaper
  target: WallpaperTarget  // The target screen(s) for the wallpaper
}

/**
 * Defines the response message from the plugin.
 */
export interface WallpaperResponse {
  message: string
}

/**
 * Plugin interface for setting wallpapers.
 */
export interface WallpaperPlugin {
  /**
   * Sets the wallpaper from a URL.
   * @param options The options object to configure the wallpaper.
   * @returns A promise with the response message.
   */
  setFromURL(options: WallpaperOptions): Promise<WallpaperResponse>

  /**
   * Requests the necessary permissions to set the wallpaper.
   * @returns A promise with the response message.
   */
  requestPermissions(): Promise<WallpaperResponse>
}