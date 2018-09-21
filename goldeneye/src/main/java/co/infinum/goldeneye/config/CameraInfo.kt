package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.Size

interface CameraInfo {
    val id: String
    val orientation: Int
    val facing: Facing
    val bestResolution: Size
}