@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.app.Activity
import android.hardware.Camera
import android.os.Build
import android.view.TextureView
import co.infinum.goldeneye.BaseGoldenEyeImpl
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.CameraState
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.utils.CameraUtils

internal class ConfigUpdateHandler(
    private val activity: Activity,
    private val camera: Camera,
    private val textureView: TextureView,
    private val config: CameraConfig
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
            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization(config.videoStabilizationEnabled)
            CameraProperty.PREVIEW_SCALE -> textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, textureView, config))
        }
    }

    private fun updatePictureSize(pictureSize: Size, previewSize: Size) {
        camera.updateParams { setPictureSize(pictureSize.width, pictureSize.height) }
        if (BaseGoldenEyeImpl.state != CameraState.RECORDING) {
            updatePreviewSize(previewSize)
        }
    }

    private fun updatePreviewSize(previewSize: Size) {
        camera.updateParams { setPreviewSize(previewSize.width, previewSize.height) }
        textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, textureView, config))
    }

    private fun updateVideoStabilization(enabled: Boolean) {
        camera.updateParams {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                videoStabilization = enabled
            }
        }
    }
}