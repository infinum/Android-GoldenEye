@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import android.app.Activity
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Build
import android.view.TextureView
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.models.CameraInfo
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import co.infinum.goldeneye.utils.LogDelegate.log
import java.io.File
import java.io.IOException

internal class GoldenEyeImpl @JvmOverloads constructor(
    private val activity: Activity,
    logger: GoldenEye.Logger? = null,
    private val onZoomChangeCallback: OnZoomChangeCallback? = null
) : GoldenEye {

    private var camera: Camera? = null
    private var textureView: TextureView? = null
    private var gestureHandler: GestureHandler? = null
    private var videoRecorder: VideoRecorder? = null
    private var pictureRecorder: PictureRecorder? = null

    private val onUpdateListener: (CameraProperty) -> Unit = {
        when (it) {
            CameraProperty.FOCUS -> camera?.updateParams { focusMode = config.focusMode.key }
            CameraProperty.FLASH -> camera?.updateParams { flashMode = config.flashMode.key }
            CameraProperty.COLOR_EFFECT -> camera?.updateParams { colorEffect = config.colorEffect.key }
            CameraProperty.ANTIBANDING -> camera?.updateParams { antibanding = config.antibanding.key }
            CameraProperty.SCENE_MODE -> camera?.updateParams { sceneMode = config.sceneMode.key }
            CameraProperty.WHITE_BALANCE -> camera?.updateParams { whiteBalance = config.whiteBalance.key }
            CameraProperty.SCALE -> applyMatrixTransformation(textureView)
            CameraProperty.PICTURE_SIZE -> {
                val pictureSize = config.pictureSize
                camera?.updateParams { setPictureSize(pictureSize.width, pictureSize.height) }
            }
            CameraProperty.PREVIEW_SIZE -> {
                val previewSize = config.previewSize
                camera?.updateParams { setPreviewSize(previewSize.width, previewSize.height) }
                applyMatrixTransformation(textureView)
            }
            CameraProperty.ZOOM -> {
                if (_config.smoothZoomEnabled) {
                    if (_config.zoomInProgress) {
                        camera?.setZoomChangeListener { zoomValue, stopped, camera ->
                            if (stopped && zoomValue != config.zoomLevel) {
                                camera.startSmoothZoom(config.zoomLevel)
                            } else {
                                _config.zoomInProgress = false
                            }
                        }
                        camera?.stopSmoothZoom()
                    } else {
                        camera?.startSmoothZoom(config.zoomLevel)
                        _config.zoomInProgress = true
                    }
                } else {
                    camera?.updateParams { zoom = config.zoomLevel }
                }
            }
            CameraProperty.VIDEO_STABILIZATION -> {
                camera?.updateParams {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        videoStabilization = config.videoStabilizationEnabled
                    }
                }
            }

        }
    }

    private val _availableCameras = mutableListOf<CameraConfigImpl>()
    override val availableCameras: List<CameraInfo>
        get() = _availableCameras.map { it.toCameraInfo() }

    private var _config: CameraConfigImpl = CameraConfigImpl(-1, -1, Facing.BACK, onUpdateListener)
    override val config: CameraConfig
        get() = _config

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    override fun init(cameraInfo: CameraInfo, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        init(cameraInfo, object : InitCallback {
            override fun onSuccess() {
                onSuccess()
            }

            override fun onError(t: Throwable) {
                onError(t)
            }
        })
    }

    override fun init(cameraInfo: CameraInfo, callback: InitCallback) {
        try {
            Intrinsics.checkCameraPermission(activity)
            release()
            val previousPreviewScale = _config.previewScale
            _config = _availableCameras.first { it.id == cameraInfo.id }
            _config.previewScale = previousPreviewScale
            openCamera(_config)
            camera?.let {
                this.gestureHandler = GestureHandler(activity, it, config, onZoomChangeCallback)
                this.videoRecorder = VideoRecorder(activity, it, _config)
                this.pictureRecorder = PictureRecorder(activity, it, _config)
            }
            callback.onSuccess()
        } catch (t: Throwable) {
            callback.onError(t)
        }
    }

    override fun startPreview(textureView: TextureView) {
        if (camera == null) {
            log("Camera is not initialized. Did you call init() method?")
            return
        }

        this.textureView = textureView
        this.gestureHandler?.init(textureView)

        textureView.onSurfaceUpdate(
            onAvailable = { startPreview() },
            onSizeChanged = { applyMatrixTransformation(it) }
        )
    }

    override fun release() {
        camera?.let {
            it.stopPreview()
            it.release()
        }
        camera = null
        gestureHandler?.release()
        gestureHandler = null
    }

    override fun takePicture(onPictureTaken: (Bitmap) -> Unit, onError: (Throwable) -> Unit, onShutter: (() -> Unit)?) {
        takePicture(object : PictureCallback() {
            override fun onPictureTaken(picture: Bitmap) {
                onPictureTaken(picture)
            }

            override fun onError(t: Throwable) {
                onError(t)
            }

            override fun onShutter() {
                onShutter?.invoke()
            }
        })
    }

    override fun takePicture(callback: PictureCallback) {
        if (isCameraReady().not()) return

        pictureRecorder?.takePicture(callback)
    }

    override fun startRecording(file: File, onVideoRecorded: (File) -> Unit, onError: (Throwable) -> Unit) {
        startRecording(file, object : VideoCallback {
            override fun onVideoRecorded(file: File) {
                onVideoRecorded(file)
            }

            override fun onError(t: Throwable) {
                onError(t)
            }
        })
    }

    override fun startRecording(file: File, callback: VideoCallback) {
        if (isCameraReady().not()) return

        videoRecorder?.startRecording(file, object : VideoCallback {
            override fun onVideoRecorded(file: File) {
                startPreview()
                callback.onVideoRecorded(file)
            }

            override fun onError(t: Throwable) {
                startPreview()
                callback.onError(t)
            }
        })
    }

    override fun stopRecording() {
        videoRecorder?.stopRecording()
    }

    @Throws(Throwable::class)
    private fun openCamera(config: CameraConfigImpl) {
        Camera.open(config.id)?.also {
            this.camera = it
            _config = config
            _config.cameraParameters = it.parameters
        }
    }

    private fun startPreview() {
        try {
            ifNotNull(camera, textureView) { camera, textureView ->
                camera.apply {
                    stopPreview()
                    setPreviewTexture(textureView.surfaceTexture)
                    setDisplayOrientation(CameraUtils.calculateDisplayOrientation(activity, config))
                    applyConfig()
                    applyMatrixTransformation(textureView)
                    startPreview()
                }
            }
        } catch (e: IOException) {
            log(e)
        }
    }

    private fun applyConfig() {
        camera?.apply {
            parameters = parameters.apply {
                val previewSize = config.previewSize
                setPreviewSize(previewSize.width, previewSize.height)

                val pictureSize = config.pictureSize
                setPictureSize(pictureSize.width, pictureSize.height)

                if (config.isVideoStabilizationSupported
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                ) {
                    videoStabilization = config.videoStabilizationEnabled
                }

                if (config.supportedFocusModes.contains(config.focusMode)) {
                    focusMode = config.focusMode.key
                }

                if (config.supportedFlashModes.contains(config.flashMode)) {
                    flashMode = config.flashMode.key
                }

                if (config.supportedAntibanding.contains(config.antibanding)) {
                    antibanding = config.antibanding.key
                }

                if (config.supportedColorEffects.contains(config.colorEffect)) {
                    colorEffect = config.colorEffect.key
                }

                if (config.supportedSceneModes.contains(config.sceneMode)) {
                    sceneMode = config.sceneMode.key
                }

                if (config.supportedWhiteBalance.contains(config.whiteBalance)) {
                    whiteBalance = config.whiteBalance.key
                }

                if (parameters.isZoomSupported) {
                    zoom = config.zoomLevel
                }
            }
        }
    }

    private fun applyMatrixTransformation(textureView: TextureView?) {
        textureView?.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
    }

    private fun initAvailableCameras() {
        for (id in 0 until Camera.getNumberOfCameras()) {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(id, cameraInfo)
            val facing = if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) Facing.BACK else Facing.FRONT
            _availableCameras.add(CameraConfigImpl(id, cameraInfo.orientation, facing, onUpdateListener))
        }
    }

    private fun isCameraReady(): Boolean {
        if (camera == null
            || pictureRecorder == null
            || videoRecorder == null
            || gestureHandler == null
        ) {
            log("Camera is not initialized. Did you call init() method?")
            return false
        }

        if (textureView == null) {
            log("Preview not active. Did you call start() method?")
            return false
        }

        if (_config.locked) {
            log("Camera is currently locked.")
            return false
        }

        return true
    }
}