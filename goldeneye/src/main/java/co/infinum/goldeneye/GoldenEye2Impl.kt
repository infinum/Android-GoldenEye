package co.infinum.goldeneye

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.config.*
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class GoldenEye2Impl(
    private val activity: Activity,
    logger: Logger? = null
) : GoldenEye {

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

    private fun initAvailableCameras() {
        with(activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager) {
            cameraIdList.forEach { id ->
                val info = getCameraCharacteristics(id)
                val orientation = info[CameraCharacteristics.SENSOR_ORIENTATION]
                val facing = when (info[CameraCharacteristics.LENS_FACING]) {
                    CameraCharacteristics.LENS_FACING_BACK -> Facing.BACK
                    CameraCharacteristics.LENS_FACING_FRONT -> Facing.FRONT
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> Facing.EXTERNAL
                    else -> throw RuntimeException()
                }
                val cameraInfo = object : CameraInfo {
                    override val id = id
                    override val orientation = orientation
                    override val facing = facing
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

    private val _availableCameras = mutableListOf<CameraConfigImpl>()
    override val availableCameras: List<CameraInfo> = _availableCameras

    override val config: CameraConfig
        get() = TODO("not implemented")

    override fun open(textureView: TextureView, cameraInfo: CameraInfo, callback: InitCallback) {
        TODO("not implemented")
    }

    override fun open(textureView: TextureView, cameraInfo: CameraInfo, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        TODO("not implemented")
    }

    override fun release() {
        TODO("not implemented")
    }

    override fun takePicture(callback: PictureCallback) {
        TODO("not implemented")
    }

    override fun takePicture(onPictureTaken: (Bitmap) -> Unit, onError: (Throwable) -> Unit, onShutter: (() -> Unit)?) {
        TODO("not implemented")
    }

    override fun startRecording(file: File, callback: VideoCallback) {
        TODO("not implemented")
    }

    override fun startRecording(file: File, onVideoRecorded: (File) -> Unit, onError: (Throwable) -> Unit) {
        TODO("not implemented")
    }

    override fun stopRecording() {
        TODO("not implemented")
    }
}