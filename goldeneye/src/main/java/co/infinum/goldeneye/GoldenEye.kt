package co.infinum.goldeneye

import android.graphics.Bitmap
import android.view.TextureView

interface GoldenEye {

    val currentCamera: CameraInfo
    val availableCameras: List<CameraInfo>
    val currentConfig: CameraConfig

    fun init(cameraInfo: CameraInfo, callback: InitCallback)
    fun start(textureView: TextureView)
    fun stop()

    fun takePicture(bitmap: Bitmap)

    interface Logger {
        fun log(message: String)
        fun log(t: Throwable)
    }
}