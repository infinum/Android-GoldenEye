@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import android.app.Activity
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.Camera
import android.view.Surface
import android.view.TextureView

internal object CameraUtils {
    private const val FOCUS_AREA_SIZE = 300

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

        val (scaleX, scaleY, scale) = calculateScale(activity, config, textureView)

        matrix.setScale(1 / scaleX * scale, 1 / scaleY * scale, textureView.width / 2f, textureView.height / 2f)
        return matrix
    }

    fun calculateFocusArea(activity: Activity, textureView: TextureView, config: CameraConfig, x: Float, y: Float): List<Camera.Area> {
        val (_, _, scale) = calculateScale(activity, config, textureView)
        val displayOrientation = calculateDisplayOrientation(activity, config)

        val scaledPreviewX = config.previewSize.width * scale
        val scaledPreviewY = config.previewSize.height * scale

        val rotatedTextureViewX = if (displayOrientation % 180 == 0) textureView.width else textureView.height
        val rotatedTextureViewY = if (displayOrientation % 180 == 0) textureView.height else textureView.width

        val rotatedX = when (displayOrientation) {
            90 -> y
            180 -> textureView.width - x
            270 -> textureView.height - y
            else -> x
        }
        val rotatedY = when (displayOrientation) {
            90 -> textureView.width - x
            180 -> textureView.height - y
            270 -> x
            else -> y
        }

        if (touchNotInPreview(rotatedTextureViewX, rotatedTextureViewY, scaledPreviewX, scaledPreviewY, rotatedX, rotatedY)) {
            return emptyList()
        }

        val translatedPreviewX = rotatedX - Math.max(0f, (rotatedTextureViewX - scaledPreviewX) / 2)
        val translatedPreviewY = rotatedY - Math.max(0f, (rotatedTextureViewY - scaledPreviewY) / 2)

        val cameraWidthRatio = 2000f / scaledPreviewX
        val cameraHeightRatio = 2000f / scaledPreviewY

        val cameraFocusX = cameraWidthRatio * translatedPreviewX - 1000
        val cameraFocusY = cameraHeightRatio * translatedPreviewY - 1000

        val left = Math.max(-1000f, cameraFocusX).toInt()
        val top = Math.max(-1000f, cameraFocusY).toInt()

        val rect = Rect(
            Math.min(left, 1000 - FOCUS_AREA_SIZE),
            Math.min(top, 1000 - FOCUS_AREA_SIZE),
            Math.min(left + FOCUS_AREA_SIZE, 1000),
            Math.min(top + FOCUS_AREA_SIZE, 1000)
        )
        return listOf(Camera.Area(rect, 1000))
    }

    private fun touchNotInPreview(
        rotatedTextureViewX: Int,
        rotatedTextureViewY: Int,
        scaledPreviewX: Float,
        scaledPreviewY: Float,
        x: Float, y: Float
    ): Boolean {

        val diffX = Math.max(0f,(rotatedTextureViewX - scaledPreviewX) / 2)
        val diffY = Math.max(0f, (rotatedTextureViewY - scaledPreviewY) / 2)
        return x < diffX || x > diffX + scaledPreviewX || y < diffY || y > diffY + scaledPreviewY
    }

    private fun calculateScale(activity: Activity, config: CameraConfig, textureView: TextureView): Triple<Float, Float, Float> {
        val previewSize = config.previewSize
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
                PreviewScale.NO_SCALE -> 1f
            }
        return Triple(scaleX, scaleY, scale)
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