package co.infinum.goldeneye

import android.graphics.Bitmap
import android.graphics.Point
import co.infinum.goldeneye.config.CameraConfig
import java.io.File

/**
 * [onPictureTaken] is received iff picture is successfully
 * taken and transformation is finished.
 *
 * [onError] is received if any error happens during the initialization
 * or while picture is being taken.
 *
 * [onShutter] method is called when picture is taken
 * and before it is processed. It can be useful to let
 * the user know that picture is taken with sound or
 * some other alert.
 *
 * @see GoldenEye.takePicture
 */
abstract class PictureCallback {
    abstract fun onPictureTaken(picture: Bitmap)
    abstract fun onError(t: Throwable)
    open fun onShutter() {}
}

/**
 * [onVideoRecorded] method returns the same file that is passed
 * in [GoldenEye.startRecording], it's just a convenience that
 * you don't need to keep reference to the file yourself.
 *
 * [onError] is received if any error happens during the initialization
 * or while video is being recorded.
 *
 * @see GoldenEye.startRecording
 */
interface VideoCallback {
    fun onVideoRecorded(file: File)
    fun onError(t: Throwable)
}

/**
 * [onReady] is received when Camera is opened and config is ready
 * to be modified. It is called just before preview is shown.
 *
 * [onActive] is received when Camera preview is active and visible.
 *
 * [onError] is received if any error happens during the initialization.
 *
 * @see GoldenEye.open
 */
abstract class InitCallback {
    open fun onReady(config: CameraConfig) {}
    open fun onActive() {}
    abstract fun onError(t: Throwable)
}

/**
 * Callback can be used to display some focus animation to the user
 * on the focused area.
 *
 * [onFocusChanged] is received each time camera focus is changed via
 * tap to focus functionality.
 * [Point] received represents (x,y) where user clicked on the [android.view.TextureView]
 * which is used for camera preview.
 */
interface OnFocusChangedCallback {
    fun onFocusChanged(point: Point)
}

/**
 * [onZoomChanged] is called on every zoom value change.
 *
 * @see CameraConfig.zoom
 */
interface OnZoomChangedCallback {
    fun onZoomChanged(zoom: Int)
}