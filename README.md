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
if (ContextCompat.checkSelfPermission(context, Manifest.permissions.CAMERA) == PackageManager.PERMISSION_GRANTED) {
  /* Find back camera */
  val backCamera = goldenEye.availableCameras().find { it.facing == Facing.BACK }
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

## Camera lifecycle

Camera lifecycle can be separated in several states:

- CLOSED - Initial state, Camera is closed and cannot be used.
- INITIALIZING - Temporary state when `open` is called. Camera initialization takes time so in that time you can find it in initialization state. If error happens, it will be back in CLOSED state.
- READY - Temporary state after INITIALIZING. `onReady` callback will be dispatched and developer can modify camera configuration.
- ACTIVE - Camera is active and preview is started. `onActive` callback will be dispatched and developer can use GoldenEye to take picture or record video.
- TAKING_PICTURE - Self-explanatory
- RECORDING_VIDEO - Self-explanatory

If you try to take picture while camera is not in ACTIVE state, it will be ignored and you will receive onError callback immediately. It is up to you to call take picture
only after `onActive` callback is received from `open` method.

## Lifecycle limitations

- Configuration can be changed only after camera is in READY or ACTIVE state - returns `null`
- Picture can be taken only after camera is in ACTIVE state - immediate onError callback
- Video can be recorded only after camera is in ACTIVE state - immediate onError callback

## Configuration

Use `GoldenEye.Builder` to configure GoldenEye to your needs. It accepts:

- Logger - logging interface that must be implemented if you want to see GoldenEye logs. Logging is **OFF** by default.
- OnZoomChangedCallback - callback to receive zoom change events
- OnFocusChangedCallback - callback to receive tap to focus events
- PictureTransformation - interface that is used when raw camera picture is received and it transforms the picture on the **background thread**. Default implementation rotates the picture to device orientation and mirrors the picture if it is taken with front camera.

## Known issues

- Video recording with external camera is not supported due to current video configuration limitations due to internal API design.

## Contributing

Feedback and code contributions are very much welcome. Just make a pull request with a short description of your changes. By making contributions to this project you give permission for your code to be used under the same [license](LICENSE).

## Credits

Maintained and sponsored by [Infinum](http://www.infinum.co).

<a href='https://infinum.co'>
  <img src='https://infinum.co/infinum.png' href='https://infinum.co' width='264'>
</a>
