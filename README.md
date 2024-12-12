# capacitor-plugin-wallpapers

Allows you to use the functionality to edit the device's wallpaper, both the lock screen and the home screen.

## Install

```bash
npm install capacitor-plugin-wallpapers
npx cap sync
```

## API

<docgen-index>

* [`setWallpaper(...)`](#setwallpaper)
* [Interfaces](#interfaces)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

Plugin interface for setting wallpapers.

### setWallpaper(...)

```typescript
setWallpaper(options: WallpaperOptions) => any
```

Sets the wallpaper.

| Param         | Type                                                          | Description                                    |
| ------------- | ------------------------------------------------------------- | ---------------------------------------------- |
| **`options`** | <code><a href="#wallpaperoptions">WallpaperOptions</a></code> | The options object to configure the wallpaper. |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### WallpaperOptions

Defines the options to set the wallpaper.

| Prop          | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`base64`**  | <code>string</code>                                           |
| **`target`**  | <code><a href="#wallpapertarget">WallpaperTarget</a></code>   |
| **`display`** | <code><a href="#wallpaperdisplay">WallpaperDisplay</a></code> |


### Enums


#### WallpaperTarget

| Members    | Value               |
| ---------- | ------------------- |
| **`HOME`** | <code>"home"</code> |
| **`LOCK`** | <code>"lock"</code> |
| **`BOTH`** | <code>"both"</code> |


#### WallpaperDisplay

| Members       | Value                  |
| ------------- | ---------------------- |
| **`FIT`**     | <code>"fit"</code>     |
| **`FILL`**    | <code>"fill"</code>    |
| **`STRETCH`** | <code>"stretch"</code> |
| **`CENTER`**  | <code>"center"</code>  |

</docgen-api>
