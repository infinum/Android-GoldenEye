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
        private var onZoomChangedCallback: OnZoomChangedCallback? = null
        private var onFocusChangedCallback: OnFocusChangedCallback? = null

        fun setLogger(logger: Logger) = apply { this.logger = logger }

        fun setOnZoomChangedCallback(onZoomChanged: (Int) -> Unit) = apply {
            this.onZoomChangedCallback = object : OnZoomChangedCallback {
                override fun onZoomChanged(zoom: Int) {
                    onZoomChanged(zoom)
                }
            }
        }

        fun setOnZoomChangedCallback(callback: OnZoomChangedCallback) = apply { this.onZoomChangedCallback = callback }

        fun setOnFocusChangedCallback(onFocusChanged: (Point) -> Unit) = apply {
            this.onFocusChangedCallback = object : OnFocusChangedCallback {
                override fun onFocusChanged(point: Point) {
                    onFocusChanged(point)
                }
            }
        }

        fun setOnFocusChangedCallback(callback: OnFocusChangedCallback) = apply { this.onFocusChangedCallback = callback }

        fun build(): GoldenEye {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                GoldenEye2Impl(activity, onZoomChangedCallback, onFocusChangedCallback, logger)
            } else {
                GoldenEye1Impl(activity, onZoomChangedCallback, onFocusChangedCallback, logger)
            }
        }
    }
}