@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.extensions

import android.hardware.Camera
import co.infinum.goldeneye.utils.LogDelegate.log

/**
 * Batch update camera parameters and apply them to Camera instantly.
 */
internal fun Camera.updateParams(update: Camera.Parameters.() -> Unit) {
    try {
        parameters = parameters?.apply(update)
    } catch (t: Throwable) {
        log("Failed to update Camera properties.", t)
    }
}