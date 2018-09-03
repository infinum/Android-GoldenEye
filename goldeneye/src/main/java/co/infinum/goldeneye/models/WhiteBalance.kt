package co.infinum.goldeneye.models

import android.hardware.Camera

enum class WhiteBalance(
    val key: String
) {
    AUTO(Camera.Parameters.WHITE_BALANCE_AUTO),
    INCANDESCENT(Camera.Parameters.WHITE_BALANCE_INCANDESCENT),
    FLUORESCENT(Camera.Parameters.WHITE_BALANCE_FLUORESCENT),
    WARM_FLUORESCENT(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT),
    DAYLIGHT(Camera.Parameters.WHITE_BALANCE_DAYLIGHT),
    CLOUDY_DAYLIGHT(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT),
    TWILIGHT(Camera.Parameters.WHITE_BALANCE_TWILIGHT),
    SHADE(Camera.Parameters.WHITE_BALANCE_SHADE),
    UNKNOWN("");

    companion object {
        fun fromString(key: String?) = values().find { it.key == key } ?: UNKNOWN
    }
}