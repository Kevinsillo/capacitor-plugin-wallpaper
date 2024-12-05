import { registerPlugin } from '@capacitor/core';

import type { WallpapersPlugin } from './definitions';

const Wallpapers = registerPlugin<WallpapersPlugin>('Wallpapers', {
  web: () => import('./web').then((m) => new m.WallpapersWeb()),
});

export * from './definitions';
export { Wallpapers };
