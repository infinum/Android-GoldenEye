package co.infinum.goldeneye.gesture

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PointF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.TextureView
import co.infinum.goldeneye.BaseGoldenEyeImpl
import co.infinum.goldeneye.extensions.MAIN_HANDLER
import co.infinum.goldeneye.models.CameraState

@SuppressLint("ClickableViewAccessibility")
internal class GestureManager(
    activity: Activity,
    textureView: TextureView,
    private val zoomHandler: ZoomHandler,
    private val focusHandler: FocusHandler
) {

    private val pinchDetector = ScaleGestureDetector(activity, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            zoomHandler.onPinchEnded()
        }

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            val pinchDelta = detector?.let { it.currentSpan - it.previousSpan } ?: 0f
            zoomHandler.onPinchStarted(pinchDelta)
            return true
        }
    })

    private val tapDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (e == null || BaseGoldenEyeImpl.state == CameraState.RECORDING) return false
            focusHandler.requestFocus(PointF(e.x, e.y))
            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }
    })

    init {
        textureView.setOnTouchListener { _, event ->
            tapDetector.onTouchEvent(event)
            pinchDetector.onTouchEvent(event)
            true
        }
    }

    fun release() {
        MAIN_HANDLER.removeCallbacksAndMessages(null)
    }
}