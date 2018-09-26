@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.gesture.camera1

import android.app.Activity
import android.graphics.Point
import android.graphics.PointF
import android.hardware.Camera
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.MAIN_HANDLER
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.gesture.FocusHandler
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.utils.CameraUtils

internal class FocusHandlerImpl(
    private val activity: Activity,
    private val camera: Camera,
    private val textureView: TextureView,
    private val config: CameraConfig,
    private val onFocusChanged: (Point) -> Unit
) : FocusHandler {

    override fun requestFocus(point: PointF) {
        if (config.tapToFocusEnabled.not() || config.supportedFocusModes.contains(FocusMode.AUTO).not()) return

        camera.updateParams {
            /* When applying tap to focus, change focus to AUTO */
            focusMode = FocusMode.AUTO.toCamera1()
            val areas = CameraUtils.calculateCamera1FocusArea(activity, textureView, config, point.x, point.y)
            focusAreas = areas
            onFocusChanged(Point(point.x.toInt(), point.y.toInt()))
        }

        camera.autoFocus { success, _ ->
            if (success) {
                /* Reset auto focus once it is focused */
                camera.cancelAutoFocus()
                resetFocusWithDelay()
            }
        }
    }

    /**
     * Reset focus to chosen focus after [CameraConfig.tapToFocusResetDelay] milliseconds.
     *
     * @see CameraConfig.tapToFocusResetDelay
     */
    private fun resetFocusWithDelay() {
        MAIN_HANDLER.removeCallbacksAndMessages(null)
        MAIN_HANDLER.postDelayed(
            { camera.updateParams { focusMode = config.focusMode.toCamera1() } },
            config.tapToFocusResetDelay
        )
    }
}