package co.infinum.goldeneye.models

import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.IllegalEnumException

enum class FlashMode {
    OFF,
    ON,
    AUTO,
    TORCH,
    RED_EYE,
    UNKNOWN;

    fun toCamera1() = when (this) {
        OFF -> Camera.Parameters.FLASH_MODE_OFF
        ON -> Camera.Parameters.FLASH_MODE_ON
        AUTO -> Camera.Parameters.FLASH_MODE_AUTO
        TORCH -> Camera.Parameters.FLASH_MODE_TORCH
        RED_EYE -> Camera.Parameters.FLASH_MODE_RED_EYE
        else -> throw IllegalEnumException
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun toCamera2() = when (this) {
        ON -> CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH
        AUTO -> CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH
        RED_EYE -> CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE
        OFF -> CameraCharacteristics.CONTROL_AE_MODE_ON
        else -> throw IllegalEnumException
    }

    companion object {
        fun fromCamera1(string: String?) = when (string) {
            Camera.Parameters.FLASH_MODE_OFF -> OFF
            Camera.Parameters.FLASH_MODE_ON -> ON
            Camera.Parameters.FLASH_MODE_AUTO -> AUTO
            Camera.Parameters.FLASH_MODE_TORCH -> TORCH
            Camera.Parameters.FLASH_MODE_RED_EYE -> RED_EYE
            else -> UNKNOWN
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun fromCamera2(int: Int?) = when (int) {
            CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH -> ON
            CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH -> AUTO
            CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE -> RED_EYE
            CameraCharacteristics.CONTROL_AE_MODE_ON -> OFF
            else -> UNKNOWN
        }
    }
}