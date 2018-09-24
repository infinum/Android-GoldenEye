@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.recorders

import android.app.Activity
import android.graphics.Bitmap
import android.hardware.Camera
import co.infinum.goldeneye.PictureCallback
import co.infinum.goldeneye.PictureConversionException
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.*
import co.infinum.goldeneye.models.Facing

internal class PictureRecorder(
    private val activity: Activity,
    private val camera: Camera,
    private val config: CameraConfig
) {

    private var pictureCallback: PictureCallback? = null

    private val onShutter: () -> Unit = { pictureCallback?.onShutter() }

    private val convertBitmapTask: (ByteArray) -> Bitmap? =
        {
            val bitmap = it.toBitmap()
            bitmap?.applyMatrix {
                reverseCameraRotation(
                    activity = activity,
                    info = config,
                    cx = bitmap.width / 2f,
                    cy = bitmap.height / 2f
                )
                if (config.facing == Facing.FRONT) {
                    mirror()
                }
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
                    task = { convertBitmapTask(data) },
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