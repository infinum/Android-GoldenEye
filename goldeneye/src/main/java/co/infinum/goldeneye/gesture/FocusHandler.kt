package co.infinum.goldeneye.gesture

import android.graphics.PointF

/**
 * Reusable interface used in [GestureManager] to
 * support multiple implementations.
 */
internal interface FocusHandler {
    fun requestFocus(point: PointF)
}

