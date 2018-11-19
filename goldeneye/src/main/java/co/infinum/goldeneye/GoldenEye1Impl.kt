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
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.gesture.GestureManager
import co.infinum.goldeneye.gesture.ZoomHandlerImpl
import co.infinum.goldeneye.gesture.camera1.FocusHandlerImpl
import co.infinum.goldeneye.models.CameraApi
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

internal class GoldenEye1Impl @JvmOverloads constructor(
    private val activity: Activity,
    private val advancedFeaturesEnabled: Boolean,
    private val onZoomChangedCallback: OnZoomChangedCallback?,
    private val onFocusChangedCallback: OnFocusChangedCallback?,
    private val pictureTransformation: PictureTransformation?,
    logger: Logger? = null
) : BaseGoldenEye(CameraApi.CAMERA1) {

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
    override val config: CameraConfig?
        get() = if (isConfigAvailable) _config else null

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    override fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        Intrinsics.checkCameraPermission(activity)
        state = CameraState.INITIALIZING
        AsyncUtils.startBackgroundThread()
        try {
            releaseInternal()
            _config = _availableCameras.first { it.id == cameraInfo.id }
            openCamera(_config)
            state = CameraState.READY
            initGestureManager(camera, textureView)
            initRecorders(camera)
            initConfigUpdateHandler(camera, textureView)
            callback.onReady(_config)
            this.textureView = textureView
            textureView.onSurfaceUpdate(
                onAvailable = { startPreview(callback) },
                onSizeChanged = { startPreview() }
            )
        } catch (t: Throwable) {
            releaseInternal()
            callback.onError(t)
        }

    }

    override fun release() {
        releaseInternal()
        AsyncUtils.stopBackgroundThread()
    }

    private fun releaseInternal() {
        state = CameraState.CLOSED
        videoRecorder?.release()
        pictureRecorder?.release()
        gestureHandler?.release()
        gestureHandler = null
        videoRecorder = null
        pictureRecorder = null
        try {
            camera?.stopPreview()
            camera?.release()
        } catch (t: Throwable) {
            log("Failed to release camera.", t)
        } finally {
            camera = null
        }
    }

    override fun takePicture(callback: PictureCallback) {
        if (state != CameraState.ACTIVE) {
            callback.onError(CameraNotActiveException())
            return
        }

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
                state = CameraState.ACTIVE
                camera?.startPreview()
            }
        })
    }

    override fun startRecording(file: File, callback: VideoCallback) {
        if (state != CameraState.ACTIVE) {
            callback.onError(CameraNotActiveException())
            return
        }

        state = CameraState.RECORDING_VIDEO
        applyConfig()
        startPreview()
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
                open(textureView!!, _config, onError = {})
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
            camera = camera,
            config = _config,
            restartPreview = {
                textureView.onSurfaceUpdate(
                    onAvailable = { startPreview() },
                    onSizeChanged = { startPreview() }
                )
            }
        )
    }

    @Throws(CameraFailedToOpenException::class)
    private fun initGestureManager(camera: Camera?, textureView: TextureView?) {
        if (camera == null || textureView == null) throw CameraFailedToOpenException

        val zoomHandler = ZoomHandlerImpl(
            activity = activity,
            config = _config
        )
        val focusHandler = FocusHandlerImpl(
            activity = activity,
            camera = camera,
            textureView = textureView,
            config = _config,
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

    private fun startPreview(initCallback: InitCallback? = null) {
        try {
            ifNotNull(camera, textureView) { camera, textureView ->
                camera.apply {
                    stopPreview()
                    setPreviewTexture(textureView.surfaceTexture)
                    applyConfig()
                    textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, textureView, _config))
                    setDisplayOrientation(CameraUtils.calculateDisplayOrientation(activity, _config))
                    startPreview()
                }
                state = CameraState.ACTIVE
                initCallback?.onActive()
            }
        } catch (t: Throwable) {
            releaseInternal()
            initCallback?.onError(t)
        }
    }

    private fun applyConfig() {
        camera?.apply {
            parameters = parameters.apply {
                val pictureSize = _config.pictureSize
                setPictureSize(pictureSize.width, pictureSize.height)

                val previewSize = _config.previewSize
                setPreviewSize(previewSize.width, previewSize.height)

                jpegQuality = _config.pictureQuality

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && _config.isVideoStabilizationSupported) {
                    videoStabilization = _config.videoStabilizationEnabled
                }

                if (_config.supportedFocusModes.contains(_config.focusMode)) {
                    focusMode = _config.focusMode.toCamera1()
                }

                if (_config.supportedFlashModes.contains(_config.flashMode)) {
                    flashMode = _config.flashMode.toCamera1()
                }

                if (_config.supportedAntibandingModes.contains(_config.antibandingMode)) {
                    antibanding = _config.antibandingMode.toCamera1()
                }

                if (_config.supportedColorEffectModes.contains(_config.colorEffectMode)) {
                    colorEffect = _config.colorEffectMode.toCamera1()
                }

                if (_config.supportedWhiteBalanceModes.contains(_config.whiteBalanceMode)) {
                    whiteBalance = _config.whiteBalanceMode.toCamera1()
                }

                if (parameters.isZoomSupported) {
                    zoom = zoomRatios.indexOf(_config.zoom)
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
                    basicFeatureConfig = BasicFeatureConfigImpl(onConfigUpdateListener),
                    advancedFeatureConfig = AdvancedFeatureConfigImpl(advancedFeaturesEnabled, onConfigUpdateListener),
                    sizeConfig = SizeConfigImpl(cameraInfo, videoConfig, onConfigUpdateListener),
                    zoomConfig = ZoomConfigImpl(onConfigUpdateListener, onZoomChangedCallback)
                )
            )
        }
    }
}