@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.models

import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.IllegalEnumException

enum class AntibandingMode {
    AUTO,
    HZ_50,
    HZ_60,
    OFF,
    UNKNOWN;

    fun toCamera1() = when (this) {
        AntibandingMode.AUTO -> Camera.Parameters.ANTIBANDING_AUTO
        AntibandingMode.HZ_50 -> Camera.Parameters.ANTIBANDING_50HZ
        AntibandingMode.HZ_60 -> Camera.Parameters.ANTIBANDING_60HZ
        AntibandingMode.OFF -> Camera.Parameters.ANTIBANDING_OFF
        else -> throw IllegalEnumException
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun toCamera2() = when (this) {
        AntibandingMode.AUTO -> CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_AUTO
        AntibandingMode.HZ_50 -> CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_50HZ
        AntibandingMode.HZ_60 -> CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_60HZ
        AntibandingMode.OFF -> CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_OFF
        else -> throw IllegalEnumException
    }

    companion object {
        fun fromCamera1(string: String?) = when (string) {
            Camera.Parameters.ANTIBANDING_AUTO -> AUTO
            Camera.Parameters.ANTIBANDING_50HZ -> HZ_50
            Camera.Parameters.ANTIBANDING_60HZ -> HZ_60
            Camera.Parameters.ANTIBANDING_OFF -> OFF
            else -> UNKNOWN
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun fromCamera2(int: Int?) = when (int) {
            CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_OFF -> OFF
            CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_50HZ -> HZ_50
            CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_60HZ -> HZ_60
            CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_AUTO -> AUTO
            else -> UNKNOWN
        }
    }
}