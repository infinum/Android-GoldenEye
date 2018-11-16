@file:Suppress("unused")

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
import android.support.annotation.RequiresApi
import android.support.annotation.RequiresPermission
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.models.CameraApi
import co.infinum.goldeneye.utils.IncompatibleDevicesUtils
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
interface GoldenEye {

    /**
     * List of available cameras. List is available as soon as GoldenEye
     * instance is initialized.
     *
     * @see CameraInfo
     */
    val availableCameras: List<CameraInfo>

    /**
     * Currently opened camera configuration. Be sure to access it only after
     * [InitCallback.onReady] is received.
     *
     * If camera is in CLOSED or INITIALIZING state, null is returned.
     */
    val config: CameraConfig?

    /**
     * Asynchronously opens the camera.
     *
     * @param textureView that will display camera preview
     * @param cameraInfo of the camera that should be opened
     * @param callback used to notify whether camera successfully initialized
     *
     * @see InitCallback
     *
     * @throws MissingCameraPermissionException if camera permission is missing
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback)

    /**
     * @see open
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    fun open(textureView: TextureView, cameraInfo: CameraInfo,
        onReady: ((CameraConfig) -> Unit)? = null, onActive: (() -> Unit)? = null, onError: (Throwable) -> Unit
    )

    /**
     * Release resources when camera is not used anymore. It stops the camera
     * and all callbacks will be canceled.
     */
    fun release()

    /**
     * Asynchronously tries to take picture. If any error happens,
     * [PictureCallback.onError] is called.
     *
     * @see PictureCallback
     */
    fun takePicture(callback: PictureCallback)

    /**
     * @see takePicture
     */
    fun takePicture(onPictureTaken: (Bitmap) -> Unit, onError: (Throwable) -> Unit, onShutter: (() -> Unit)? = null)

    /**
     * Asynchronously tries to record video. If any error happens,
     * [VideoCallback.onError] is called.
     *
     * NOTE: Video recording is currently not supported for External sources!
     *
     * @param file which will be used to store the recording
     *
     * @see VideoCallback
     */
    fun startRecording(file: File, callback: VideoCallback)

    /**
     * @see startRecording
     */
    fun startRecording(file: File, onVideoRecorded: (File) -> Unit, onError: (Throwable) -> Unit)

    /**
     * Stops video recording.
     */
    fun stopRecording()

    class Builder(private val activity: Activity) {

        private var logger: Logger? = null
        private var onZoomChangedCallback: OnZoomChangedCallback? = null
        private var onFocusChangedCallback: OnFocusChangedCallback? = null
        private var pictureTransformation: PictureTransformation? = PictureTransformation.Default
        private var advancedFeaturesEnabled = false
        private var cameraApi: CameraApi? = null

        /**
         * @see Logger
         */
        fun setLogger(onMessage: (String) -> Unit, onThrowable: (Throwable) -> Unit) = apply {
            this.logger = object : Logger {
                override fun log(message: String) {
                    onMessage(message)
                }

                override fun log(t: Throwable) {
                    onThrowable(t)
                }
            }
        }

        /**
         * @see OnZoomChangedCallback
         */
        fun setOnZoomChangedCallback(onZoomChanged: (Int) -> Unit) = apply {
            this.onZoomChangedCallback = object : OnZoomChangedCallback {
                override fun onZoomChanged(zoom: Int) {
                    onZoomChanged(zoom)
                }
            }
        }

        /**
         * @see OnFocusChangedCallback
         */
        fun setOnFocusChangedCallback(onFocusChanged: (Point) -> Unit) = apply {
            this.onFocusChangedCallback = object : OnFocusChangedCallback {
                override fun onFocusChanged(point: Point) {
                    onFocusChanged(point)
                }
            }
        }

        /**
         * @see PictureTransformation
         */
        fun setPictureTransformation(transform: (Bitmap, CameraConfig, Float) -> Bitmap) = apply {
            this.pictureTransformation = object : PictureTransformation {
                override fun transform(picture: Bitmap, config: CameraConfig, orientationDifference: Float) =
                    transform(picture, config, orientationDifference)
            }
        }

        /**
         * @see Logger
         */
        fun setLogger(logger: Logger) = apply { this.logger = logger }

        /**
         * @see OnZoomChangedCallback
         */
        fun setOnZoomChangedCallback(callback: OnZoomChangedCallback) = apply { this.onZoomChangedCallback = callback }

        /**
         * @see OnFocusChangedCallback
         */
        fun setOnFocusChangedCallback(callback: OnFocusChangedCallback) = apply { this.onFocusChangedCallback = callback }

        /**
         * @see PictureTransformation
         */
        fun setPictureTransformation(transformation: PictureTransformation) = apply { this.pictureTransformation = transformation }

        /**
         * Enables the use of [co.infinum.goldeneye.config.AdvancedFeatureConfig] features. Those features
         * are experimental and disabled by default. If you try to change the value of any advanced feature,
         * it will be ignored unless you enable them.
         */
        fun withAdvancedFeatures() = apply { this.advancedFeaturesEnabled = true }

        /**
         * Manually select Camera API version.
         *
         * It might be useful to force Camera1 API if application does not use video recording feature
         * because it is more consistent when taking pictures with [co.infinum.goldeneye.models.FlashMode.ON].
         *
         * CAUTION: Video recording on newer Android devices can crash when using Camera1 API!
         *
         * @throws IllegalArgumentException when trying to force Camera2 API on devices older than Lollipop.
         */
        @Throws(IllegalArgumentException::class)
        fun setCameraApi(cameraApi: CameraApi): GoldenEye.Builder {
            if (cameraApi == CameraApi.CAMERA2 && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                throw IllegalArgumentException("Camera2 API is available from SDK 21.")
            }

            return apply { this.cameraApi = cameraApi }
        }

        /**
         * Behavior can be overridden by using [setCameraApi] method.
         *
         * @return true if GoldenEye will use Camera2 API, otherwise false.
         */
        fun shouldUseCamera2Api() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && isLegacyCamera().not()
                && IncompatibleDevicesUtils.isIncompatibleDevice(Build.MODEL).not()

        /**
         * Builds GoldenEye implementation. Builds Camera1 API wrapper for devices older than
         * LOLLIPOP and devices that use LEGACY camera, otherwise Camera2 API wrapper is built.
         */
        @SuppressLint("NewApi")
        fun build(): GoldenEye {
            val selectedCameraApi = this.cameraApi ?: (if (shouldUseCamera2Api()) CameraApi.CAMERA2 else CameraApi.CAMERA1)

            return when (selectedCameraApi) {
                CameraApi.CAMERA1 -> GoldenEye1Impl(
                    activity, advancedFeaturesEnabled, onZoomChangedCallback,
                    onFocusChangedCallback, pictureTransformation, logger
                )
                CameraApi.CAMERA2 -> GoldenEye2Impl(
                    activity, advancedFeaturesEnabled, onZoomChangedCallback,
                    onFocusChangedCallback, pictureTransformation, logger
                )
            }
        }

        /**
         * There were more issues than benefits when using Legacy camera with Camera2 API.
         * I found it to be working much better with deprecated Camera1 API instead.
         *
         * @see CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
         */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun isLegacyCamera(): Boolean {
            return try {
                val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                val characteristics = cameraManager?.cameraIdList?.map { cameraManager.getCameraCharacteristics(it) }
                val characteristic = characteristics?.firstOrNull {
                    it.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                } ?: characteristics?.get(0)
                val hardwareLevel = characteristic?.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
            } catch (t: Throwable) {
                false
            }
        }
    }
}