package co.infinum.goldeneye.extensions

import android.app.Activity
import android.graphics.Bitmap
import android.hardware.Camera
import co.infinum.goldeneye.CameraConfig
import co.infinum.goldeneye.PictureConversionException
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.utils.LogDelegate

internal fun Camera.updateParams(update: Camera.Parameters.() -> Unit) {
    try {
        parameters = parameters?.apply(update)
    } catch (e: Exception) {
        LogDelegate.log(e)
    }
}