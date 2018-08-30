package co.infinum.goldeneye

import android.app.Activity
import android.view.TextureView
import co.infinum.goldeneye.models.CameraInfo
import java.io.File

interface GoldenEye {

    val availableCameras: List<CameraInfo>
    val currentConfig: CameraConfig

    fun init(cameraInfo: CameraInfo, callback: InitCallback)
    fun startPreview(textureView: TextureView)
    fun stopPreview()

    fun takePicture(callback: PictureCallback)

    fun startRecording(file: File, callback: VideoCallback)
    fun stopRecording()

    interface Logger {
        fun log(message: String)
        fun log(t: Throwable)
    }

    class Builder(private val activity: Activity) {

        private var logger: GoldenEye.Logger? = null

        fun setLogger(logger: GoldenEye.Logger) = apply { this.logger = logger }
        fun build(): GoldenEye = GoldenEyeImpl(activity, logger)
    }
}