@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.config.camera1

import android.app.Activity
import android.hardware.Camera
import android.os.Build
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.models.CameraProperty
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
            CameraProperty.COLOR_EFFECT -> camera.updateParams { colorEffect = config.colorEffect.toCamera1() }
            CameraProperty.ANTIBANDING -> camera.updateParams { antibanding = config.antibanding.toCamera1() }
            CameraProperty.SCENE_MODE -> camera.updateParams { sceneMode = config.sceneMode.toCamera1() }
            CameraProperty.WHITE_BALANCE -> camera.updateParams { whiteBalance = config.whiteBalance.toCamera1() }
            CameraProperty.PICTURE_SIZE -> updatePictureSize()
            CameraProperty.PREVIEW_SIZE -> updatePreviewSize()
            CameraProperty.ZOOM -> camera.updateParams {
                //TODO update zoom
            }
            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization()
            CameraProperty.PREVIEW_SCALE -> textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
        }
    }

    private fun updatePictureSize() {
        val pictureSize = config.pictureSize
        camera.updateParams { setPictureSize(pictureSize.width, pictureSize.height) }
        if (config.autoPickPreviewSize) {
            config.previewSize = CameraUtils.findBestMatchingSize(pictureSize, config.supportedPreviewSizes)
        }
    }

    private fun updatePreviewSize() {
        val previewSize = config.previewSize
        camera.updateParams { setPreviewSize(previewSize.width, previewSize.height) }
        textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
    }

    private fun updateVideoStabilization() {
        camera.updateParams {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                videoStabilization = config.videoStabilizationEnabled
            }
        }
    }
}