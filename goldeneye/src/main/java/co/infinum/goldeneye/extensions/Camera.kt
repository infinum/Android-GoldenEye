package co.infinum.goldeneye.extensions

import android.hardware.Camera
import co.infinum.goldeneye.utils.LogDelegate

internal fun Camera.updateParams(update: Camera.Parameters.() -> Unit) {
    try {
        parameters = parameters?.apply(update)
    } catch (e: Exception) {
        LogDelegate.log(e)
    }
}