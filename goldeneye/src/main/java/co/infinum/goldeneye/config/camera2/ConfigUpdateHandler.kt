package co.infinum.goldeneye.config.camera2

import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.FlashMode
import co.infinum.goldeneye.sessions.SessionsManager
import co.infinum.goldeneye.utils.CameraUtils

/**
 * Handles property updates. Syncs CameraConfig with active Camera2 session.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class ConfigUpdateHandler(
    private val sessionsManager: SessionsManager,
    private val config: Camera2ConfigImpl
) {

    fun onPropertyUpdated(property: CameraProperty) {
        when (property) {
            CameraProperty.FOCUS -> sessionsManager.updateSession {
                set(CaptureRequest.CONTROL_AF_MODE, config.focusMode.toCamera2())
            }
            CameraProperty.FLASH -> {
                sessionsManager.resetFlashMode()
                sessionsManager.updateSession { updateFlashMode(this, config.flashMode) }
            }
            CameraProperty.COLOR_EFFECT -> sessionsManager.updateSession {
                set(CaptureRequest.CONTROL_EFFECT_MODE, config.colorEffectMode.toCamera2())
            }
            CameraProperty.ANTIBANDING -> sessionsManager.updateSession {
                set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, config.antibandingMode.toCamera2())
            }
            CameraProperty.WHITE_BALANCE -> sessionsManager.updateSession {
                set(CaptureRequest.CONTROL_AWB_MODE, config.whiteBalanceMode.toCamera2())
            }
            CameraProperty.PICTURE_SIZE -> sessionsManager.restartSession()
            CameraProperty.PREVIEW_SIZE -> sessionsManager.restartSession()
            CameraProperty.ZOOM -> sessionsManager.updateSession { updateZoom(this) }
            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization()
            CameraProperty.PREVIEW_SCALE -> sessionsManager.restartSession()
        }
    }

    private fun updateVideoStabilization() {
        sessionsManager.updateSession {
            val videoStabilizationMode = if (config.videoStabilizationEnabled) {
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
            } else {
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
            }
            set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, videoStabilizationMode)
        }
    }

    /**
     * Update Camera2 zoom by measuring ZoomRect.
     */
    private fun updateZoom(requestBuilder: CaptureRequest.Builder) {
        /* Get active Rect size. This corresponds to actual camera size seen by Camera2 API */
        val zoomedRect = CameraUtils.calculateCamera2ZoomRect(config) ?: return

        /* BAM */
        requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomedRect)
    }

    /**
     * Update Flash mode. Edge case is if it is FlashMode.TORCH is applied.
     * Flash mode is handled by AE_MODE except FlashMode.TORCH. If TORCH is
     * set, make sure to disable AE_MODE, otherwise they clash.
     */
    private fun updateFlashMode(requestBuilder: CaptureRequest.Builder, flashMode: FlashMode) {
        if (flashMode == FlashMode.TORCH) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            requestBuilder.set(CaptureRequest.FLASH_MODE, FlashMode.TORCH.toCamera2())
        } else {
            requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, flashMode.toCamera2())
        }
    }
}