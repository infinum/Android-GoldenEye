@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.recorders

import android.app.Activity
import android.graphics.Bitmap
import android.hardware.Camera
import co.infinum.goldeneye.PictureCallback
import co.infinum.goldeneye.PictureConversionException
import co.infinum.goldeneye.PictureTransformation
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.async
import co.infinum.goldeneye.extensions.toBitmap
import co.infinum.goldeneye.utils.CameraUtils

internal class PictureRecorder(
    private val activity: Activity,
    private val camera: Camera,
    private val config: CameraConfig,
    private val pictureTransformation: PictureTransformation
) {

    private var pictureCallback: PictureCallback? = null

    private val onShutter: () -> Unit = { pictureCallback?.onShutter() }

    private val transformBitmapTask: (ByteArray) -> Bitmap? = {
        val bitmap = it.toBitmap()
        if (bitmap != null) {
            val orientationDifference = CameraUtils.calculateDisplayOrientation(activity, config).toFloat()
            pictureTransformation.transform(bitmap, config, orientationDifference)
        } else {
            null
        }
    }

    private val onResult: (Bitmap?) -> Unit = {
        if (it != null) {
            pictureCallback?.onPictureTaken(it)
        } else {
            pictureCallback?.onError(PictureConversionException)
        }
    }

    fun takePicture(callback: PictureCallback) {
        this.pictureCallback = callback
        try {
            val cameraShutterCallback = Camera.ShutterCallback { onShutter() }
            val cameraPictureCallback = Camera.PictureCallback { data, _ ->
                async(
                    task = { transformBitmapTask(data) },
                    onResult = { onResult(it) }
                )
            }

            camera.takePicture(cameraShutterCallback, null, cameraPictureCallback)
        } catch (t: Throwable) {
            callback.onError(t)
        }
    }

    fun release() {
        this.pictureCallback = null
    }
}