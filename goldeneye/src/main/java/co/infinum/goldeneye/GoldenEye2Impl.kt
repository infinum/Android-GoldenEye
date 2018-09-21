package co.infinum.goldeneye

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresApi
import android.support.annotation.RequiresPermission
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.camera2.*
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.gesture.GestureManager
import co.infinum.goldeneye.gesture.ZoomHandlerImpl
import co.infinum.goldeneye.gesture.camera2.FocusHandlerImpl
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.sessions.PictureSession
import co.infinum.goldeneye.sessions.SessionsManager
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class GoldenEye2Impl(
    private val activity: Activity,
    logger: Logger? = null
) : BaseGoldenEyeImpl() {
    companion object {

        val backgroundHandler: Handler by lazy {
            val backgroundThread = HandlerThread("camera")
            backgroundThread.start()
            Handler(backgroundThread.looper)
        }
    }

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
        try {
            release()
            when (state) {
                CameraState.CLOSED, CameraState.READY -> openCamera(cameraInfo, textureView, callback)
                CameraState.INITIALIZING -> lastCameraRequest = CameraRequest(cameraInfo, callback)
                CameraState.TAKING_PICTURE, CameraState.RECORDING -> throw CameraInUseException
            }
        } catch (t: Throwable) {
            callback.onError(t)
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @SuppressLint("MissingPermission")
    private fun openCamera(cameraInfo: CameraInfo, textureView: TextureView, callback: InitCallback) {
        cameraManager.openCamera(cameraInfo.id.toString(), object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice?) {
                if (lastCameraRequest != null) {
                    openLastRequestedCamera()
                    return
                }

                try {
                    initSessions(camera, textureView, cameraInfo)
                    initGestureManager(textureView, sessionsManager)
                    initConfigUpdateHandler(sessionsManager, textureView)
                    callback.onConfigReady()
                    textureView.onSurfaceUpdate(
                        onAvailable = { _ ->
                            sessionsManager?.startPreview()
                            state = CameraState.READY
                        },
                        onSizeChanged = { applyMatrixTransformation(it) }
                    )
                } catch (t: Throwable) {
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
                    LogDelegate.log("Camera disconnected")
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
        }, null)
    }

    @Throws(CameraFailedToOpenException::class)
    private fun initConfigUpdateHandler(sessionsSyncManager: SessionsManager?, textureView: TextureView?) {
        if (sessionsSyncManager == null || textureView == null) throw CameraFailedToOpenException

        this.configUpdateHandler = ConfigUpdateHandler(
            activity = activity,
            textureView = textureView,
            sessionsSyncManager = sessionsSyncManager,
            config = config
        )
    }

    @Throws(CameraFailedToOpenException::class)
    fun initSessions(camera: CameraDevice?, textureView: TextureView, cameraInfo: CameraInfo) {
        if (camera == null) throw CameraFailedToOpenException

        this.cameraDevice = camera
        this._config = _availableCameras.first { it.id == cameraInfo.id }
        this._config.characteristics = cameraManager.getCameraCharacteristics(cameraDevice?.id)
        val pictureSession = PictureSession(activity, config, camera)
        this.sessionsManager = SessionsManager(textureView, pictureSession)
    }

    @Throws(CameraFailedToOpenException::class)
    fun initGestureManager(textureView: TextureView?, sessionsManager: SessionsManager?) {
        if (textureView == null || sessionsManager == null) throw CameraFailedToOpenException

        val zoomHandler = ZoomHandlerImpl(
            activity = activity,
            config = config,
            onZoomChanged = {}
        )
        val focusHandler = FocusHandlerImpl(
            activity = activity,
            textureView = textureView,
            config = config,
            sessionsManager = sessionsManager,
            onFocusChanged = {}
        )
        this.gestureManager = GestureManager(activity, textureView, zoomHandler, focusHandler)
    }

    override fun release() {
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
            LogDelegate.log("Camera is not ready.")
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
        TODO("not implemented")
    }

    override fun stopRecording() {
        TODO("not implemented")
    }

    private fun initAvailableCameras() {
        cameraManager.cameraIdList?.mapNotNull { it.toIntOrNull() }?.forEach { id ->
            val info = cameraManager.getCameraCharacteristics(id.toString())
            val orientation = info[CameraCharacteristics.SENSOR_ORIENTATION]
            val facing =
                if (info[CameraCharacteristics.LENS_FACING] == CameraCharacteristics.LENS_FACING_FRONT) Facing.FRONT else Facing.BACK
            val bestResolution = info[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.getOutputSizes(ImageFormat.JPEG)
                ?.map { it.toInternalSize() }
                ?.sorted()
                ?.firstOrNull()
                ?: Size.UNKNOWN

            val cameraInfo = object : CameraInfo {
                override val id = id
                override val orientation = orientation
                override val facing = facing
                override val bestResolution = bestResolution
            }
            val cameraConfig = Camera2ConfigImpl(
                cameraInfo = cameraInfo,
                videoConfig = VideoConfigImpl(id, onConfigUpdateListener),
                featureConfig = FeatureConfigImpl(onConfigUpdateListener),
                sizeConfig = SizeConfigImpl(onConfigUpdateListener),
                zoomConfig = ZoomConfigImpl(onConfigUpdateListener)
            )
            cameraConfig.characteristics = info
            _availableCameras.add(cameraConfig)
        }
    }

    private fun applyMatrixTransformation(textureView: TextureView?) {
        textureView?.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
    }
}