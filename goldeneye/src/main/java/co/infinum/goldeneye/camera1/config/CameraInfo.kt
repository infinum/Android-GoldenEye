package co.infinum.goldeneye.camera1.config

import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.Size

interface CameraInfo {
    val id: Int
    val orientation: Int
    val facing: Facing
    val bestResolution: Size
}