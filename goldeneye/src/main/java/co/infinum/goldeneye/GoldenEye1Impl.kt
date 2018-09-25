@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Build
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.camera1.*
import co.infinum.goldeneye.extensions.CameraApi
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.gesture.GestureManager
import co.infinum.goldeneye.gesture.ZoomHandlerImpl
import co.infinum.goldeneye.gesture.camera1.FocusHandlerImpl
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.CameraState
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.recorders.PictureRecorder
import co.infinum.goldeneye.recorders.VideoRecorder
import co.infinum.goldeneye.utils.AsyncUtils
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import co.infinum.goldeneye.utils.LogDelegate.log
import java.io.File
import java.io.IOException

internal class GoldenEye1Impl @JvmOverloads constructor(
    private val activity: Activity,
    private val onZoomChangedCallback: OnZoomChangedCallback? = null,
    private val onFocusChangedCallback: OnFocusChangedCallback? = null,
    private val pictureTransformation: PictureTransformation,
    logger: Logger? = null
) : BaseGoldenEyeImpl(CameraApi.VERSION_1) {

    private var camera: Camera? = null
    private var textureView: TextureView? = null
    private var gestureHandler: GestureManager? = null
    private var videoRecorder: VideoRecorder? = null
    private var pictureRecorder: PictureRecorder? = null
    private var configUpdateHandler: ConfigUpdateHandler? = null
    private val onConfigUpdateListener: (CameraProperty) -> Unit = { configUpdateHandler?.onPropertyUpdated(it) }

    private val _availableCameras = mutableListOf<Camera1ConfigImpl>()
    override val availableCameras: List<CameraInfo> = _availableCameras

    private lateinit var _config: Camera1ConfigImpl
    override val config: CameraConfig
        get() = _config

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    override fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        Intrinsics.checkCameraPermission(activity)
        AsyncUtils.startBackgroundThread()
        try {
            releaseInternal()
            state = CameraState.INITIALIZING
            _config = _availableCameras.first { it.id == cameraInfo.id }
            openCamera(_config)
            initGestureManager(camera, textureView)
            initRecorders(camera)
            initConfigUpdateHandler(camera, textureView)
            callback.onConfigReady()
            this.textureView = textureView
            textureView.onSurfaceUpdate(
                onAvailable = {
                    state = CameraState.READY
                    startPreview()
                },
                onSizeChanged = { it.setTransform(CameraUtils.calculateTextureMatrix(activity, it, config)) }
            )
        } catch (t: Throwable) {
            state = CameraState.CLOSED
            camera = null
            callback.onError(t)
        }

    }

    override fun release() {
        releaseInternal()
        AsyncUtils.stopBackgroundThread()
    }

    private fun releaseInternal() {
        state = CameraState.CLOSED
        try {
            camera?.stopPreview()
            camera?.release()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            camera = null
            gestureHandler?.release()
            gestureHandler = null
            videoRecorder?.release()
            videoRecorder = null
            pictureRecorder?.release()
            pictureRecorder = null
        }
    }

    override fun takePicture(callback: PictureCallback) {
        if (state != CameraState.READY) return

        state = CameraState.TAKING_PICTURE
        pictureRecorder?.takePicture(object : PictureCallback() {
            override fun onPictureTaken(picture: Bitmap) {
                resetCameraPreview()
                callback.onPictureTaken(picture)
            }

            override fun onError(t: Throwable) {
                resetCameraPreview()
                callback.onError(t)
            }

            private fun resetCameraPreview() {
                state = CameraState.READY
                camera?.startPreview()
            }
        })
    }

    override fun startRecording(file: File, callback: VideoCallback) {
        if (state != CameraState.READY) {
            callback.onError(CameraNotReadyException())
            return
        }

        state = CameraState.RECORDING
        applyConfig()
        textureView?.let { it.setTransform(CameraUtils.calculateTextureMatrix(activity, it, config)) }
        camera?.unlock()
        videoRecorder?.startRecording(file, object : VideoCallback {
            override fun onVideoRecorded(file: File) {
                resetCameraPreview()
                callback.onVideoRecorded(file)
            }

            override fun onError(t: Throwable) {
                resetCameraPreview()
                callback.onError(t)
            }

            @SuppressLint("MissingPermission")
            private fun resetCameraPreview() {
                state = CameraState.CLOSED
                open(textureView!!, config, {}, {})
            }
        })
    }

    override fun stopRecording() {
        videoRecorder?.stopRecording()
    }

    @Throws(CameraFailedToOpenException::class)
    private fun initConfigUpdateHandler(camera: Camera?, textureView: TextureView?) {
        if (camera == null || textureView == null) throw CameraFailedToOpenException

        this.configUpdateHandler = ConfigUpdateHandler(
            activity = activity,
            camera = camera,
            textureView = textureView,
            config = config
        )
    }

    @Throws(CameraFailedToOpenException::class)
    private fun initGestureManager(camera: Camera?, textureView: TextureView?) {
        if (camera == null || textureView == null) throw CameraFailedToOpenException

        val zoomHandler = ZoomHandlerImpl(
            activity = activity,
            config = config,
            onZoomChanged = { onZoomChangedCallback?.onZoomChanged(it) }
        )
        val focusHandler = FocusHandlerImpl(
            activity = activity,
            camera = camera,
            textureView = textureView,
            config = config,
            onFocusChanged = { onFocusChangedCallback?.onFocusChanged(it) }
        )
        this.gestureHandler = GestureManager(
            activity = activity,
            textureView = textureView,
            zoomHandler = zoomHandler,
            focusHandler = focusHandler
        )
    }

    @Throws(CameraFailedToOpenException::class)
    private fun initRecorders(camera: Camera?) {
        if (camera == null) throw CameraFailedToOpenException

        this.videoRecorder = VideoRecorder(activity, camera, _config)
        this.pictureRecorder = PictureRecorder(activity, camera, _config, pictureTransformation)
    }

    @Throws(Throwable::class)
    private fun openCamera(config: Camera1ConfigImpl) {
        Camera.open(config.id.toInt())?.also {
            this.camera = it
            _config = config
            _config.characteristics = it.parameters
        }
    }

    private fun startPreview() {
        try {
            ifNotNull(camera, textureView) { camera, textureView ->
                camera.apply {
                    stopPreview()
                    setPreviewTexture(textureView.surfaceTexture)
                    applyConfig()
                    textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, textureView, config))
                    setDisplayOrientation(CameraUtils.calculateDisplayOrientation(activity, config))
                    startPreview()
                }
            }
        } catch (e: IOException) {
            state = CameraState.CLOSED
            log(e)
        }
    }

    private fun applyConfig() {
        camera?.apply {
            parameters = parameters.apply {
                val pictureSize = config.pictureSize
                setPictureSize(pictureSize.width, pictureSize.height)

                val previewSize = config.previewSize
                setPreviewSize(previewSize.width, previewSize.height)

                if (config.isVideoStabilizationSupported
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                ) {
                    videoStabilization = config.videoStabilizationEnabled
                }

                if (config.supportedFocusModes.contains(config.focusMode)) {
                    focusMode = config.focusMode.toCamera1()
                }

                if (config.supportedFlashModes.contains(config.flashMode)) {
                    flashMode = config.flashMode.toCamera1()
                }

                if (config.supportedAntibandingModes.contains(config.antibandingMode)) {
                    antibanding = config.antibandingMode.toCamera1()
                }

                if (config.supportedColorEffectModes.contains(config.colorEffectMode)) {
                    colorEffect = config.colorEffectMode.toCamera1()
                }

                if (config.supportedWhiteBalanceModes.contains(config.whiteBalanceMode)) {
                    whiteBalance = config.whiteBalanceMode.toCamera1()
                }

                if (parameters.isZoomSupported) {
                    zoom = zoomRatios.indexOf(config.zoom)
                }
            }
        }
    }

    private fun initAvailableCameras() {
        for (id in 0 until Camera.getNumberOfCameras()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(id, info)
            val facing = if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) Facing.BACK else Facing.FRONT

            val cameraInfo = object : CameraInfo {
                override val id = id.toString()
                override val orientation = info.orientation
                override val facing = facing
            }

            val videoConfig = VideoConfigImpl(id.toString(), onConfigUpdateListener)
            _availableCameras.add(
                Camera1ConfigImpl(
                    cameraInfo = cameraInfo,
                    videoConfig = videoConfig,
                    featureConfig = FeatureConfigImpl(onConfigUpdateListener),
                    sizeConfig = SizeConfigImpl(cameraInfo, videoConfig, onConfigUpdateListener),
                    zoomConfig = ZoomConfigImpl(onConfigUpdateListener)
                )
            )
        }
    }
}