package co.infinum.goldeneye.camera2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.annotation.RequiresPermission
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.*
import co.infinum.goldeneye.camera1.config.CameraInfo
import co.infinum.goldeneye.camera2.config.*
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class GoldenEyeImpl(
    private val activity: Activity,
    logger: Logger? = null
) : BaseGoldenEyeImpl() {

    private val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private var cameraDevice: CameraDevice? = null
    private var lastCameraRequest: CameraRequest? = null
    private var textureView: TextureView? = null

    private val _availableCameras = mutableListOf<CameraConfigImpl>()
    override val availableCameras: List<CameraInfo> = _availableCameras

    private lateinit var _config: CameraConfigImpl
    override val config: CameraConfig
        get() = _config

    private val onUpdateListener: (CameraProperty) -> Unit = {
        //        when (it) {
        //            CameraProperty.FOCUS -> camera?.updateParams { focusMode = config.focusMode.key }
        //            CameraProperty.FLASH -> camera?.updateParams { flashMode = config.flashMode.key }
        //            CameraProperty.COLOR_EFFECT -> camera?.updateParams { colorEffect = config.colorEffect.key }
        //            CameraProperty.ANTIBANDING -> camera?.updateParams { antibanding = config.antibanding.key }
        //            CameraProperty.SCENE_MODE -> camera?.updateParams { sceneMode = config.sceneMode.key }
        //            CameraProperty.WHITE_BALANCE -> camera?.updateParams { whiteBalance = config.whiteBalance.key }
        //            CameraProperty.PICTURE_SIZE -> updatePictureSize()
        //            CameraProperty.PREVIEW_SIZE -> updatePreviewSize(config.pictureSize)
        //            CameraProperty.ZOOM -> camera?.updateParams { zoom = config.zoom.level }
        //            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization()
        //            CameraProperty.EXPOSURE_COMPENSATION -> camera?.updateParams { exposureCompensation = config.exposureCompensation }
        //            CameraProperty.PREVIEW_SCALE -> applyMatrixTransformation(textureView)
        //        }
    }

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    private fun openCamera(width: Int, height: Int) {

        //        camera = Camera.initInstance(manager).apply { // (2)
        //            setUpCameraOutputs(width, height, this) // (3)
        //            configureTransform(width, height) // (4)
        //            this.open() // (5) and (6)
        //            val texture = textureView.surfaceTexture
        //            texture.setDefaultBufferSize(previewSize.width, previewSize.height) // (7)
        //            this.start(Surface(texture)) // (8) and (9)
        //        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        Intrinsics.checkCameraPermission(activity)
        try {
            this.textureView = textureView
            when (state) {
                CameraState.CLOSED, CameraState.READY -> openCamera(cameraInfo, callback)
                CameraState.INITIALIZING -> lastCameraRequest = CameraRequest(cameraInfo, callback)
                CameraState.TAKING_PICTURE, CameraState.RECORDING -> throw CameraInUseException
            }
        } catch (t: Throwable) {
            callback.onError(t)
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @SuppressLint("MissingPermission")
    private fun openCamera(cameraInfo: CameraInfo, callback: InitCallback) {
        cameraManager.openCamera(cameraInfo.id.toString(), object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice?) {
                if (lastCameraRequest == null) {
                    cameraDevice = camera
                    _config = _availableCameras.first { it.id == cameraInfo.id }
                    _config.characteristics = cameraManager.getCameraCharacteristics(cameraDevice?.id)
                    startPreview()
                } else {
                    openLastRequestedCamera()
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

    private fun startPreview() {
        textureView?.onSurfaceUpdate(
            onAvailable = { view ->
                try {
                    view.setTransform(CameraUtils.calculateTextureMatrix(activity, config, view))
                    val texture = textureView?.surfaceTexture?.apply {
                        setDefaultBufferSize(config.previewSize.width, config.previewSize.height)
                    }
                    val surface = Surface(texture)
                    val previewRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
                        addTarget(surface)
                    }
                    cameraDevice?.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(previewRequestBuilder?.build(), null, null)
                            } catch (t: Throwable) {
                                LogDelegate.log(t)
                            }
                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        }
                    }, null
                    )
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            },
            onSizeChanged = { it.setTransform(CameraUtils.calculateTextureMatrix(activity, config, it)) }
        )
    }

    override fun release() {
        TODO("not implemented")
    }

    override fun takePicture(callback: PictureCallback) {
        TODO("not implemented")
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
            _availableCameras.add(
                CameraConfigImpl(
                    cameraInfo = cameraInfo,
                    videoConfig = VideoConfigImpl(id, onUpdateListener),
                    featureConfig = FeatureConfigImpl(onUpdateListener),
                    sizeConfig = SizeConfigImpl(onUpdateListener),
                    zoomConfig = ZoomConfigImpl(onUpdateListener)
                )
            )
        }
    }
}