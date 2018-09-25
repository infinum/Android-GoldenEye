package co.infinum.goldeneye

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
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
        private var pictureTransformation: PictureTransformation? = null

        fun setOnZoomChangedCallback(onZoomChanged: (Int) -> Unit) = apply {
            this.onZoomChangedCallback = object : OnZoomChangedCallback {
                override fun onZoomChanged(zoom: Int) {
                    onZoomChanged(zoom)
                }
            }
        }

        fun setOnFocusChangedCallback(onFocusChanged: (Point) -> Unit) = apply {
            this.onFocusChangedCallback = object : OnFocusChangedCallback {
                override fun onFocusChanged(point: Point) {
                    onFocusChanged(point)
                }
            }
        }

        fun setPictureTransformation(transform: (Bitmap, CameraConfig, Float) -> Bitmap) = apply {
            this.pictureTransformation = object : PictureTransformation {
                override fun transform(picture: Bitmap, config: CameraConfig, orientationDifference: Float) =
                    transform(picture, config, orientationDifference)
            }
        }

        fun setLogger(logger: Logger) = apply { this.logger = logger }
        fun setOnZoomChangedCallback(callback: OnZoomChangedCallback) = apply { this.onZoomChangedCallback = callback }
        fun setOnFocusChangedCallback(callback: OnFocusChangedCallback) = apply { this.onFocusChangedCallback = callback }
        fun setPictureTransformation(transformation: PictureTransformation) = apply { this.pictureTransformation = transformation }

        @SuppressLint("NewApi")
        fun build(): GoldenEye {
            val pictureTransformationImpl = pictureTransformation ?: PictureTransformation.Default

            val useCamera2Api = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    val info = cameraManager.getCameraCharacteristics(cameraManager.cameraIdList.first())
                    val hardwareLevel = info.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    hardwareLevel != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                } catch (t: Throwable) {
                    false
                }
            } else {
                false
            }

            return if (useCamera2Api) {
                GoldenEye2Impl(activity, onZoomChangedCallback, onFocusChangedCallback, pictureTransformationImpl, logger)
            } else {
                GoldenEye1Impl(activity, onZoomChangedCallback, onFocusChangedCallback, pictureTransformationImpl, logger)
            }
        }
    }
}