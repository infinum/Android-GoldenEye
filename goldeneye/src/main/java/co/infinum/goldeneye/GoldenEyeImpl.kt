@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import android.annotation.SuppressLint
import android.app.Activity
import android.hardware.Camera
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.TextureView
import co.infinum.goldeneye.LogDelegate.log
import java.io.IOException

private const val DELAY_FOCUS_RESET = 10_000L

class GoldenEyeImpl @JvmOverloads constructor(
    private val activity: Activity,
    logger: GoldenEye.Logger? = null
) : GoldenEye {

    private var camera: Camera? = null
    private var textureView: TextureView? = null
    private var mainHandler = Handler(Looper.getMainLooper())

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

    init {
        LogDelegate.logger = logger
        initAvailableCameras()
    }

    override fun init(cameraInfo: CameraInfo, callback: InitCallback) {
        try {
            mainHandler.removeCallbacksAndMessages(null)
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
        initTapToFocus()

        textureView.onSurfaceUpdate(
            onAvailable = {
                startPreview()
            },
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
                onShutter = {
                    callback.onShutter()
                },
                onPicture = {
                    _currentConfig.locked = false
                    callback.onPictureTaken(it)
                    camera?.startPreview()
                },
                onError = {
                    _currentConfig.locked = false
                    callback.onError(it)
                    camera?.startPreview()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initTapToFocus() {
        textureView?.setOnTouchListener { _, event ->
            if (currentConfig.isTapToFocusEnabled.not()
                || event.actionMasked != MotionEvent.ACTION_DOWN
                || currentConfig.supportedFocusModes.contains(FocusMode.AUTO).not()
            ) {
                return@setOnTouchListener false
            }

            ifNotNull(camera, textureView) { camera, textureView ->
                camera.updateParams {
                    focusMode = FocusMode.AUTO.key
                    val areas = CameraUtils.calculateFocusArea(activity, textureView, currentConfig, event.x, event.y)
                    if (maxNumFocusAreas > 0) {
                        focusAreas = areas
                    }
                    if (maxNumMeteringAreas > 0) {
                        meteringAreas = areas
                    }
                }

                camera.autoFocus { success, _ ->
                    if (success) {
                        camera.cancelAutoFocus()
                        resetFocusWithDelay()
                    }
                }
            }

            return@setOnTouchListener true
        }
    }

    private fun resetFocusWithDelay() {
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({ camera?.updateParams { focusMode = currentConfig.focusMode.key } }, DELAY_FOCUS_RESET)
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