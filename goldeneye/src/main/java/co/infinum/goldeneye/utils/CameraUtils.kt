@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.utils

import android.app.Activity
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.camera2.params.MeteringRectangle
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.BaseGoldenEyeImpl
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.camera2.Camera2ConfigImpl
import co.infinum.goldeneye.extensions.isNotMeasured
import co.infinum.goldeneye.models.CameraApi
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size
import kotlin.math.max
import kotlin.math.min

internal object CameraUtils {
    private const val FOCUS_AREA_SIZE = 200

    /**
     * Camera and device orientation do not sync. This method will calculate
     * their orientation difference so that can be used to sync them manually.
     */
    fun calculateDisplayOrientation(activity: Activity, config: CameraInfo): Int {
        val deviceOrientation = getDeviceOrientation(activity)
        val cameraOrientation = config.orientation

        return if (config.facing == Facing.FRONT) {
            (360 - (cameraOrientation + deviceOrientation) % 360) % 360
        } else {
            (cameraOrientation - deviceOrientation + 360) % 360
        }
    }

    /**
     * This is needed to transform Preview so that it is not distorted.
     *
     * When camera is given a texture view, it will scale its preview to take full width and height
     * of given texture view. So if you have Preview size of 50x50, and texture view of 100x200, it
     * will automatically scale width 2x and height 4x. Different width and height scale will lead
     * to distorted images. To fix this issue we have to calculate this automatic scaling and reverse
     * the process.
     *
     * After we reverse the process, we can scale our preview however we want regarding to PreviewScale
     * of current config.
     */
    fun calculateTextureMatrix(activity: Activity, textureView: TextureView, config: CameraConfig): Matrix {
        val matrix = Matrix()
        val previewSize = config.previewSize
        if (textureView.isNotMeasured() || previewSize == Size.UNKNOWN) {
            return matrix
        }

        /* scaleX and scaleY are used to reverse the process and scale is used to scale image according to PreviewScale */
        val (scaleX, scaleY, scale) = calculateScale(activity, textureView, config)

        if (BaseGoldenEyeImpl.version == CameraApi.VERSION_2 && getDeviceOrientation(activity) % 180 != 0) {
            matrix.postScale(
                textureView.height / textureView.width.toFloat() / scaleY * scale,
                textureView.width / textureView.height.toFloat() / scaleX * scale,
                textureView.width / 2f,
                textureView.height / 2f
            )
            matrix.postRotate(
                calculateDisplayOrientation(activity, config).toFloat() - config.orientation,
                textureView.width / 2f,
                textureView.height / 2f
            )
        } else {
            matrix.postScale(1 / scaleX * scale, 1 / scaleY * scale, textureView.width / 2f, textureView.height / 2f)
        }

        return matrix
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun calculateCamera2FocusArea(
        activity: Activity,
        textureView: TextureView,
        config: Camera2ConfigImpl,
        x: Float,
        y: Float
    ): Array<MeteringRectangle> {
        //TODO scale to available array size rectdamn nigga
        val rect = calculateFocusRect(activity, textureView, config, x, y)
        return arrayOf(MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_MAX - 1))
    }

    /**
     * This... oh...
     *
     * There are 5 different values in here that we must sync. Preview size, Texture view size,
     * Real preview size that is actually displayed inside texture view after applying scale matrix,
     * actual touch point which we want to focus on and finally, the f****** genius, focus area
     * that must use coordinates [-1000, 1000] and doesn't give a crap about your preview size
     * or orientation.
     *
     * What we must accomplish is scale x,y coordinates that user pressed to [-1000, 1000] coordinates
     * and manually do all the scaling and potential rotation because camera and device orientation
     * is not in sync.
     */
    fun calculateCamera1FocusArea(
        activity: Activity,
        textureView: TextureView,
        config: CameraConfig,
        x: Float,
        y: Float
    ): List<Camera.Area>? {

        val rect = calculateFocusRect(activity, textureView, config, x, y) ?: return null

        val previewSize = config.previewSize
        /* Ratio of genius [-1000,1000] coordinates to scaled preview size */
        val cameraWidthRatio = 2000f / previewSize.width
        val cameraHeightRatio = 2000f / previewSize.height

        /* Measure left and top rectangle point */
        val left = (cameraWidthRatio * rect.left - 1000).coerceIn(-1000f, 1000f - FOCUS_AREA_SIZE).toInt()
        val top = (cameraHeightRatio * rect.height() - 1000).coerceIn(-1000f, 1000f - FOCUS_AREA_SIZE).toInt()
        val right = min(left + FOCUS_AREA_SIZE, 1000)
        val bottom = min(top + FOCUS_AREA_SIZE, 1000)

        return listOf(Camera.Area(Rect(left, top, right, bottom), 1000))
    }

    fun findBestMatchingSize(referenceSize: Size, availableSizes: List<Size>): Size =
        availableSizes.find { it.aspectRatio == referenceSize.aspectRatio } ?: availableSizes.getOrNull(0) ?: Size.UNKNOWN

    private fun touchNotInPreview(
        rotatedTextureViewX: Int,
        rotatedTextureViewY: Int,
        scaledPreviewX: Float,
        scaledPreviewY: Float,
        x: Float,
        y: Float
    ): Boolean {

        val diffX = max(0f, (rotatedTextureViewX - scaledPreviewX) / 2)
        val diffY = max(0f, (rotatedTextureViewY - scaledPreviewY) / 2)
        return x < diffX || x > diffX + scaledPreviewX || y < diffY || y > diffY + scaledPreviewY
    }

    private fun calculateScale(activity: Activity, textureView: TextureView, config: CameraConfig): Triple<Float, Float, Float> {
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
                PreviewScale.MANUAL_FILL,
                PreviewScale.AUTO_FILL -> max(scaleX, scaleY)
                PreviewScale.MANUAL_FIT,
                PreviewScale.AUTO_FIT -> min(scaleX, scaleY)
                PreviewScale.MANUAL -> 1f
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

    private fun calculateFocusRect(
        activity: Activity,
        textureView: TextureView,
        config: CameraConfig,
        x: Float,
        y: Float
    ): Rect? {

        val (_, _, scale) = calculateScale(activity, textureView, config)
        val displayOrientation = calculateDisplayOrientation(activity, config)
        val previewSize = config.previewSize
        val textureViewSize = Size(textureView.width, textureView.height)
        /* Calculate real scaled preview size */
        val scaledPreviewX = previewSize.width * scale
        val scaledPreviewY = previewSize.height * scale

        /* Sync texture view orientation with camera orientation */
        val rotatedTextureViewX = if (displayOrientation % 180 == 0) textureViewSize.width else textureViewSize.height
        val rotatedTextureViewY = if (displayOrientation % 180 == 0) textureViewSize.height else textureViewSize.width

        /* Convert texture view x,y into camera x,y */
        val rotatedX = when (displayOrientation) {
            90 -> y
            180 -> textureViewSize.width - x
            270 -> textureViewSize.height - y
            else -> x
        }
        val rotatedY = when (displayOrientation) {
            90 -> textureViewSize.width - x
            180 -> textureViewSize.height - y
            270 -> x
            else -> y
        }

        if (touchNotInPreview(rotatedTextureViewX, rotatedTextureViewY, scaledPreviewX, scaledPreviewY, rotatedX, rotatedY)) {
            return null
        }

        /* Convert camera x,y into preview x,y that translates x,y if preview is not fullscreen or if is scaled outside of screen */
        val translatedPreviewX = rotatedX - max(0f, (rotatedTextureViewX - scaledPreviewX) / 2)
        val translatedPreviewY = rotatedY - max(0f, (rotatedTextureViewY - scaledPreviewY) / 2)

        val rectWidth = previewSize.width * 0.1f
        val rectHeight = previewSize.height * 0.1f
        val left = (translatedPreviewX / scale - rectWidth / 2).coerceIn(0f, previewSize.width - rectWidth).toInt()
        val top = (translatedPreviewY / scale - rectHeight / 2).coerceIn(0f, previewSize.height - rectHeight).toInt()
        val right = min(left + rectWidth.toInt(), previewSize.width - 1)
        val bottom = min(top + rectHeight.toInt(), previewSize.height - 1)

        return Rect(left, top, right, bottom)
    }
}