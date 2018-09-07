package co.infinum.goldeneye

import android.app.Activity
import android.hardware.Camera
import co.infinum.goldeneye.config.CameraConfigImpl
import co.infinum.goldeneye.extensions.*
import co.infinum.goldeneye.models.Facing

internal class PictureRecorder(
    private val activity: Activity,
    private val camera: Camera,
    private val config: CameraConfigImpl
) {

    fun takePicture(callback: PictureCallback) {

        try {
            config.locked = true
            val shutterCallback = Camera.ShutterCallback { callback.onShutter() }
            val pictureCallback = Camera.PictureCallback { data, _ ->
                async(
                    task = {
                        val bitmap = data.toBitmap()
                        bitmap?.mutate {
                            reverseCameraRotation(
                                activity = activity,
                                config = config,
                                cx = bitmap.width / 2f,
                                cy = bitmap.height / 2f
                            )
                            if (config.facing == Facing.FRONT) {
                                mirror()
                            }
                        }
                    },
                    onResult = {
                        unlock()
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
            unlock()
            callback.onError(t)
        }
    }

    private fun unlock() {
        config.locked = false
        camera.startPreview()
    }
}