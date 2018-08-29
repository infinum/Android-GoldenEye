package co.infinum.goldeneye

import android.app.Activity
import android.graphics.Matrix
import android.view.Surface
import android.view.TextureView

internal object CameraUtils {

    fun calculateDisplayOrientation(activity: Activity, config: CameraConfig): Int {
        val deviceOrientation = getDeviceOrientation(activity)
        val cameraOrientation = config.orientation

        return if (config.facing == Facing.FRONT) {
            (360 - (cameraOrientation + deviceOrientation) % 360) % 360
        } else {
            (cameraOrientation - deviceOrientation + 360) % 360
        }
    }

    fun calculateTextureMatrix(activity: Activity, config: CameraConfig, textureView: TextureView): Matrix {
        val matrix = Matrix()
        val previewSize = config.previewSize
        if (textureView.isNotMeasured() || previewSize == Size.UNKNOWN) {
            return matrix
        }

        val displayOrientation = calculateDisplayOrientation(activity, config)

        val scaleX =
            if (displayOrientation % 180 == 0) {
                textureView.width.toFloat() / previewSize.width
            } else {
                textureView.width.toFloat() / previewSize.height
            }

        val scaleY =
            if (displayOrientation % 180 == 0) {
                textureView.height.toFloat() / previewSize.height
            } else {
                textureView.height.toFloat() / previewSize.width
            }

        val scale =
            when (config.previewScale) {
                PreviewScale.SCALE_TO_FILL -> Math.max(scaleX, scaleY)
                PreviewScale.SCALE_TO_FIT -> Math.min(scaleX, scaleY)
                PreviewScale.FIT -> if (Math.min(scaleX, scaleY) < 1) Math.min(scaleX, scaleY) else 1f
            }

        matrix.setScale(1 / scaleX * scale, 1 / scaleY * scale, textureView.width / 2f, textureView.height / 2f)
        return matrix
    }

    fun <T : CameraConfig> findCamera(availableCameras: List<T>, facing: Facing): T? {
        for (cameraConfig in availableCameras) {
            if (cameraConfig.facing == facing) {
                return cameraConfig
            }
        }
        return null
    }

    fun <T : CameraConfig> hasFacing(availableCameras: List<T>, facing: Facing): Boolean {
        return findCamera(availableCameras, facing) != null
    }

    fun <T : CameraConfig> nextCamera(availableCameras: List<T>, currentCameraConfig: T): T? {
        val currentIndex = availableCameras.indexOf(currentCameraConfig)
        return if (currentIndex != -1) availableCameras[(currentIndex + 1) % availableCameras.size] else null
    }

    private fun getDeviceOrientation(activity: Activity) =
        when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            Surface.ROTATION_0 -> 0
            else -> 0
        }
}