@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.recorders

import android.app.Activity
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

    fun takePicture(callback: PictureCallback) {
        try {
            val shutterCallback = Camera.ShutterCallback { callback.onShutter() }
            val pictureCallback = Camera.PictureCallback { data, _ ->
                async(
                    task = {
                        val bitmap = data.toBitmap()
                        bitmap?.mutate {
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
                    },
                    onResult = {
                        if (it != null) {
                            callback.onPictureTaken(it)
                        } else {
                            callback.onError(PictureConversionException)
                        }
                    }
                )
            }
            camera.takePicture(shutterCallback, null, pictureCallback)
        } catch (t: Throwable) {
            callback.onError(t)
        }
    }
}