package co.infinum.goldeneye.gesture.camera2

import android.app.Activity
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.camera2.Camera2ConfigImpl
import co.infinum.goldeneye.extensions.MAIN_HANDLER
import co.infinum.goldeneye.gesture.FocusHandler
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.sessions.SessionsManager
import co.infinum.goldeneye.utils.CameraUtils

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class FocusHandlerImpl(
    private val activity: Activity,
    private val textureView: TextureView,
    private val config: Camera2ConfigImpl,
    private val sessionsManager: SessionsManager,
    private val onFocusChanged: (Point) -> Unit
) : FocusHandler {

    override fun requestFocus(point: PointF) {
        if (config.tapToFocusEnabled.not() || config.supportedFocusModes.contains(FocusMode.AUTO).not()) return

        val region = CameraUtils.calculateCamera2FocusArea(activity, textureView, config, point.x, point.y)
        if (region != null) {
            sessionsManager.lockFocus(region)
            onFocusChanged(Point(point.x.toInt(), point.y.toInt()))
            resetFocusWithDelay()
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
            { sessionsManager.unlockFocus(config.focusMode) },
            config.tapToFocusResetDelay
        )
    }
}