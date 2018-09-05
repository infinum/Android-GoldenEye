package co.infinum.goldeneye

import android.app.Activity
import android.graphics.Bitmap
import android.view.TextureView
import co.infinum.goldeneye.models.CameraInfo
import java.io.File

interface GoldenEye {

    val availableCameras: List<CameraInfo>
    val config: CameraConfig

    fun init(cameraInfo: CameraInfo, callback: InitCallback)
    fun init(cameraInfo: CameraInfo, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
    fun startPreview(textureView: TextureView)
    fun release()

    fun takePicture(callback: PictureCallback)
    fun takePicture(onPictureTaken: (Bitmap) -> Unit, onError: (Throwable) -> Unit, onShutter: (() -> Unit)? = null)

    fun startRecording(file: File, callback: VideoCallback)
    fun startRecording(file: File, onVideoRecorded: (File) -> Unit, onError: (Throwable) -> Unit)
    fun stopRecording()

    interface Logger {
        fun log(message: String)
        fun log(t: Throwable)
    }

    class Builder(private val activity: Activity) {

        private var logger: GoldenEye.Logger? = null
        private var onZoomChangeCallback: OnZoomChangeCallback? = null

        fun setLogger(logger: GoldenEye.Logger) = apply { this.logger = logger }
        fun setOnZoomChangeCallback(callback: OnZoomChangeCallback) = apply { this.onZoomChangeCallback = callback }
        fun build(): GoldenEye = GoldenEyeImpl(activity, logger, onZoomChangeCallback)
    }
}