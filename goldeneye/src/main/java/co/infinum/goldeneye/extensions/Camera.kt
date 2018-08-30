package co.infinum.goldeneye.extensions

import android.graphics.Bitmap
import android.hardware.Camera
import co.infinum.goldeneye.PictureConversionException
import co.infinum.goldeneye.utils.LogDelegate

internal fun Camera.updateParams(update: Camera.Parameters.() -> Unit) {
    try {
        parameters = parameters?.apply(update)
    } catch (e: Exception) {
        LogDelegate.log(e)
    }
}

internal fun Camera.takePicture(
    onShutter: () -> Unit,
    onPicture: (Bitmap) -> Unit,
    onError: (Throwable) -> Unit
) {
    val shutterCallback = Camera.ShutterCallback { onShutter() }
    val pictureCallback = Camera.PictureCallback { data, _ ->
        async(
            task = { data.toBitmap() },
            onResult = {
                if (it != null) {
                    onPicture(it)
                } else {
                    onError(PictureConversionException)
                }
            }
        )
    }
    takePicture(shutterCallback, null, pictureCallback)
}