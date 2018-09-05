package co.infinum.goldeneye.models

import android.hardware.Camera

enum class SceneMode(
    val key: String
) {
    AUTO(Camera.Parameters.SCENE_MODE_AUTO),
    ACTION(Camera.Parameters.SCENE_MODE_ACTION),
    PORTRAIT(Camera.Parameters.SCENE_MODE_PORTRAIT),
    LANDSCAPE(Camera.Parameters.SCENE_MODE_LANDSCAPE),
    NIGHT(Camera.Parameters.SCENE_MODE_NIGHT),
    NIGHT_PORTRAIT(Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT),
    THEATRE(Camera.Parameters.SCENE_MODE_THEATRE),
    BEACH(Camera.Parameters.SCENE_MODE_BEACH),
    SNOW(Camera.Parameters.SCENE_MODE_SNOW),
    SUNSET(Camera.Parameters.SCENE_MODE_SUNSET),
    STEADYPHOTO(Camera.Parameters.SCENE_MODE_STEADYPHOTO),
    FIREWORKS(Camera.Parameters.SCENE_MODE_FIREWORKS),
    SPORTS(Camera.Parameters.SCENE_MODE_SPORTS),
    PARTY(Camera.Parameters.SCENE_MODE_PARTY),
    CANDLELIGHT(Camera.Parameters.SCENE_MODE_CANDLELIGHT),
    BARCODE(Camera.Parameters.SCENE_MODE_BARCODE),
    UNKNOWN("");

    companion object {
        fun fromString(string: String?) = values().find { it.key == string } ?: UNKNOWN
    }
}