package co.infinum.goldeneye.gesture

internal interface ZoomHandler {
    fun onPinchStarted(pinchDelta: Float)
    fun onPinchEnded()
}

