package co.infinum.goldeneye

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.support.annotation.RequiresPermission
import android.view.TextureView
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.CameraConfig
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
        private var onZoomChangeCallback: OnZoomChangeCallback? = null
        private var onFocusChangeCallback: OnFocusChangeCallback? = null

        fun setLogger(logger: Logger) = apply { this.logger = logger }

//        fun setOnZoomChangeCallback(onZoomChanged: (Zoom) -> Unit) = apply {
//            this.onZoomChangeCallback = object : OnZoomChangeCallback {
//                override fun onZoomChanged(zoom: Zoom) {
//                    onZoomChanged(zoom)
//                }
//            }
//        }

        fun setOnZoomChangeCallback(callback: OnZoomChangeCallback) = apply { this.onZoomChangeCallback = callback }

        fun setOnFocusChangeCallback(onFocusChanged: (Point) -> Unit) = apply {
            this.onFocusChangeCallback = object : OnFocusChangeCallback {
                override fun onFocusChanged(point: Point) {
                    onFocusChanged(point)
                }
            }
        }

        fun setOnFocusChangeCallback(callback: OnFocusChangeCallback) = apply { this.onFocusChangeCallback = callback }

        fun build(): GoldenEye {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                GoldenEye2Impl(activity)
            } else {
                GoldenEye1Impl(activity, onZoomChangeCallback, onFocusChangeCallback, logger)
            }
        }
    }
}