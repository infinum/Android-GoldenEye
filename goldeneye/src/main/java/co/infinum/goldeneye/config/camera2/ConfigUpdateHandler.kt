package co.infinum.goldeneye.config.camera2

import android.app.Activity
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.FlashMode
import co.infinum.goldeneye.sessions.SessionsManager
import co.infinum.goldeneye.utils.CameraUtils

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class ConfigUpdateHandler(
    private val activity: Activity,
    private val textureView: TextureView,
    private val sessionsManager: SessionsManager,
    private val config: Camera2ConfigImpl
) {

    fun onPropertyUpdated(property: CameraProperty) {
        when (property) {
            CameraProperty.FOCUS -> sessionsManager.updateSession {
                set(CaptureRequest.CONTROL_AF_MODE, config.focusMode.toCamera2())
            }
            CameraProperty.FLASH -> sessionsManager.updateSession {
                updateFlashMode(this, config.flashMode)
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
            CameraProperty.ZOOM -> sessionsManager.updateSession { updateZoom(this, config.zoom) }
            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization()
            CameraProperty.PREVIEW_SCALE -> textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, textureView, config))
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

    private fun updateZoom(requestBuilder: CaptureRequest.Builder, zoom: Int) {
        val activeRect = config.characteristics?.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return
        val zoomPercentage = zoom / 100f
        val zoomedWidth = (activeRect.width() / zoomPercentage).toInt()
        val zoomedHeight = (activeRect.height() / zoomPercentage).toInt()
        val halfWidthDiff = (activeRect.width() - zoomedWidth) / 2
        val halfHeightDiff = (activeRect.height() - zoomedHeight) / 2
        val zoomedRect = Rect(
            activeRect.left + halfWidthDiff,
            activeRect.top + halfHeightDiff,
            activeRect.right - halfWidthDiff,
            activeRect.bottom - halfHeightDiff
        )
        requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomedRect)
    }

    private fun updateFlashMode(requestBuilder: CaptureRequest.Builder, flashMode: FlashMode) {
        if (flashMode == FlashMode.TORCH) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            requestBuilder.set(CaptureRequest.FLASH_MODE, flashMode.toCamera2())
        } else {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, flashMode.toCamera2())
            requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
        }
    }
}