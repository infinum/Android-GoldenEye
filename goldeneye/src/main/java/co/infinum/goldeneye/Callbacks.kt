package co.infinum.goldeneye

import android.graphics.Bitmap
import android.graphics.Point
import java.io.File

/**
 * Callback used when taking pictures.
 *
 * onShutter method is called when Image is taken
 * and before it is processed. It can be useful to let
 * the user know that Image is taken with sound or
 * some other alert.
 */
abstract class PictureCallback {
    abstract fun onPictureTaken(picture: Bitmap)
    abstract fun onError(t: Throwable)
    open fun onShutter() {
    }
}

/**
 * Callback used when recording videos.
 */
interface VideoCallback {
    fun onVideoRecorded(file: File)
    fun onError(t: Throwable)
}

/**
 * Callback used when initializing camera.
 */
interface InitCallback {
    fun onConfigReady()
    fun onError(t: Throwable)
}

interface OnFocusChangedCallback {
    fun onFocusChanged(point: Point)
}

interface OnZoomChangedCallback {
    fun onZoomChanged(zoom: Int)
}