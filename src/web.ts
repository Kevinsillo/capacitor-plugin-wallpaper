import { WebPlugin } from '@capacitor/core';

import type { WallpapersPlugin } from './definitions';

export class WallpapersWeb extends WebPlugin implements WallpapersPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
