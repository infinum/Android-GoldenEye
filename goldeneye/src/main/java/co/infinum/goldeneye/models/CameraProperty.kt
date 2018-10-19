package co.infinum.goldeneye.models

/**
 * Defines CameraProperties that can be changed. Used in config
 * to let Config update handler know which property is updated.
 */
internal enum class CameraProperty {
    FOCUS,
    FLASH,
    PICTURE_SIZE,
    PREVIEW_SIZE,
    WHITE_BALANCE,
    ZOOM,
    VIDEO_STABILIZATION,
    COLOR_EFFECT,
    ANTIBANDING,
    PREVIEW_SCALE
}