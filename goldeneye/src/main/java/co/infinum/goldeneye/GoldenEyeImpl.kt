@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.hardware.Camera
import android.view.TextureView
import co.infinum.goldeneye.LogDelegate.log
import java.io.IOException

class GoldenEyeImpl @JvmOverloads constructor(
    private val activity: Activity,
    logger: GoldenEye.Logger? = null
) : GoldenEye {

    private var camera: Camera? = null
    private var textureView: TextureView? = null

    private val onUpdateListener: (CameraProperty) -> Unit = {
        when (it) {
            CameraProperty.FOCUS -> camera?.updateParams { focusMode = currentConfig.focusMode.key }
            CameraProperty.FLASH -> camera?.updateParams { flashMode = currentConfig.flashMode.key }
            CameraProperty.SCALE -> applyMatrixTransformation(textureView)
            CameraProperty.PICTURE_SIZE -> {
                val pictureSize = currentConfig.pictureSize
                camera?.updateParams { setPictureSize(pictureSize.width, pictureSize.height) }
            }
            CameraProperty.VIDEO_SIZE -> log("Ignoring video size for now")
            CameraProperty.PREVIEW_SIZE -> {
                val previewSize = currentConfig.previewSize
                camera?.updateParams { setPreviewSize(previewSize.width, previewSize.height) }
                applyMatrixTransformation(textureView)
            }
        }
    }

    private val _availableCameras = mutableListOf<CameraConfigImpl>()
    override val availableCameras: List<CameraInfo>
        get() = _availableCameras.map { it.toCameraInfo() }

    private var _currentConfig: CameraConfigImpl = CameraConfigImpl(-1, -1, Facing.BACK, onUpdateListener)
    override val currentConfig: CameraConfig
        get() = _currentConfig
    override val currentCamera: CameraInfo
        get() = _currentConfig.toCameraInfo()

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    override fun init(cameraInfo: CameraInfo, callback: InitCallback) {
        try {
            Intrinsics.checkCameraPermission(activity)

            stop()
            _currentConfig = _availableCameras.first { it.id == cameraInfo.id }
            openCamera(_currentConfig)
            callback.onSuccess()
        } catch (t: Throwable) {
            callback.onError(t)
        }
    }

    override fun start(textureView: TextureView) {
        if (camera == null) {
            log("Camera is not initialized. Did you call init() method?")
            return
        }

        this.textureView = textureView
        textureView.onSurfaceUpdate(
            onAvailable = { startPreview() },
            onSizeChanged = { applyMatrixTransformation(it) }
        )
    }

    override fun stop() {
        camera?.let {
            it.stopPreview()
            it.release()
        }
        camera = null
    }

    override fun takePicture(callback: PictureCallback) {
        if (camera == null) {
            log("Camera is not initialized. Did you call init() method?")
            return
        }

        if (textureView == null) {
            log("Preview not active. Did you call start() method?")
            return
        }

        if (_currentConfig.locked) {
            log("Camera is currently locked.")
            return
        }

        try {
            _currentConfig.locked = true
            camera?.takePicture(
                onShutter = { callback.onShutter() },
                onPicture = {
                    _currentConfig.locked = false
                    callback.onPictureTaken(it)
                },
                onError = {
                    _currentConfig.locked = false
                    callback.onError(it)
                }
            )
        } catch (t: Throwable) {
            _currentConfig.locked = false
            callback.onError(t)
        }
    }

    @Throws(Throwable::class)
    private fun openCamera(config: CameraConfigImpl) {
        Camera.open(config.id)?.also {
            this.camera = it
            _currentConfig = config
            _currentConfig.cameraParameters = it.parameters
        }
    }

    private fun startPreview() {
        try {
            ifNotNull(camera, textureView) { camera, textureView ->
                camera.apply {
                    stopPreview()
                    setPreviewTexture(textureView.surfaceTexture)
                    setDisplayOrientation(CameraUtils.calculateDisplayOrientation(activity, currentConfig))
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
                val previewSize = currentConfig.previewSize
                setPreviewSize(previewSize.width, previewSize.height)
                val pictureSize = currentConfig.pictureSize
                setPictureSize(pictureSize.width, pictureSize.height)
                if (currentConfig.supportedFocusModes.contains(currentConfig.focusMode)) {
                    focusMode = currentConfig.focusMode.key
                }
                if (currentConfig.supportedFlashModes.contains(currentConfig.flashMode)) {
                    flashMode = currentConfig.flashMode.key
                }
            }
        }
    }

    private fun applyMatrixTransformation(textureView: TextureView?) {
        textureView?.setTransform(CameraUtils.calculateTextureMatrix(activity, currentConfig, textureView))
    }

    private fun initAvailableCameras() {
        for (id in 0 until Camera.getNumberOfCameras()) {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(id, cameraInfo)
            val facing = if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) Facing.BACK else Facing.FRONT
            _availableCameras.add(CameraConfigImpl(id, cameraInfo.orientation, facing, onUpdateListener))
        }
    }
}