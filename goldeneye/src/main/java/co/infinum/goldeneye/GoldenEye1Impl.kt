@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

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
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.gesture.GestureHandler
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.CameraState
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.recorders.PictureRecorder
import co.infinum.goldeneye.recorders.VideoRecorder
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.Intrinsics
import co.infinum.goldeneye.utils.LogDelegate
import co.infinum.goldeneye.utils.LogDelegate.log
import java.io.File
import java.io.IOException

internal class GoldenEye1Impl @JvmOverloads constructor(
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
            CameraProperty.FOCUS -> camera?.updateParams { focusMode = config.focusMode.toCamera1() }
            CameraProperty.FLASH -> camera?.updateParams { flashMode = config.flashMode.toCamera1() }
            CameraProperty.COLOR_EFFECT -> camera?.updateParams { colorEffect = config.colorEffect.toCamera1() }
            CameraProperty.ANTIBANDING -> camera?.updateParams { antibanding = config.antibanding.toCamera1() }
            CameraProperty.SCENE_MODE -> camera?.updateParams { sceneMode = config.sceneMode.toCamera1() }
            CameraProperty.WHITE_BALANCE -> camera?.updateParams { whiteBalance = config.whiteBalance.toCamera1() }
            CameraProperty.PICTURE_SIZE -> updatePictureSize()
            CameraProperty.PREVIEW_SIZE -> updatePreviewSize()
            CameraProperty.ZOOM -> camera?.updateParams {
                //TODO update zoom
            }
            CameraProperty.VIDEO_STABILIZATION -> updateVideoStabilization()
            CameraProperty.PREVIEW_SCALE -> applyMatrixTransformation(textureView)
        }
    }

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
        try {
            Intrinsics.checkCameraPermission(activity)
            release()
            _config = _availableCameras.first { it.id == cameraInfo.id }
            openCamera(_config)
            camera?.let {
                this.gestureHandler = GestureHandler(activity, config, onZoomChangeCallback, onFocusChangeCallback)
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
        if (isCameraReady().not()) return

        state = CameraState.RECORDING
        if (config.autoPickPreviewSize) {
            config.previewSize = CameraUtils.findBestMatchingSize(config.videoSize, config.supportedPreviewSizes)
        }
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
                if (config.autoPickPreviewSize) {
                    config.previewSize = CameraUtils.findBestMatchingSize(config.pictureSize, config.supportedPreviewSizes)
                }
            }
        })
    }

    override fun stopRecording() {
        videoRecorder?.stopRecording()
    }

    @Throws(Throwable::class)
    private fun openCamera(config: Camera1ConfigImpl) {
        Camera.open(config.id)?.also {
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

                if (config.supportedAntibanding.contains(config.antibanding)) {
                    antibanding = config.antibanding.toCamera1()
                }

                if (config.supportedColorEffects.contains(config.colorEffect)) {
                    colorEffect = config.colorEffect.toCamera1()
                }

                if (config.supportedSceneModes.contains(config.sceneMode)) {
                    sceneMode = config.sceneMode.toCamera1()
                }

                if (config.supportedWhiteBalance.contains(config.whiteBalance)) {
                    whiteBalance = config.whiteBalance.toCamera1()
                }

                if (parameters.isZoomSupported) {
                    //                    zoom = config.zoom.level
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

            val cameraInfo = object : CameraInfo {
                override val id = id
                override val orientation = info.orientation
                override val facing = facing
                override val bestResolution = Size.UNKNOWN
            }

            _availableCameras.add(
                Camera1ConfigImpl(
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
        if (config.autoPickPreviewSize) {
            config.previewSize = CameraUtils.findBestMatchingSize(pictureSize, config.supportedPreviewSizes)
        }
    }

    private fun updatePreviewSize() {
        val previewSize = config.previewSize
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