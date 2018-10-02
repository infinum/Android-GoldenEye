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
import co.infinum.goldeneye.extensions.MAIN_HANDLER
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.gesture.GestureManager
import co.infinum.goldeneye.gesture.ZoomHandlerImpl
import co.infinum.goldeneye.gesture.camera2.FocusHandlerImpl
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.sessions.PictureSession
import co.infinum.goldeneye.sessions.SessionsManager
import co.infinum.goldeneye.sessions.VideoSession
import co.infinum.goldeneye.utils.AsyncUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class GoldenEye2Impl(
    private val activity: Activity,
    private val onZoomChangedCallback: OnZoomChangedCallback?,
    private val onFocusChangedCallback: OnFocusChangedCallback?,
    private val pictureTransformation: PictureTransformation?,
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
    override val config: CameraConfig?
        get() = if (isConfigAvailable) _config else null

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        Intrinsics.checkCameraPermission(activity)
        if (state == CameraState.INITIALIZING) {
            lastCameraRequest = CameraRequest(cameraInfo, callback)
            return
        }

        state = CameraState.INITIALIZING
        AsyncUtils.startBackgroundThread()
        try {
            releaseInternal()
            openCamera(textureView, cameraInfo, callback)
        } catch (t: Throwable) {
            releaseInternal()
            callback.onError(t)
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        cameraManager.openCamera(cameraInfo.id, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice?) {
                if (lastCameraRequest != null) {
                    openLastRequestedCamera(lastCameraRequest!!)
                    return
                }
                initInternal(camera, textureView, cameraInfo, callback)
            }

            fun openLastRequestedCamera(request: CameraRequest) {
                releaseInternal()
                lastCameraRequest = null
                open(textureView, request.cameraInfo, request.callback)
            }

            override fun onDisconnected(camera: CameraDevice?) {
                releaseInternal()
            }

            override fun onError(camera: CameraDevice?, error: Int) {
                val currentState = state
                if (lastCameraRequest == null) {
                    releaseInternal()
                    LogDelegate.log(Camera2Error.fromInt(error).message)
                    if (currentState == CameraState.INITIALIZING) {
                        MAIN_HANDLER.post { callback.onError(CameraFailedToOpenException) }
                    }
                } else {
                    openLastRequestedCamera(lastCameraRequest!!)
                }
            }
        }, AsyncUtils.backgroundHandler)
    }

    private fun initInternal(camera: CameraDevice?, textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        try {
            state = CameraState.READY
            initSessions(camera, textureView, cameraInfo)
            initGestureManager(textureView, sessionsManager)
            initConfigUpdateHandler(sessionsManager, textureView)
            MAIN_HANDLER.post {
                callback.onReady(_config)
                startPreview(textureView, callback)
            }
        } catch (t: Throwable) {
            releaseInternal()
            MAIN_HANDLER.post { callback.onError(t) }
        }
    }

    private fun startPreview(textureView: TextureView, callback: InitCallback) {
        textureView.onSurfaceUpdate(
            onAvailable = {
                sessionsManager?.startPreview(object : InitCallback() {
                    override fun onActive() {
                        state = CameraState.ACTIVE
                        callback.onActive()
                    }

                    override fun onError(t: Throwable) {
                        releaseInternal()
                        callback.onError(t)
                    }
                })
            },
            onSizeChanged = { sessionsManager?.restartSession() }
        )
    }

    @Throws(CameraFailedToOpenException::class)
    private fun initConfigUpdateHandler(sessionsSyncManager: SessionsManager?, textureView: TextureView?) {
        if (sessionsSyncManager == null || textureView == null) throw CameraFailedToOpenException

        this.configUpdateHandler = ConfigUpdateHandler(
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
        val pictureSession = PictureSession(activity, camera, _config, pictureTransformation)
        val videoSession = VideoSession(activity, camera, _config)
        this.sessionsManager = SessionsManager(textureView, pictureSession, videoSession)
    }

    @Throws(CameraFailedToOpenException::class)
    fun initGestureManager(textureView: TextureView?, sessionsManager: SessionsManager?) {
        if (textureView == null || sessionsManager == null) throw CameraFailedToOpenException

        val zoomHandler = ZoomHandlerImpl(
            activity = activity,
            config = _config,
            onZoomChanged = { onZoomChangedCallback?.onZoomChanged(it) }
        )
        val focusHandler = FocusHandlerImpl(
            activity = activity,
            textureView = textureView,
            config = _config,
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
            lastCameraRequest = null
            cameraDevice = null
            sessionsManager = null
            gestureManager?.release()
            gestureManager = null
            configUpdateHandler = null
        }
    }

    override fun takePicture(callback: PictureCallback) {
        if (state != CameraState.ACTIVE) {
            callback.onError(CameraNotActiveException())
            return
        }

        state = CameraState.TAKING_PICTURE
        sessionsManager?.takePicture(object : PictureCallback() {
            override fun onPictureTaken(picture: Bitmap) {
                state = CameraState.ACTIVE
                callback.onPictureTaken(picture)
            }

            override fun onError(t: Throwable) {
                state = CameraState.ACTIVE
                callback.onError(t)
            }

            override fun onShutter() {
                callback.onShutter()
            }
        })
    }

    override fun startRecording(file: File, callback: VideoCallback) {
        if (_config.facing == Facing.EXTERNAL) {
            callback.onError(ExternalVideoRecordingNotSupportedException)
            return
        }

        if (state != CameraState.ACTIVE) {
            callback.onError(CameraNotActiveException())
            return
        }

        state = CameraState.RECORDING_VIDEO
        sessionsManager?.startRecording(file, object : VideoCallback {
            override fun onVideoRecorded(file: File) {
                state = CameraState.ACTIVE
                callback.onVideoRecorded(file)
            }

            override fun onError(t: Throwable) {
                state = CameraState.ACTIVE
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
            val facing = when (info[CameraCharacteristics.LENS_FACING]) {
                CameraCharacteristics.LENS_FACING_FRONT -> Facing.FRONT
                CameraCharacteristics.LENS_FACING_EXTERNAL -> Facing.EXTERNAL
                else -> Facing.BACK
            }
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
            cameraConfig.characteristics = info
            _availableCameras.add(cameraConfig)
        }
    }
}