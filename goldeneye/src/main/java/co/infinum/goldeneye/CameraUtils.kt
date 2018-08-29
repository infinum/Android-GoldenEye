@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import android.app.Activity
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.Camera
import android.view.Surface
import android.view.TextureView

internal object CameraUtils {
    private const val FOCUS_AREA_SIZE = 200

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

    fun calculateFocusArea(config: CameraConfig, x: Float, y: Float): Camera.Area {
        val cameraWidthRatio = 2000f / config.previewSize.width
        val cameraHeightRatio = 2000f / config.previewSize.height

        val cameraX = if (config.orientation % 180 == 0) x else y
        val cameraY = if (config.orientation % 180 == 0) y else x

        val cameraFocusX = cameraWidthRatio * cameraX - 1000
        val cameraFocusY = cameraHeightRatio * cameraY - 1000
        val left = Math.max(-1000f, cameraFocusX).toInt()
        val top = Math.max(-1000f, cameraFocusY).toInt()

        val rect = Rect(
            Math.min(left, 1000 - FOCUS_AREA_SIZE),
            Math.min(top, 1000 - FOCUS_AREA_SIZE),
            Math.min(left + FOCUS_AREA_SIZE, 1000),
            Math.min(top + FOCUS_AREA_SIZE, 1000)
        )
        return Camera.Area(rect, 1000)
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