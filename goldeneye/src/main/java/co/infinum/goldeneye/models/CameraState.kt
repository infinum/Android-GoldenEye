package co.infinum.goldeneye.models

internal enum class CameraState {
    CLOSED,
    INITIALIZING,
    READY,
    ACTIVE,
    TAKING_PICTURE,
    RECORDING_VIDEO
}