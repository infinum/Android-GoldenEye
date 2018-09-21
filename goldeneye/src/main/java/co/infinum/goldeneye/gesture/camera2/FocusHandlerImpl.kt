package co.infinum.goldeneye.gesture.camera2

import android.app.Activity
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.MAIN_HANDLER
import co.infinum.goldeneye.gesture.FocusHandler
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.sessions.SessionsManager
import co.infinum.goldeneye.utils.CameraUtils

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class FocusHandlerImpl(
    private val activity: Activity,
    private val textureView: TextureView,
    private val config: CameraConfig,
    private val sessionsManager: SessionsManager,
    private val onFocusChanged: (Point) -> Unit
) : FocusHandler {

    override fun requestFocus(point: PointF) {
        if (config.isTapToFocusSupported.not() || config.supportedFocusModes.contains(FocusMode.AUTO).not()) return

        val region = CameraUtils.calculateCamera2FocusArea(activity, textureView, config, point.x, point.y)
        sessionsManager.lockFocus(region)
        onFocusChanged(Point(point.x.toInt(), point.y.toInt()))
        resetFocusWithDelay()
    }

    /**
     * Possible use case is that current focus mode is continuous and user
     * wants to tap to focus. If he taps, we have to switch focusMode to AUTO
     * and focus on tapped area.
     */
    private fun resetFocusWithDelay() {
        MAIN_HANDLER.removeCallbacksAndMessages(null)
        MAIN_HANDLER.postDelayed(
            { sessionsManager.unlockFocus(config.focusMode) },
            config.resetFocusDelay
        )
    }
}