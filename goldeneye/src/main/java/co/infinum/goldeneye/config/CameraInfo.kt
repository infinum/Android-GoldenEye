package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.Facing

interface CameraInfo {
    val id: String
    val orientation: Int
    val facing: Facing
}