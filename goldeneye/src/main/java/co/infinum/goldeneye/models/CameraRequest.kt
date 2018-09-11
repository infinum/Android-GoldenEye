package co.infinum.goldeneye.models

import android.view.TextureView
import co.infinum.goldeneye.InitCallback
import co.infinum.goldeneye.camera1.config.CameraInfo
import java.util.*

internal class CameraRequest(
    val cameraInfo: CameraInfo,
    val callback: InitCallback
)