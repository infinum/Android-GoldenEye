@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.gesture.camera1

import android.app.Activity
import android.graphics.Point
import android.graphics.PointF
import android.hardware.Camera
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.mainHandler
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.gesture.FocusHandler
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.utils.CameraUtils

internal class FocusHandlerImpl(
    private val activity: Activity,
    private val camera: Camera?,
    private val textureView: TextureView?,
    private val config: CameraConfig,
    private val onFocusChanged: (Point) -> Unit
) : FocusHandler {

    override fun requestFocus(point: PointF) {
        if (config.tapToFocusEnabled.not() && config.supportedFocusModes.contains(FocusMode.AUTO)) return

        ifNotNull(camera, textureView) { camera, textureView ->
            camera.updateParams {
                focusMode = FocusMode.AUTO.toCamera1()
                val areas = CameraUtils.calculateCamera1FocusArea(activity, textureView, config, point.x, point.y)
                if (maxNumFocusAreas > 0) {
                    focusAreas = areas
                    onFocusChanged(Point(point.x.toInt(), point.y.toInt()))
                }
            }

            camera.autoFocus { success, _ ->
                if (success) {
                    camera.cancelAutoFocus()
                    resetFocusWithDelay()
                }
            }
        }
    }

    /**
     * Possible use case is that current focus mode is continuous and user
     * wants to tap to focus. If he taps, we have to switch focusMode to AUTO
     * and focus on tapped area.
     */
    private fun resetFocusWithDelay() {
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed(
            { camera?.updateParams { focusMode = config.focusMode.toCamera1() } },
            config.resetFocusDelay
        )
    }
}