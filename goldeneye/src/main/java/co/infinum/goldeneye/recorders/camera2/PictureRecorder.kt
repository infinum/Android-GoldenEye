package co.infinum.goldeneye.recorders.camera2

import android.app.Activity
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import android.text.AndroidCharacter.mirror
import co.infinum.goldeneye.PictureCallback
import co.infinum.goldeneye.PictureConversionException
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.*
import co.infinum.goldeneye.models.DevicePreview
import co.infinum.goldeneye.models.Facing

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class PictureRecorder(
    private val activity: Activity,
    private val config: CameraConfig,
    private val devicePreview: DevicePreview
) {

    fun takePicture(callback: PictureCallback) {
        ifNotNull(devicePreview.imageReader, devicePreview.requestBuilder, devicePreview.session) { reader, builder, session ->
            reader.setOnImageAvailableListener({ _ ->
                async(
                    task = {
                        val image = reader.acquireLatestImage()
                        val bitmap = image.toBitmap()
                        image.close()
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
                        reader.setOnImageAvailableListener(null, null)
                        builder.removeTarget(reader.surface)
                        session.setRepeatingRequest(builder.build(), null, null)
                        if (it != null) {
                            callback.onPictureTaken(it)
                        } else {
                            callback.onError(PictureConversionException)
                        }
                    }
                )
            }, null)

            session.stopRepeating()
            builder.addTarget(reader.surface)
            session.capture(builder.build(), null, null)
        }
    }
}