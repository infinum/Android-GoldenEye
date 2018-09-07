package co.infinum.goldeneye

import android.app.Activity
import android.graphics.Bitmap
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.GoldenEyeConfig
import java.io.File

interface GoldenEye {

    val availableCameras: List<CameraInfo>
    val config: CameraConfig

    fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback)
    fun open(textureView: TextureView, cameraInfo: CameraInfo, onSuccess: () -> Unit, onError: (Throwable) -> Unit)
    fun release()

    fun takePicture(callback: PictureCallback)
    fun takePicture(onPictureTaken: (Bitmap) -> Unit, onError: (Throwable) -> Unit, onShutter: (() -> Unit)? = null)

    fun startRecording(file: File, callback: VideoCallback)
    fun startRecording(file: File, onVideoRecorded: (File) -> Unit, onError: (Throwable) -> Unit)
    fun stopRecording()

    class Builder(private val activity: Activity) {

        private var logger: Logger? = null
        private var onZoomChangeCallback: OnZoomChangeCallback? = null
        private var onFocusChangeCallback: OnFocusChangeCallback? = null
        private var goldenEyeConfig = GoldenEyeConfig.Builder().build()

        fun setLogger(logger: Logger) = apply { this.logger = logger }
        fun setOnZoomChangeCallback(callback: OnZoomChangeCallback) = apply { this.onZoomChangeCallback = callback }
        fun setOnFocusChangeCallback(callback: OnFocusChangeCallback) = apply { this.onFocusChangeCallback = callback }
        fun setConfig(config: GoldenEyeConfig) = apply { this.goldenEyeConfig = config }

        fun build(): GoldenEye = GoldenEyeImpl(activity, goldenEyeConfig, onZoomChangeCallback, onFocusChangeCallback, logger)
    }
}