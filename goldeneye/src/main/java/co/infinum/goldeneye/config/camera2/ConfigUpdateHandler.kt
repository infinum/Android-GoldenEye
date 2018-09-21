package co.infinum.goldeneye.config.camera2

import android.app.Activity
import android.graphics.Rect
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.FlashMode
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.sessions.SessionsManager
import co.infinum.goldeneye.utils.CameraUtils

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class ConfigUpdateHandler(
    private val activity: Activity,
    private val textureView: TextureView,
    private val sessionsSyncManager: SessionsManager,
    private val config: CameraConfig
) {

    fun onPropertyUpdated(property: CameraProperty) {
        when (property) {
            CameraProperty.FOCUS -> sessionsSyncManager.updateRequests {
                set(CaptureRequest.CONTROL_AF_MODE, config.focusMode.toCamera2())
            }
            CameraProperty.FLASH -> sessionsSyncManager.updateRequests {
                updateFlashMode(this, config.flashMode)
            }
            CameraProperty.COLOR_EFFECT -> sessionsSyncManager.updateRequests {
                set(CaptureRequest.CONTROL_EFFECT_MODE, config.colorEffect.toCamera2())
            }
            CameraProperty.ANTIBANDING -> sessionsSyncManager.updateRequests {
                set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, config.antibanding.toCamera2())
            }
            CameraProperty.SCENE_MODE -> sessionsSyncManager.updateRequests {
                set(CaptureRequest.CONTROL_SCENE_MODE, config.sceneMode.toCamera2())
            }
            CameraProperty.WHITE_BALANCE -> sessionsSyncManager.updateRequests {
                set(CaptureRequest.CONTROL_AWB_MODE, config.whiteBalance.toCamera2())
            }
            CameraProperty.PICTURE_SIZE -> updatePictureSize(config.pictureSize)
            CameraProperty.PREVIEW_SIZE -> {
            } //TODO restart preview
            CameraProperty.ZOOM -> sessionsSyncManager.updateRequests { updateZoom(this, config.zoom) }
            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization()
            CameraProperty.PREVIEW_SCALE -> textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
        }
    }

    private fun updateVideoStabilization() {
        sessionsSyncManager.updateRequests {
            val videoStabilizationMode = if (config.videoStabilizationEnabled) {
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
            } else {
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
            }
            set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, videoStabilizationMode)
        }
    }

    private fun updateZoom(requestBuilder: CaptureRequest.Builder, zoom: Int) {
        val zoomPercentage = zoom / 100f
        val zoomedWidth = (config.previewSize.width / zoomPercentage).toInt()
        val zoomedHeight = (config.previewSize.height / zoomPercentage).toInt()
        val halfWidthDiff = (config.previewSize.width - zoomedWidth) / 2
        val halfHeightDiff = (config.previewSize.height - zoomedHeight) / 2
        val zoomedRect = Rect(
            halfWidthDiff,
            halfHeightDiff,
            config.previewSize.width - halfWidthDiff,
            config.previewSize.height - halfHeightDiff
        )
        requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomedRect)
    }

    private fun updatePictureSize(size: Size) {
        //TODO update image reader with size
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