package co.infinum.goldeneye.models

import android.hardware.Camera

enum class Antibanding(
    val key: String
) {
    AUTO(Camera.Parameters.ANTIBANDING_AUTO),
    HZ_50(Camera.Parameters.ANTIBANDING_50HZ),
    HZ_60(Camera.Parameters.ANTIBANDING_60HZ),
    OFF(Camera.Parameters.ANTIBANDING_OFF),
    UNKNOWN("");

    companion object {
        fun fromString(string: String?) = values().find { it.key == string } ?: UNKNOWN
    }
}