@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.hardware.Camera
import android.os.Build
import co.infinum.goldeneye.BaseGoldenEyeImpl
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.CameraState
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size

/**
 * Handles property updates. Syncs CameraConfig with active Camera.
 *
 * Sometimes we have to restart the preview so we have to pass function
 * that does that because it is not accessible from this class otherwise.
 */
internal class ConfigUpdateHandler(
    private val camera: Camera,
    private val config: CameraConfig,
    private val restartPreview: () -> Unit
) {

    fun onPropertyUpdated(property: CameraProperty) {
        when (property) {
            CameraProperty.FOCUS -> camera.updateParams { focusMode = config.focusMode.toCamera1() }
            CameraProperty.FLASH -> camera.updateParams { flashMode = config.flashMode.toCamera1() }
            CameraProperty.COLOR_EFFECT -> camera.updateParams { colorEffect = config.colorEffectMode.toCamera1() }
            CameraProperty.ANTIBANDING -> camera.updateParams { antibanding = config.antibandingMode.toCamera1() }
            CameraProperty.WHITE_BALANCE -> camera.updateParams { whiteBalance = config.whiteBalanceMode.toCamera1() }
            CameraProperty.PICTURE_SIZE -> updatePictureSize(config.pictureSize, config.previewSize)
            CameraProperty.PREVIEW_SIZE -> updatePreviewSize(config.previewSize)
            CameraProperty.ZOOM -> camera.updateParams { zoom = zoomRatios.indexOf(config.zoom) }
            CameraProperty.VIDEO_STABILIZATION -> camera.updateParams {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    videoStabilization = config.videoStabilizationEnabled
                }
            }
            CameraProperty.PREVIEW_SCALE -> restartPreview()
        }
    }

    private fun updatePictureSize(pictureSize: Size, previewSize: Size) {
        camera.updateParams { setPictureSize(pictureSize.width, pictureSize.height) }
        /* Update preview if AUTO_* mode is active and video is not being recorded */
        if ((config.previewScale == PreviewScale.AUTO_FILL || config.previewScale == PreviewScale.AUTO_FIT)
            && BaseGoldenEyeImpl.state != CameraState.RECORDING_VIDEO
        ) {
            updatePreviewSize(previewSize)
        }
    }

    private fun updatePreviewSize(previewSize: Size) {
        camera.updateParams { setPreviewSize(previewSize.width, previewSize.height) }
        restartPreview()
    }
}