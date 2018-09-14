package co.infinum.goldeneye.models

import co.infinum.goldeneye.InitCallback
import co.infinum.goldeneye.config.CameraInfo

internal class CameraRequest(
    val cameraInfo: CameraInfo,
    val callback: InitCallback
)