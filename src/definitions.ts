/**
 * Defines the different wallpaper targets.
 */
export enum WallpaperTarget {
  HOME = "home",  // Home screen wallpaper
  LOCK = "lock",  // Lock screen wallpaper
  BOTH = "both",  // Both home and lock screen wallpaper
}

/**
 * Defines the methods of displaying the wallpaper.
 */
export enum WallpaperDisplay {
  FIT = "fit",  // Fits the wallpaper to the screen
  FILL = "fill",  // Fills the screen with the wallpaper
  STRETCH = "stretch",  // Stretches the wallpaper to cover the screen
  CENTER = "center",  // Centers the wallpaper on the screen
}

/**
 * Defines the options to set the wallpaper.
 */
export interface WallpaperOptions {
  input: string  // The image for the wallpaper
  target: WallpaperTarget  // The target screen(s) for the wallpaper
  display: WallpaperDisplay  // The display method for the wallpaper
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
   * Sets the wallpaper from a base64 string.
   * @param options The options object to configure the wallpaper.
   * @returns A promise with the response message.
   */
  setFromBase64(options: WallpaperOptions): Promise<WallpaperResponse>

  /**
   * Requests the necessary permissions to set the wallpaper.
   * @returns A promise with the response message.
   */
  requestPermissions(): Promise<WallpaperResponse>
}