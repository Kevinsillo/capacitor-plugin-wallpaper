export interface WallpapersPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
