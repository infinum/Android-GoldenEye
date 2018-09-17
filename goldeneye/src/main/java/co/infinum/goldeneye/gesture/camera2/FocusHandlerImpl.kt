package co.infinum.goldeneye.gesture.camera2

import android.app.Activity
import android.graphics.Point
import android.graphics.PointF
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.mainHandler
import co.infinum.goldeneye.gesture.FocusHandler
import co.infinum.goldeneye.models.DevicePreview
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.utils.CameraUtils

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class FocusHandlerImpl(
    private val activity: Activity,
    private val textureView: TextureView,
    private val config: CameraConfig,
    private val devicePreview: DevicePreview,
    private val onFocusChanged: (Point) -> Unit
) : FocusHandler {

    override fun requestFocus(point: PointF) {
        if (config.isTapToFocusSupported.not() || config.supportedFocusModes.contains(FocusMode.AUTO).not()) return

        ifNotNull(devicePreview.session, devicePreview.requestBuilder) { session, builder ->
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            session.capture(builder.build(), null, null)

            devicePreview.updateRequest {
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
                val region = CameraUtils.calculateCamera2FocusArea(activity, textureView, config, point.x, point.y)
                set(CaptureRequest.CONTROL_AF_REGIONS, region)
            }
            resetFocusWithDelay()
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
            {
                devicePreview.updateRequest {
                    set(CaptureRequest.CONTROL_AF_MODE, config.focusMode.toCamera2())
                    set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
                    set(CaptureRequest.CONTROL_AF_REGIONS, null)
                }
            },
            config.resetFocusDelay
        )
    }
}