@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.camera1

import android.app.Activity
import android.graphics.Bitmap
import android.hardware.Camera
import android.os.Build
import android.view.TextureView
import co.infinum.goldeneye.*
import co.infinum.goldeneye.camera1.config.*
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.onSurfaceUpdate
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.CameraState
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import co.infinum.goldeneye.utils.LogDelegate.log
import java.io.File
import java.io.IOException

internal class GoldenEyeImpl @JvmOverloads constructor(
    private val activity: Activity,
    private val onZoomChangeCallback: OnZoomChangeCallback? = null,
    private val onFocusChangeCallback: OnFocusChangeCallback? = null,
    logger: Logger? = null
) : BaseGoldenEyeImpl() {

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
            CameraProperty.PICTURE_SIZE -> updatePictureSize()
            CameraProperty.PREVIEW_SIZE -> updatePreviewSize(config.pictureSize)
            CameraProperty.ZOOM -> camera?.updateParams { zoom = config.zoom.level }
            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization()
            CameraProperty.PREVIEW_SCALE -> applyMatrixTransformation(textureView)
        }
    }

    private val _availableCameras = mutableListOf<CameraConfigImpl>()
    override val availableCameras: List<CameraInfo> = _availableCameras

    private lateinit var _config: CameraConfigImpl
    override val config: CameraConfig
        get() = _config

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    override fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        try {
            Intrinsics.checkCameraPermission(activity)
            release()
            _config = _availableCameras.first { it.id == cameraInfo.id }
            openCamera(_config)
            camera?.let {
                this.gestureHandler = GestureHandler(activity, it, config, onZoomChangeCallback, onFocusChangeCallback)
                this.videoRecorder = VideoRecorder(activity, it, _config)
                this.pictureRecorder = PictureRecorder(activity, it, _config)
            }
            callback.onConfigReady()
            state = CameraState.READY

            this.textureView = textureView
            this.gestureHandler?.init(textureView)
            textureView.onSurfaceUpdate(
                onAvailable = { startPreview() },
                onSizeChanged = { applyMatrixTransformation(it) }
            )
        } catch (t: Throwable) {
            callback.onError(t)
        }

    }

    override fun release() {
        camera?.let {
            it.stopPreview()
            it.release()
        }
        camera = null
        gestureHandler?.release()
        gestureHandler = null
        state = CameraState.CLOSED
    }

    override fun takePicture(callback: PictureCallback) {
        if (isCameraReady().not()) return

        state = CameraState.TAKING_PICTURE
        pictureRecorder?.takePicture(object: PictureCallback() {
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
        if (isCameraReady().not()) return

        state = CameraState.RECORDING
        updatePreviewSize(config.videoSize)
        videoRecorder?.startRecording(file, object : VideoCallback {
            override fun onVideoRecorded(file: File) {
                resetCameraPreview()
                callback.onVideoRecorded(file)
            }

            override fun onError(t: Throwable) {
                resetCameraPreview()
                callback.onError(t)
            }

            private fun resetCameraPreview() {
                state = CameraState.READY
                startPreview()
                updatePreviewSize(config.pictureSize)
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
            _config.params = it.parameters
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
                val pictureSize = config.pictureSize
                setPictureSize(pictureSize.width, pictureSize.height)

                val previewSize = config.previewSize.takeIf { it != Size.UNKNOWN }
                    ?: CameraUtils.findBestMatchingSize(pictureSize, config.supportedPreviewSizes)
                setPreviewSize(previewSize.width, previewSize.height)


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
                    zoom = config.zoom.level
                }
            }
        }
    }

    private fun applyMatrixTransformation(textureView: TextureView?) {
        textureView?.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
    }

    private fun initAvailableCameras() {
        for (id in 0 until Camera.getNumberOfCameras()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(id, info)
            val facing = if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) Facing.BACK else Facing.FRONT

            val cameraInfo = object: CameraInfo {
                override val id = id
                override val orientation = info.orientation
                override val facing = facing
                override val bestResolution = Size.UNKNOWN
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

        if (state != CameraState.READY) {
            log("Camera is not ready.")
            return false
        }

        return true
    }

    private fun updatePictureSize() {
        val pictureSize = config.pictureSize
        camera?.updateParams { setPictureSize(pictureSize.width, pictureSize.height) }
        updatePreviewSize(pictureSize)
    }

    private fun updatePreviewSize(referenceSize: Size) {
        val previewSize = config.previewSize.takeIf { it != Size.UNKNOWN }
            ?: CameraUtils.findBestMatchingSize(referenceSize, config.supportedPreviewSizes)
        camera?.updateParams { setPreviewSize(previewSize.width, previewSize.height) }
        applyMatrixTransformation(textureView)
    }

    private fun updateVideoStabilization() {
        camera?.updateParams {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                videoStabilization = config.videoStabilizationEnabled
            }
        }
    }
}