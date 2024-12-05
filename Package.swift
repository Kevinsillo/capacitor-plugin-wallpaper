// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorPluginWallpapers",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "CapacitorPluginWallpapers",
            targets: ["WallpapersPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "WallpapersPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/WallpapersPlugin"),
        .testTarget(
            name: "WallpapersPluginTests",
            dependencies: ["WallpapersPlugin"],
            path: "ios/Tests/WallpapersPluginTests")
    ]
)