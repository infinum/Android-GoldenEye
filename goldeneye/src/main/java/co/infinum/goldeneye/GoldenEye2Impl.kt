package co.infinum.goldeneye

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.annotation.RequiresPermission
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.camera2.*
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class GoldenEye2Impl(
    private val activity: Activity,
    logger: Logger? = null
) : BaseGoldenEyeImpl() {

    private val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private var cameraDevice: CameraDevice? = null
    private var lastCameraRequest: CameraRequest? = null
    private var textureView: TextureView? = null
    private var devicePreview: DevicePreview? = null

    private val _availableCameras = mutableListOf<Camera2ConfigImpl>()
    override val availableCameras: List<CameraInfo> = _availableCameras

    private lateinit var _config: Camera2ConfigImpl
    override val config: CameraConfig
        get() = _config

    private val onUpdateListener: (CameraProperty) -> Unit = {
        when (it) {
            CameraProperty.FOCUS -> devicePreview?.updateRequest {
                set(CaptureRequest.CONTROL_AF_MODE, config.focusMode.toCamera2())
            }
            CameraProperty.FLASH -> devicePreview?.updateRequest {
                updateFlashMode(this, config.flashMode)
            }
            CameraProperty.COLOR_EFFECT -> devicePreview?.updateRequest {
                set(CaptureRequest.CONTROL_EFFECT_MODE, config.colorEffect.toCamera2())
            }
            CameraProperty.ANTIBANDING -> devicePreview?.updateRequest {
                set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, config.antibanding.toCamera2())
            }
            CameraProperty.SCENE_MODE -> devicePreview?.updateRequest {
                set(CaptureRequest.CONTROL_SCENE_MODE, config.sceneMode.toCamera2())
            }
            CameraProperty.WHITE_BALANCE -> devicePreview?.updateRequest {
                set(CaptureRequest.CONTROL_AWB_MODE, config.whiteBalance.toCamera2())
            }
            CameraProperty.PICTURE_SIZE -> updatePictureSize(config.pictureSize)
            CameraProperty.PREVIEW_SIZE -> startPreview()
            CameraProperty.ZOOM -> devicePreview?.updateRequest { updateZoom(this, config.zoom) }
            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization()
            CameraProperty.PREVIEW_SCALE -> applyMatrixTransformation(textureView)
        }
    }

    private fun updateVideoStabilization() {
        devicePreview?.updateRequest {
            val videoStabilizationMode = if (config.videoStabilizationEnabled) {
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
            } else {
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
            }
            set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, videoStabilizationMode)
        }
    }

    private fun updateZoom(requestBuilder: CaptureRequest.Builder, zoom: Int) {
        val zoomPercentage = zoom / 100f
        val zoomedWidth = (config.previewSize.width / zoomPercentage).toInt()
        val zoomedHeight = (config.previewSize.height / zoomPercentage).toInt()
        val halfWidthDiff = (config.previewSize.width - zoomedWidth) / 2
        val halfHeightDiff = (config.previewSize.height - zoomedHeight) / 2
        val zoomedRect = Rect(
            halfWidthDiff,
            halfHeightDiff,
            config.previewSize.width - halfWidthDiff,
            config.previewSize.height - halfHeightDiff
        )
        requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomedRect)
    }

    private fun updatePictureSize(size: Size) {
        //TODO update image reader with size
    }

    private fun updateFlashMode(requestBuilder: CaptureRequest.Builder, flashMode: FlashMode) {
        if (flashMode == FlashMode.TORCH) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            requestBuilder.set(CaptureRequest.FLASH_MODE, flashMode.toCamera2())
        } else {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, flashMode.toCamera2())
            requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
        }
    }

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
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
                if (lastCameraRequest != null) {
                    openLastRequestedCamera()
                    return
                }

                if (camera != null) {
                    cameraDevice = camera
                    _config = _availableCameras.first { it.id == cameraInfo.id }
                    _config.characteristics = cameraManager.getCameraCharacteristics(cameraDevice?.id)
                    devicePreview = DevicePreview(camera, { state = CameraState.READY }, { state = CameraState.CLOSED })
                    callback.onConfigReady()
                    startPreview()
                } else {
                    callback.onError(CameraFailedToOpenException)
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
            onAvailable = { _ ->
                try {
                    val texture = textureView?.surfaceTexture?.apply {
                        setDefaultBufferSize(config.previewSize.width, config.previewSize.height)
                    }
                    applyMatrixTransformation(textureView)
                    devicePreview?.startSession(Surface(texture))
                } catch (t: Throwable) {
                    LogDelegate.log(t)
                }
            },
            onSizeChanged = { applyMatrixTransformation(it) }
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
            val cameraConfig = Camera2ConfigImpl(
                cameraInfo = cameraInfo,
                videoConfig = VideoConfigImpl(id, onUpdateListener),
                featureConfig = FeatureConfigImpl(onUpdateListener),
                sizeConfig = SizeConfigImpl(onUpdateListener),
                zoomConfig = ZoomConfigImpl(onUpdateListener)
            )
            cameraConfig.characteristics = info
            _availableCameras.add(cameraConfig)
        }
    }

    private fun applyMatrixTransformation(textureView: TextureView?) {
        textureView?.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
    }
}