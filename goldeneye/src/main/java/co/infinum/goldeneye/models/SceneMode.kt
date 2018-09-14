@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.models

import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.IllegalEnumException

enum class SceneMode {
    OFF,
    FACE_PRIORITY,
    AUTO,
    ACTION,
    PORTRAIT,
    LANDSCAPE,
    NIGHT,
    NIGHT_PORTRAIT,
    THEATRE,
    BEACH,
    SNOW,
    SUNSET,
    STEADYPHOTO,
    FIREWORKS,
    SPORTS,
    PARTY,
    CANDLELIGHT,
    BARCODE,
    HDR,
    UNKNOWN;

    fun toCamera1() = when (this) {
        AUTO -> Camera.Parameters.SCENE_MODE_AUTO
        ACTION -> Camera.Parameters.SCENE_MODE_ACTION
        PORTRAIT -> Camera.Parameters.SCENE_MODE_PORTRAIT
        LANDSCAPE -> Camera.Parameters.SCENE_MODE_LANDSCAPE
        NIGHT -> Camera.Parameters.SCENE_MODE_NIGHT
        NIGHT_PORTRAIT -> Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT
        THEATRE -> Camera.Parameters.SCENE_MODE_THEATRE
        BEACH -> Camera.Parameters.SCENE_MODE_BEACH
        SNOW -> Camera.Parameters.SCENE_MODE_SNOW
        SUNSET -> Camera.Parameters.SCENE_MODE_SUNSET
        STEADYPHOTO -> Camera.Parameters.SCENE_MODE_STEADYPHOTO
        FIREWORKS -> Camera.Parameters.SCENE_MODE_FIREWORKS
        SPORTS -> Camera.Parameters.SCENE_MODE_SPORTS
        PARTY -> Camera.Parameters.SCENE_MODE_PARTY
        CANDLELIGHT -> Camera.Parameters.SCENE_MODE_CANDLELIGHT
        BARCODE -> Camera.Parameters.SCENE_MODE_BARCODE
        else -> throw IllegalEnumException
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun toCamera2() = when (this) {
        OFF -> CameraCharacteristics.CONTROL_SCENE_MODE_DISABLED
        FACE_PRIORITY -> CameraCharacteristics.CONTROL_SCENE_MODE_FACE_PRIORITY
        ACTION -> CameraCharacteristics.CONTROL_SCENE_MODE_ACTION
        PORTRAIT -> CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT
        LANDSCAPE -> CameraCharacteristics.CONTROL_SCENE_MODE_LANDSCAPE
        NIGHT -> CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT
        NIGHT_PORTRAIT -> CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT_PORTRAIT
        THEATRE -> CameraCharacteristics.CONTROL_SCENE_MODE_THEATRE
        BEACH -> CameraCharacteristics.CONTROL_SCENE_MODE_BEACH
        SNOW -> CameraCharacteristics.CONTROL_SCENE_MODE_SNOW
        SUNSET -> CameraCharacteristics.CONTROL_SCENE_MODE_SUNSET
        STEADYPHOTO -> CameraCharacteristics.CONTROL_SCENE_MODE_STEADYPHOTO
        FIREWORKS -> CameraCharacteristics.CONTROL_SCENE_MODE_FIREWORKS
        SPORTS -> CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS
        PARTY -> CameraCharacteristics.CONTROL_SCENE_MODE_PARTY
        CANDLELIGHT -> CameraCharacteristics.CONTROL_SCENE_MODE_CANDLELIGHT
        BARCODE -> CameraCharacteristics.CONTROL_SCENE_MODE_BARCODE
        HDR -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) CameraCharacteristics.CONTROL_SCENE_MODE_HDR else throw IllegalEnumException
        else -> throw IllegalEnumException
    }

    companion object {
        fun fromCamera1(string: String?) = when (string) {
            Camera.Parameters.SCENE_MODE_AUTO -> AUTO
            Camera.Parameters.SCENE_MODE_ACTION -> ACTION
            Camera.Parameters.SCENE_MODE_PORTRAIT -> SceneMode.PORTRAIT
            Camera.Parameters.SCENE_MODE_LANDSCAPE -> SceneMode.LANDSCAPE
            Camera.Parameters.SCENE_MODE_NIGHT -> SceneMode.NIGHT
            Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT -> SceneMode.NIGHT
            Camera.Parameters.SCENE_MODE_THEATRE -> SceneMode.THEATRE
            Camera.Parameters.SCENE_MODE_BEACH -> SceneMode.BEACH
            Camera.Parameters.SCENE_MODE_SNOW -> SNOW
            Camera.Parameters.SCENE_MODE_SUNSET -> SUNSET
            Camera.Parameters.SCENE_MODE_STEADYPHOTO -> STEADYPHOTO
            Camera.Parameters.SCENE_MODE_FIREWORKS -> FIREWORKS
            Camera.Parameters.SCENE_MODE_SPORTS -> SPORTS
            Camera.Parameters.SCENE_MODE_PARTY -> PARTY
            Camera.Parameters.SCENE_MODE_CANDLELIGHT -> CANDLELIGHT
            Camera.Parameters.SCENE_MODE_BARCODE -> BARCODE
            else -> UNKNOWN
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun fromCamera2(int: Int?) = when (int) {
            CameraCharacteristics.CONTROL_SCENE_MODE_DISABLED -> OFF
            CameraCharacteristics.CONTROL_SCENE_MODE_FACE_PRIORITY -> FACE_PRIORITY
            CameraCharacteristics.CONTROL_SCENE_MODE_ACTION -> ACTION
            CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT -> PORTRAIT
            CameraCharacteristics.CONTROL_SCENE_MODE_LANDSCAPE -> LANDSCAPE
            CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT -> NIGHT
            CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT_PORTRAIT -> NIGHT_PORTRAIT
            CameraCharacteristics.CONTROL_SCENE_MODE_THEATRE -> THEATRE
            CameraCharacteristics.CONTROL_SCENE_MODE_BEACH -> BEACH
            CameraCharacteristics.CONTROL_SCENE_MODE_SNOW -> SNOW
            CameraCharacteristics.CONTROL_SCENE_MODE_SUNSET -> SUNSET
            CameraCharacteristics.CONTROL_SCENE_MODE_STEADYPHOTO -> STEADYPHOTO
            CameraCharacteristics.CONTROL_SCENE_MODE_FIREWORKS -> FIREWORKS
            CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS -> SPORTS
            CameraCharacteristics.CONTROL_SCENE_MODE_PARTY -> PARTY
            CameraCharacteristics.CONTROL_SCENE_MODE_CANDLELIGHT -> CANDLELIGHT
            CameraCharacteristics.CONTROL_SCENE_MODE_BARCODE -> BARCODE
            CameraCharacteristics.CONTROL_SCENE_MODE_HDR -> HDR
            else -> UNKNOWN
        }
    }
}