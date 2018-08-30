package co.infinum.goldeneye.models

import android.hardware.Camera

enum class FocusMode(
    val key: String
) {
    UNKNOWN("unknown"),
    AUTO(Camera.Parameters.FOCUS_MODE_AUTO),
    INFINITY(Camera.Parameters.FOCUS_MODE_INFINITY),
    MACRO(Camera.Parameters.FOCUS_MODE_MACRO),
    FIXED(Camera.Parameters.FOCUS_MODE_FIXED),
    EDOF(Camera.Parameters.FOCUS_MODE_EDOF),
    CONTINUOUS_VIDEO(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO),
    CONTINUOUS_PICTURE(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

    companion object {
        fun fromString(key: String?) = values().find { it.key == key } ?: UNKNOWN
    }
}