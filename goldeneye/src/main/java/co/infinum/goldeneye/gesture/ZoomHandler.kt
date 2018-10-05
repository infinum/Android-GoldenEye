package co.infinum.goldeneye.gesture

/**
 * Reusable interface used in [GestureManager] to
 * support multiple implementations.
 */
internal interface ZoomHandler {
    fun onPinchStarted(pinchDelta: Float)
    fun onPinchEnded()
}

