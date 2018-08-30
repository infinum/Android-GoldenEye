package co.infinum.goldeneye.models

import android.hardware.Camera

enum class FlashMode constructor(
    val key: String
) {
    UNKNOWN("unknown"),
    OFF(Camera.Parameters.FLASH_MODE_OFF),
    ON(Camera.Parameters.FLASH_MODE_ON),
    AUTO(Camera.Parameters.FLASH_MODE_AUTO),
    TORCH(Camera.Parameters.FLASH_MODE_TORCH),
    RED_EYE(Camera.Parameters.FLASH_MODE_RED_EYE);

    companion object {
        fun fromString(key: String?) = values().find { it.key == key } ?: UNKNOWN
    }
}