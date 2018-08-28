package co.infinum.goldeneye

import android.hardware.Camera

data class Size internal constructor(
    val width: Int,
    val height: Int
) : Comparable<Size> {
    companion object {
        val UNKNOWN = Size(0, 0)
    }

    val aspectRatio = width.toFloat() / height.toFloat()

    override fun compareTo(other: Size): Int {
        return other.height * other.width - width * height
    }
}

data class CameraInfo internal constructor(
    val id: Int,
    val orientation: Int,
    val facing: Facing
)

enum class PreviewScale {
    FIT, FILL
}

enum class PreviewType {
    PICTURE, VIDEO
}

enum class Facing {
    BACK, FRONT
}

enum class FlashMode constructor(
    val key: String
) {
    OFF(Camera.Parameters.FLASH_MODE_OFF),
    ON(Camera.Parameters.FLASH_MODE_ON),
    AUTO(Camera.Parameters.FLASH_MODE_AUTO),
    TORCH(Camera.Parameters.FLASH_MODE_TORCH),
    RED_EYE(Camera.Parameters.FLASH_MODE_RED_EYE);

    companion object {
        fun fromString(key: String?) = values().find { it.key == key } ?: OFF
    }
}

enum class FocusMode(
    val key: String
) {
    AUTO(Camera.Parameters.FOCUS_MODE_AUTO),
    INFINITY(Camera.Parameters.FOCUS_MODE_INFINITY),
    MACRO(Camera.Parameters.FOCUS_MODE_MACRO),
    FIXED(Camera.Parameters.FOCUS_MODE_FIXED),
    EDOF(Camera.Parameters.FOCUS_MODE_EDOF),
    CONTINUOUS_VIDEO(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO),
    CONTINUOUS_PICTURE(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

    companion object {
        fun fromString(key: String?) = values().find { it.key == key } ?: AUTO
    }
}