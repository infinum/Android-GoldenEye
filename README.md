# GoldenEye [![JCenter](https://api.bintray.com/packages/infinum/android/goldeneye/images/download.svg)](https://bintray.com/infinum/android/goldeneye/_latestVersion)

<img src='./logo.svg' width='264'/>

## Quick guide

#### Add dependency

```gradle
implementation 'co.infinum:goldeneye:1.0.0-rc1'
```

#### Initialize

```kotlin
val goldenEye = GoldenEye.Builder(activity).build()
```

#### Open camera

```kotlin
if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    == PackageManager.PERMISSION_GRANTED
    ) {
  /* Find back camera */
  val backCamera = goldenEye.availableCameras.find { it.facing == Facing.BACK }
  /* Open back camera */
  goldenEye.open(textureView, backCamera, initCallback)
}
```

#### Take picture

```kotlin
goldenEye.takePicture(pictureCallback)
```

#### Record video

```kotlin
goldenEye.startRecording(file, videoCallback)
/* Somewhere later */
goldenEye.stopRecording()
```

You can see all GoldenEye methods [here](./goldeneye/src/main/java/co/infinum/goldeneye/GoldenEye.kt).

## Features

GoldenEye supports multiple Camera features that can be changed at runtime:

- [Flash mode](./goldeneye/src/main/java/co/infinum/goldeneye/models/FlashMode.kt)
- [Focus mode](./goldeneye/src/main/java/co/infinum/goldeneye/models/FocusMode.kt)
- [Preview scale](./goldeneye/src/main/java/co/infinum/goldeneye/models/PreviewScale.kt)
- Preview size
- Picture size
- Picture quality
- [Video quality](./goldeneye/src/main/java/co/infinum/goldeneye/models/VideoQuality.kt)
- Tap to focus
- Pinch to zoom
- [Antibanding mode](./goldeneye/src/main/java/co/infinum/goldeneye/models/AntibandingMode.kt)
- [White balance mode](./goldeneye/src/main/java/co/infinum/goldeneye/models/WhiteBalanceMode.kt)
- [Color effect mode](./goldeneye/src/main/java/co/infinum/goldeneye/models/ColorEffectMode.kt)

If you are interested to read in detail what is supported, there is thorough documentation inside [interfaces](./goldeneye/src/main/java/co/infinum/goldeneye/config).

## Builder

When initializing GoldenEye instance you can configure it to fit your needs with several interfaces.

#### [Logger](./goldeneye/src/main/java/co/infinum/goldeneye/Logger.kt)

By default logging is turned **OFF**. By implementing Logger interface, you can enable logs if needed.

```kotlin
object: Logger {
  override fun log(message: String) {
    /* Log standard message */
  }

  override fun log(t: Throwable) {
    /* Log error */
  }
}
```

#### [OnZoomChangedCallback](./goldeneye/src/main/java/co/infinum/goldeneye/Callbacks.kt)

GoldenEye supports pinch to zoom functionality. By using OnZoomChangedCallback you can receive callback every time the zoom changes.

```kotlin
object: OnZoomChangedCallback {
  override fun onZoomChanged(zoom: Int) {
    /* Do something */
  }
}
```

#### [OnFocusChangedCallback](./goldeneye/src/main/java/co/infinum/goldeneye/Callbacks.kt)

GoldenEye supports tap to focus functionality. By using OnFocusChangedCallback you can receive callback every time the focus changes.

```kotlin
object: OnFocusChangedCallback {
  override fun onFocusChanged(point: Point) {
    /* Do something */
  }
}
```

#### [PictureTransformation](./goldeneye/src/main/java/co/infinum/goldeneye/PictureTransformation.kt)

Once the picture is taken, by default, library will rotate the bitmap to be in sync with device's orientation and mirror
the image if it is taken with front camera. If you are not OK with this behavior, you can provide `PictureTransformation` implementation
that will be used instead. `PictureTransformation.transform` method is executed on the **background** thread!

```kotlin
object: PictureTransformation {
  override fun transform(picture: Bitmap, config: CameraConfig, orientationDifference: Float): Bitmap {
    /* Transform raw picture */
  }
}
```

#### Advanced features

Advanced features are:

- [Antibanding mode](./goldeneye/src/main/java/co/infinum/goldeneye/models/AntibandingMode.kt)
- [White balance mode](./goldeneye/src/main/java/co/infinum/goldeneye/models/WhiteBalanceMode.kt)
- [Color effect mode](./goldeneye/src/main/java/co/infinum/goldeneye/models/ColorEffectMode.kt)

Advanced features are still in experimental phase and we noticed that they do not work on some devices and they were not
thoroughly tested so we decided to disable them by default. That means that if you try to change the value via setter, it will simply be ignored.

In case you want to try and play with advanced features, you can enable them when initializing GoldenEye instance.

```kotlin
GoldenEye.Builder(activity)
  .withAdvancedFeatures()
  .build()
```

## Edge case behavior

- If you call `startRecording` or `takePicture` while GoldenEye is already taking a picture or recording a video, immediate `onError` callback will be dispatched
- If you call `release` while GoldenEye is taking a picture or recording a video, everything will be canceled and nothing is dispatched
- If you call `GoldenEye.config` before `InitCallback#onReady` is received, returned `config` will be `null`
- If you call `open` while camera is already opened, old camera will be released and closed and new camera will be opened

## Known issues

- Video recording with external camera is not supported due to current video configuration limitations of the internal API design
- OnePlus 6 - ColorEffectMode does not work
- Huawei Nexus 6P - Picture taken with Flash is too dark
- LG G5 - Picture taken with Flash is too dark

## Contributing

Feedback and code contributions are very much welcome. Just make a pull request with a short description of your changes.
By making contributions to this project you give permission for your code to be used under the same [license](LICENSE).

## Credits

Maintained and sponsored by [Infinum](http://www.infinum.co).

<a href='https://infinum.co'>
  <img src='https://infinum.co/infinum.png' href='https://infinum.co' width='264'>
</a>
