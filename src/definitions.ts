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
 * Plugin interface for setting wallpapers.
 */
export interface WallpapersPlugin {
  /**
   * Sets the wallpaper from a base64 string.
   * @param options The options object to configure the wallpaper.
   * @returns A promise that resolves when the wallpaper is set successfully.
   */
  setWallpaperBase64(options: WallpaperOptions): Promise<void>

  /**
   * Sets the wallpaper from a URL.
   * @param options The options object to configure the wallpaper.
   * @returns A promise that resolves when the wallpaper is set successfully.
   */
  setWallpaperURL(options: WallpaperOptions): Promise<void>
}