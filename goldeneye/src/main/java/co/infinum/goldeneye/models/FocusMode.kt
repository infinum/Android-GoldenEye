@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.models

import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.IllegalEnumException

enum class FocusMode {
    /**
     * Auto-focus mode.
     */
    AUTO,
    /**
     * Focus is set at infinity.
     */
    INFINITY,
    /**
     * Close-up focusing mode.
     */
    MACRO,
    /**
     * Focus is fixed.
     */
    FIXED,
    /**
     * Extended depth of field.
     */
    EDOF,
    /**
     * Continuous auto focus mode intended for video recording.
     * It has smoother change than [CONTINUOUS_PICTURE].
     */
    CONTINUOUS_VIDEO,
    /**
     * Continuous auto focus mode intended for picture taking.
     * It is more aggressive than [CONTINUOUS_VIDEO].
     */
    CONTINUOUS_PICTURE,
    UNKNOWN;

    fun toCamera1() = when (this) {
        FocusMode.AUTO -> Camera.Parameters.FOCUS_MODE_AUTO
        FocusMode.INFINITY -> Camera.Parameters.FOCUS_MODE_INFINITY
        FocusMode.MACRO -> Camera.Parameters.FOCUS_MODE_MACRO
        FocusMode.FIXED -> Camera.Parameters.FOCUS_MODE_FIXED
        FocusMode.EDOF -> Camera.Parameters.FOCUS_MODE_EDOF
        FocusMode.CONTINUOUS_VIDEO -> Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
        FocusMode.CONTINUOUS_PICTURE -> Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        FocusMode.UNKNOWN -> throw IllegalEnumException
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun toCamera2() = when (this) {
        FocusMode.AUTO -> CameraCharacteristics.CONTROL_AF_MODE_AUTO
        FocusMode.MACRO -> CameraCharacteristics.CONTROL_AF_MODE_MACRO
        FocusMode.EDOF -> CameraCharacteristics.CONTROL_AF_MODE_EDOF
        FocusMode.CONTINUOUS_VIDEO -> CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO
        FocusMode.CONTINUOUS_PICTURE -> CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        else -> throw IllegalEnumException
    }

    companion object {
        fun fromCamera1(string: String?) = when (string) {
            Camera.Parameters.FOCUS_MODE_AUTO -> AUTO
            Camera.Parameters.FOCUS_MODE_INFINITY -> INFINITY
            Camera.Parameters.FOCUS_MODE_MACRO -> MACRO
            Camera.Parameters.FOCUS_MODE_FIXED -> FIXED
            Camera.Parameters.FOCUS_MODE_EDOF -> EDOF
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO -> CONTINUOUS_VIDEO
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE -> CONTINUOUS_PICTURE
            else -> UNKNOWN
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun fromCamera2(int: Int?) = when (int) {
            CameraCharacteristics.CONTROL_AF_MODE_AUTO -> AUTO
            CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> CONTINUOUS_PICTURE
            CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> CONTINUOUS_VIDEO
            CameraCharacteristics.CONTROL_AF_MODE_EDOF -> EDOF
            CameraCharacteristics.CONTROL_AF_MODE_MACRO -> MACRO
            else -> UNKNOWN
        }
    }
}