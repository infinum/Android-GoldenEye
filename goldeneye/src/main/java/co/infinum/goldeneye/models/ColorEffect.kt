package co.infinum.goldeneye.models

import android.hardware.Camera

enum class ColorEffect(
    val key: String
) {
    NONE(Camera.Parameters.EFFECT_NONE),
    MONO(Camera.Parameters.EFFECT_MONO),
    NEGATIVE(Camera.Parameters.EFFECT_NEGATIVE),
    SOLARIZE(Camera.Parameters.EFFECT_SOLARIZE),
    SEPIA(Camera.Parameters.EFFECT_SEPIA),
    POSTERIZE(Camera.Parameters.EFFECT_POSTERIZE),
    WHITEBOARD(Camera.Parameters.EFFECT_WHITEBOARD),
    BLACKBOARD(Camera.Parameters.EFFECT_BLACKBOARD),
    AQUA(Camera.Parameters.EFFECT_AQUA),
    UNKNOWN("");

    companion object {
        fun fromString(string: String?) = values().find { it.key == string } ?: UNKNOWN
    }
}