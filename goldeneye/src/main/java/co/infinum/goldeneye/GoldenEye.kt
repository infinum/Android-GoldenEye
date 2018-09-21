package co.infinum.goldeneye

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.support.annotation.RequiresPermission
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import java.io.File

interface GoldenEye {

    val availableCameras: List<CameraInfo>
    val config: CameraConfig

    @RequiresPermission(Manifest.permission.CAMERA)
    fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback)

    @RequiresPermission(Manifest.permission.CAMERA)
    fun open(textureView: TextureView, cameraInfo: CameraInfo, onSuccess: () -> Unit, onError: (Throwable) -> Unit)

    fun release()

    fun takePicture(callback: PictureCallback)
    fun takePicture(onPictureTaken: (Bitmap) -> Unit, onError: (Throwable) -> Unit, onShutter: (() -> Unit)? = null)

    fun startRecording(file: File, callback: VideoCallback)
    fun startRecording(file: File, onVideoRecorded: (File) -> Unit, onError: (Throwable) -> Unit)
    fun stopRecording()

    class Builder(private val activity: Activity) {

        private var logger: Logger? = null
        private var onZoomChangeCallback: OnZoomChangedCallback? = null
        private var onFocusChangeCallback: OnFocusChangedCallback? = null

        fun setLogger(logger: Logger) = apply { this.logger = logger }

        fun setOnZoomChangeCallback(onZoomChanged: (Int) -> Unit) = apply {
            this.onZoomChangeCallback = object : OnZoomChangedCallback {
                override fun onZoomChanged(zoom: Int) {
                    onZoomChanged(zoom)
                }
            }
        }

        fun setOnZoomChangeCallback(callback: OnZoomChangedCallback) = apply { this.onZoomChangeCallback = callback }

        fun setOnFocusChangeCallback(onFocusChanged: (Point) -> Unit) = apply {
            this.onFocusChangeCallback = object : OnFocusChangedCallback {
                override fun onFocusChanged(point: Point) {
                    onFocusChanged(point)
                }
            }
        }

        fun setOnFocusChangeCallback(callback: OnFocusChangedCallback) = apply { this.onFocusChangeCallback = callback }

        fun build(): GoldenEye {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                GoldenEye2Impl(activity, onZoomChangeCallback, onFocusChangeCallback, logger)
            } else {
                GoldenEye1Impl(activity, onZoomChangeCallback, onFocusChangeCallback, logger)
            }
        }
    }
}