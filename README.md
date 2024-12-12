<p align="center">
    <br />
    <img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" />
</p>
<h3 align="center">Wallpaper</h3>
<p align="center">
    <strong><code>@Kevinsillo/capacitor-plugin-wallpaper</code></strong>
</p>
<p align="center">
    A Capacitor community plugin that enables functionality to set the device's wallpaper, supporting both the lock screen and the home screen, with options to apply the image in different modes: fit, fill, stretch, and center.
</p>

<p align="center">
    <img src="https://img.shields.io/maintenance/yes/2024?style=flat-square" />    
    <a href="https://github.com/capacitor-community/example/actions?query=workflow%3A%22CI%22">
        <img src="https://img.shields.io/github/workflow/status/capacitor-community/example/CI?style=flat-square" />
    </a>
    <a href="https://www.npmjs.com/package/@capacitor-community/example">
        <img src="https://img.shields.io/npm/l/@capacitor-community/example?style=flat-square" />
    </a>
    <br/>
    <a href="https://www.npmjs.com/package/@capacitor-community/example">
        <img src="https://img.shields.io/npm/dw/@capacitor-community/example?style=flat-square" />
    </a>
    <a href="https://www.npmjs.com/package/@capacitor-community/example">
        <img src="https://img.shields.io/npm/v/@capacitor-community/example?style=flat-square" />
    </a>
    <!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
    <a href="#contributors-">
        <img src="https://img.shields.io/badge/all%20contributors-0-orange?style=flat-square" />
    </a>
    <!-- ALL-CONTRIBUTORS-BADGE:END -->
</p>

## Maintainers

| Maintainer | GitHub | Social |
| -----------| -------| -------|
| Kevinsillo | [Kevinsillo](https://github.com/Kevinsillo) | [@Quebinaso](https://twitter.com/Quebinaso) |

## Install

First you need clone this project, install and pack it.

```bash
git clone https://github.com/Kevinsillo/capacitor-plugin-wallpaper.git
cd capacitor-plugin-wallpaper
npm install
npm run build
npm pack
```

Then you can install the plugin in your project.

```bash
npm install /path/to/capacitor-plugin-wallpaper-x.x.x.tgz
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
