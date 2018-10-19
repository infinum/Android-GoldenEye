@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.models

import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.IllegalEnumException

/**
 * One of the advanced features. Use [co.infinum.goldeneye.GoldenEye.Builder.withAdvancedFeatures] method
 * to gain access to it.
 */
enum class ColorEffectMode {
    /**
     * @see Camera.Parameters.EFFECT_NONE
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_OFF
     */
    NONE,
    /**
     * @see Camera.Parameters.EFFECT_MONO
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_MONO
     */
    MONO,
    /**
     * @see Camera.Parameters.EFFECT_NEGATIVE
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_NEGATIVE
     */
    NEGATIVE,
    /**
     * @see Camera.Parameters.EFFECT_SOLARIZE
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_SOLARIZE
     */
    SOLARIZE,
    /**
     * @see Camera.Parameters.EFFECT_SEPIA
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_SEPIA
     */
    SEPIA,
    /**
     * @see Camera.Parameters.EFFECT_POSTERIZE
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_POSTERIZE
     */
    POSTERIZE,
    /**
     * @see Camera.Parameters.EFFECT_WHITEBOARD
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_WHITEBOARD
     */
    WHITEBOARD,
    /**
     * @see Camera.Parameters.EFFECT_BLACKBOARD
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_BLACKBOARD
     */
    BLACKBOARD,
    /**
     * @see Camera.Parameters.EFFECT_AQUA
     * @see CameraCharacteristics.CONTROL_EFFECT_MODE_AQUA
     */
    AQUA,
    UNKNOWN;

    fun toCamera1() = when (this) {
        NONE -> Camera.Parameters.EFFECT_NONE
        MONO -> Camera.Parameters.EFFECT_MONO
        NEGATIVE -> Camera.Parameters.EFFECT_NEGATIVE
        SOLARIZE -> Camera.Parameters.EFFECT_SOLARIZE
        SEPIA -> Camera.Parameters.EFFECT_SEPIA
        POSTERIZE -> Camera.Parameters.EFFECT_POSTERIZE
        WHITEBOARD -> Camera.Parameters.EFFECT_WHITEBOARD
        BLACKBOARD -> Camera.Parameters.EFFECT_BLACKBOARD
        AQUA -> Camera.Parameters.EFFECT_AQUA
        else -> throw IllegalEnumException
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun toCamera2() = when (this) {
        NONE -> CameraCharacteristics.CONTROL_EFFECT_MODE_OFF
        MONO -> CameraCharacteristics.CONTROL_EFFECT_MODE_MONO
        NEGATIVE -> CameraCharacteristics.CONTROL_EFFECT_MODE_NEGATIVE
        SOLARIZE -> CameraCharacteristics.CONTROL_EFFECT_MODE_SOLARIZE
        SEPIA -> CameraCharacteristics.CONTROL_EFFECT_MODE_SEPIA
        POSTERIZE -> CameraCharacteristics.CONTROL_EFFECT_MODE_POSTERIZE
        WHITEBOARD -> CameraCharacteristics.CONTROL_EFFECT_MODE_WHITEBOARD
        BLACKBOARD -> CameraCharacteristics.CONTROL_EFFECT_MODE_BLACKBOARD
        AQUA -> CameraCharacteristics.CONTROL_EFFECT_MODE_AQUA
        else -> throw IllegalEnumException
    }

    companion object {
        fun fromCamera1(string: String?) = when (string) {
            Camera.Parameters.EFFECT_NONE -> NONE
            Camera.Parameters.EFFECT_MONO -> MONO
            Camera.Parameters.EFFECT_NEGATIVE -> NEGATIVE
            Camera.Parameters.EFFECT_SOLARIZE -> SOLARIZE
            Camera.Parameters.EFFECT_SEPIA -> SEPIA
            Camera.Parameters.EFFECT_POSTERIZE -> POSTERIZE
            Camera.Parameters.EFFECT_WHITEBOARD -> WHITEBOARD
            Camera.Parameters.EFFECT_BLACKBOARD -> BLACKBOARD
            Camera.Parameters.EFFECT_AQUA -> AQUA
            else -> UNKNOWN
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun fromCamera2(int: Int?) = when (int) {
            CameraCharacteristics.CONTROL_EFFECT_MODE_OFF -> NONE
            CameraCharacteristics.CONTROL_EFFECT_MODE_MONO -> MONO
            CameraCharacteristics.CONTROL_EFFECT_MODE_NEGATIVE -> NEGATIVE
            CameraCharacteristics.CONTROL_EFFECT_MODE_SOLARIZE -> SOLARIZE
            CameraCharacteristics.CONTROL_EFFECT_MODE_SEPIA -> SEPIA
            CameraCharacteristics.CONTROL_EFFECT_MODE_POSTERIZE -> POSTERIZE
            CameraCharacteristics.CONTROL_EFFECT_MODE_WHITEBOARD -> WHITEBOARD
            CameraCharacteristics.CONTROL_EFFECT_MODE_BLACKBOARD -> BLACKBOARD
            CameraCharacteristics.CONTROL_EFFECT_MODE_AQUA -> AQUA
            else -> UNKNOWN
        }
    }
}