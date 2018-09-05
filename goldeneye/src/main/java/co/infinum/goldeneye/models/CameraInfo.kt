package co.infinum.goldeneye.models

data class CameraInfo internal constructor(
    val id: Int,
    val orientation: Int,
    val facing: Facing
)