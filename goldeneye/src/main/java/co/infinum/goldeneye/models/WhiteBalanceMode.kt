@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.models

import android.annotation.TargetApi
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import co.infinum.goldeneye.IllegalEnumException

enum class WhiteBalanceMode {
    OFF,
    AUTO,
    INCANDESCENT,
    FLUORESCENT,
    WARM_FLUORESCENT,
    DAYLIGHT,
    CLOUDY_DAYLIGHT,
    TWILIGHT,
    SHADE,
    UNKNOWN;

    fun toCamera1() = when (this) {
        AUTO -> Camera.Parameters.WHITE_BALANCE_AUTO
        INCANDESCENT -> Camera.Parameters.WHITE_BALANCE_INCANDESCENT
        FLUORESCENT -> Camera.Parameters.WHITE_BALANCE_FLUORESCENT
        WARM_FLUORESCENT -> Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT
        DAYLIGHT -> Camera.Parameters.WHITE_BALANCE_DAYLIGHT
        CLOUDY_DAYLIGHT -> Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT
        TWILIGHT -> Camera.Parameters.WHITE_BALANCE_TWILIGHT
        SHADE -> Camera.Parameters.WHITE_BALANCE_SHADE
        else -> throw IllegalEnumException
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun toCamera2() = when (this) {
        OFF -> CameraCharacteristics.CONTROL_AWB_MODE_OFF
        AUTO -> CameraCharacteristics.CONTROL_AWB_MODE_AUTO
        INCANDESCENT -> CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT
        FLUORESCENT -> CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT
        WARM_FLUORESCENT -> CameraCharacteristics.CONTROL_AWB_MODE_WARM_FLUORESCENT
        DAYLIGHT -> CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT
        CLOUDY_DAYLIGHT -> CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
        TWILIGHT -> CameraCharacteristics.CONTROL_AWB_MODE_TWILIGHT
        SHADE -> CameraCharacteristics.CONTROL_AWB_MODE_SHADE
        else -> throw IllegalEnumException
    }

    companion object {
        fun fromCamera1(string: String?) = when (string) {
            Camera.Parameters.WHITE_BALANCE_AUTO -> AUTO
            Camera.Parameters.WHITE_BALANCE_INCANDESCENT -> INCANDESCENT
            Camera.Parameters.WHITE_BALANCE_FLUORESCENT -> FLUORESCENT
            Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT -> WARM_FLUORESCENT
            Camera.Parameters.WHITE_BALANCE_DAYLIGHT -> DAYLIGHT
            Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT -> CLOUDY_DAYLIGHT
            Camera.Parameters.WHITE_BALANCE_TWILIGHT -> TWILIGHT
            Camera.Parameters.WHITE_BALANCE_SHADE -> SHADE
            else -> UNKNOWN
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun fromCamera2(int: Int?) = when (int) {
            CameraCharacteristics.CONTROL_AWB_MODE_OFF -> OFF
            CameraCharacteristics.CONTROL_AWB_MODE_AUTO -> AUTO
            CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT -> INCANDESCENT
            CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT -> FLUORESCENT
            CameraCharacteristics.CONTROL_AWB_MODE_WARM_FLUORESCENT -> WARM_FLUORESCENT
            CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT -> DAYLIGHT
            CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> CLOUDY_DAYLIGHT
            CameraCharacteristics.CONTROL_AWB_MODE_TWILIGHT -> TWILIGHT
            CameraCharacteristics.CONTROL_AWB_MODE_SHADE -> SHADE
            else -> UNKNOWN
        }
    }
}