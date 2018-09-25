package co.infinum.goldeneye

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.annotation.RequiresPermission
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.camera2.*
import co.infinum.goldeneye.extensions.CameraApi
import co.infinum.goldeneye.extensions.MAIN_HANDLER
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.gesture.GestureManager
import co.infinum.goldeneye.gesture.ZoomHandlerImpl
import co.infinum.goldeneye.gesture.camera2.FocusHandlerImpl
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.sessions.PictureSession
import co.infinum.goldeneye.sessions.SessionsManager
import co.infinum.goldeneye.sessions.VideoSession
import co.infinum.goldeneye.utils.AsyncUtils
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class GoldenEye2Impl(
    private val activity: Activity,
    private val onZoomChangedCallback: OnZoomChangedCallback?,
    private val onFocusChangedCallback: OnFocusChangedCallback?,
    private val pictureTransformation: PictureTransformation,
    logger: Logger? = null
) : BaseGoldenEyeImpl(CameraApi.VERSION_2) {

    private val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var lastCameraRequest: CameraRequest? = null
    private var sessionsManager: SessionsManager? = null
    private var gestureManager: GestureManager? = null
    private var configUpdateHandler: ConfigUpdateHandler? = null
    private val onConfigUpdateListener: (CameraProperty) -> Unit = { configUpdateHandler?.onPropertyUpdated(it) }

    private val _availableCameras = mutableListOf<Camera2ConfigImpl>()
    override val availableCameras: List<CameraInfo> = _availableCameras

    private lateinit var _config: Camera2ConfigImpl
    override val config: CameraConfig
        get() = _config

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        Intrinsics.checkCameraPermission(activity)
        AsyncUtils.startBackgroundThread()
        try {
            releaseInternal()
            when (state) {
                CameraState.CLOSED, CameraState.READY -> openCamera(textureView, cameraInfo, callback)
                CameraState.INITIALIZING -> lastCameraRequest = CameraRequest(cameraInfo, callback)
                CameraState.TAKING_PICTURE, CameraState.RECORDING -> throw CameraInUseException
            }
        } catch (t: Throwable) {
            state = CameraState.CLOSED
            callback.onError(t)
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @SuppressLint("MissingPermission")
    private fun openCamera(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        state = CameraState.INITIALIZING
        cameraManager.openCamera(cameraInfo.id, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice?) {
                if (lastCameraRequest != null) {
                    openLastRequestedCamera()
                    return
                }

                try {
                    initSessions(camera, textureView, cameraInfo)
                    initGestureManager(textureView, sessionsManager)
                    initConfigUpdateHandler(sessionsManager, textureView)
                    MAIN_HANDLER.post {
                        callback.onConfigReady()
                        textureView.onSurfaceUpdate(
                            onAvailable = { _ ->
                                sessionsManager?.startPreview()
                                state = CameraState.READY
                            },
                            onSizeChanged = { applyMatrixTransformation(it) }
                        )
                    }
                } catch (t: Throwable) {
                    state = CameraState.CLOSED
                    callback.onError(t)
                }
            }

            fun openLastRequestedCamera() {
                ifNotNull(textureView, lastCameraRequest) { textureView, request ->
                    open(textureView, request.cameraInfo, request.callback)
                }
                lastCameraRequest = null
            }

            override fun onDisconnected(camera: CameraDevice?) {
                if (lastCameraRequest == null) {
                    release()
                } else {
                    openLastRequestedCamera()
                }
            }

            override fun onError(camera: CameraDevice?, error: Int) {
                if (lastCameraRequest == null) {
                    LogDelegate.log(Camera2Error.fromInt(error).message)
                } else {
                    openLastRequestedCamera()
                }
            }
        }, AsyncUtils.backgroundHandler)
    }

    @Throws(CameraFailedToOpenException::class)
    private fun initConfigUpdateHandler(sessionsSyncManager: SessionsManager?, textureView: TextureView?) {
        if (sessionsSyncManager == null || textureView == null) throw CameraFailedToOpenException

        this.configUpdateHandler = ConfigUpdateHandler(
            activity = activity,
            textureView = textureView,
            sessionsManager = sessionsSyncManager,
            config = _config
        )
    }

    @Throws(CameraFailedToOpenException::class)
    fun initSessions(camera: CameraDevice?, textureView: TextureView, cameraInfo: CameraInfo) {
        if (camera == null) throw CameraFailedToOpenException

        this.cameraDevice = camera
        this._config = _availableCameras.first { it.id == cameraInfo.id }
        this._config.characteristics = cameraManager.getCameraCharacteristics(cameraDevice?.id)
        val pictureSession = PictureSession(activity, camera, config, pictureTransformation)
        val videoSession = VideoSession(activity, camera, config)
        this.sessionsManager = SessionsManager(textureView, pictureSession, videoSession)
    }

    @Throws(CameraFailedToOpenException::class)
    fun initGestureManager(textureView: TextureView?, sessionsManager: SessionsManager?) {
        if (textureView == null || sessionsManager == null) throw CameraFailedToOpenException

        val zoomHandler = ZoomHandlerImpl(
            activity = activity,
            config = config,
            onZoomChanged = { onZoomChangedCallback?.onZoomChanged(it) }
        )
        val focusHandler = FocusHandlerImpl(
            activity = activity,
            textureView = textureView,
            config = config,
            sessionsManager = sessionsManager,
            onFocusChanged = { onFocusChangedCallback?.onFocusChanged(it) }
        )
        this.gestureManager = GestureManager(activity, textureView, zoomHandler, focusHandler)
    }

    override fun release() {
        releaseInternal()
        AsyncUtils.stopBackgroundThread()
    }

    private fun releaseInternal() {
        state = CameraState.CLOSED
        try {
            sessionsManager?.release()
            cameraDevice?.close()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            cameraDevice = null
            sessionsManager = null
        }
    }

    override fun takePicture(callback: PictureCallback) {
        if (state != CameraState.READY) {
            callback.onError(CameraNotReadyException())
            return
        }

        state = CameraState.TAKING_PICTURE
        sessionsManager?.takePicture(object : PictureCallback() {
            override fun onPictureTaken(picture: Bitmap) {
                state = CameraState.READY
                callback.onPictureTaken(picture)
            }

            override fun onError(t: Throwable) {
                state = CameraState.READY
                callback.onError(t)
            }

            override fun onShutter() {
                callback.onShutter()
            }
        })
    }

    override fun startRecording(file: File, callback: VideoCallback) {
        if (config.id.toIntOrNull() == null) {
            callback.onError(ExternalVideoRecordingNotSupportedException)
            return
        }

        if (BaseGoldenEyeImpl.state != CameraState.READY) {
            callback.onError(CameraNotReadyException())
            return
        }

        BaseGoldenEyeImpl.state = CameraState.RECORDING
        sessionsManager?.startRecording(file, object : VideoCallback {
            override fun onVideoRecorded(file: File) {
                BaseGoldenEyeImpl.state = CameraState.READY
                callback.onVideoRecorded(file)
            }

            override fun onError(t: Throwable) {
                BaseGoldenEyeImpl.state = CameraState.READY
                callback.onError(t)
            }
        })
    }

    override fun stopRecording() {
        sessionsManager?.stopRecording()
    }

    private fun initAvailableCameras() {
        cameraManager.cameraIdList?.forEach { id ->
            val info = cameraManager.getCameraCharacteristics(id)
            val orientation = info[CameraCharacteristics.SENSOR_ORIENTATION]
            val facing =
                if (info[CameraCharacteristics.LENS_FACING] == CameraCharacteristics.LENS_FACING_FRONT) Facing.FRONT else Facing.BACK

            val cameraInfo = object : CameraInfo {
                override val id = id
                override val orientation = orientation
                override val facing = facing
            }
            val videoConfig = VideoConfigImpl(id, onConfigUpdateListener)
            val cameraConfig = Camera2ConfigImpl(
                cameraInfo = cameraInfo,
                videoConfig = videoConfig,
                featureConfig = FeatureConfigImpl(onConfigUpdateListener),
                sizeConfig = SizeConfigImpl(cameraInfo, videoConfig, onConfigUpdateListener),
                zoomConfig = ZoomConfigImpl(onConfigUpdateListener)
            )
            CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE
            cameraConfig.characteristics = info
            _availableCameras.add(cameraConfig)
        }
    }

    private fun applyMatrixTransformation(textureView: TextureView?) {
        textureView?.setTransform(CameraUtils.calculateTextureMatrix(activity, textureView, config))
    }
}