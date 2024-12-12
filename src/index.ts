import { registerPlugin } from '@capacitor/core'
import type { WallpapersPlugin } from './definitions'

// Register the 'Wallpaper' plugin with Capacitor, specifying the web implementation
const Wallpapers = registerPlugin<WallpapersPlugin>('Wallpaper', {
  web: () => import('./web').then((m) => new m.WallpapersWeb()),
})

// Export all definitions from the './definitions' file
export * from './definitions'

// Export the Wallpapers plugin as the default export
export default Wallpapers