package co.infinum.goldeneye

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Point
import android.hardware.Camera
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.limit
import co.infinum.goldeneye.extensions.mainHandler
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.utils.CameraUtils

internal class GestureHandler(
    private val activity: Activity,
    private val camera: Camera,
    private val config: CameraConfig,
    private val onZoomChangeCallback: OnZoomChangeCallback? = null,
    private val onFocusChangeCallback: OnFocusChangeCallback? = null
) {

    private val zoomDeltaSpan: Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            2f,
            activity.resources.displayMetrics
        ).toInt()
    private var textureView: TextureView? = null
    private var spanDelta = 0f

    private val pinchDetector = ScaleGestureDetector(activity, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            spanDelta = 0f
        }

        override fun onScale(detector: ScaleGestureDetector?): Boolean {
            if (detector == null
                || config.pinchToZoomEnabled.not()
            ) {
                return true
            }

            spanDelta += detector.currentSpan - detector.previousSpan
            val zoomLevelDelta = (spanDelta / (zoomDeltaSpan * config.pinchToZoomFriction)).toInt()

            if (zoomLevelDelta != 0) {
                val zoomLevel = (config.zoom.level + zoomLevelDelta).limit(0, config.maxZoom.level)
                config.zoom = config.supportedZooms[zoomLevel]
                onZoomChangeCallback?.onZoomChanged(config.zoom)
            }

            spanDelta %= zoomDeltaSpan
            return true
        }
    })

    private val tapDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (e == null
                || config.tapToFocusEnabled.not()
                || config.supportedFocusModes.contains(FocusMode.AUTO).not()
            ) {
                return true
            }

            ifNotNull(camera, textureView) { camera, textureView ->
                camera.updateParams {
                    focusMode = FocusMode.AUTO.key
                    val areas = CameraUtils.calculateFocusArea(activity, textureView, config, e.x, e.y)
                    if (maxNumFocusAreas > 0) {
                        focusAreas = areas
                        onFocusChangeCallback?.onFocusChanged(Point(e.x.toInt(), e.y.toInt()))
                    }
                }

                camera.autoFocus { success, _ ->
                    if (success) {
                        camera.cancelAutoFocus()
                        resetFocusWithDelay()
                    }
                }
            }

            return true
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }
    })

    /**
     * Possible use case is that current focus mode is continuous and user
     * wants to tap to focus. If he taps, we have to switch focusMode to AUTO
     * and focus on tapped area.
     */
    private fun resetFocusWithDelay() {
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed(
            { camera.updateParams { focusMode = config.focusMode.key } },
            config.resetFocusDelay
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    fun init(textureView: TextureView) {
        this.textureView = textureView
        textureView.setOnTouchListener { _, event ->
            tapDetector.onTouchEvent(event)
            pinchDetector.onTouchEvent(event)
            true
        }
    }

    fun release() {
        mainHandler.removeCallbacksAndMessages(null)
    }
}