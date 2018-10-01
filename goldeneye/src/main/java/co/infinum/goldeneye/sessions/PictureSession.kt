package co.infinum.goldeneye.sessions

import android.app.Activity
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.*
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.applyConfig
import co.infinum.goldeneye.extensions.async
import co.infinum.goldeneye.extensions.copyParamsFrom
import co.infinum.goldeneye.extensions.toBitmap
import co.infinum.goldeneye.models.CameraState
import co.infinum.goldeneye.utils.AsyncUtils
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

/**
 * Class handles Preview and Picture capturing session.
 *
 * When preview session is created, [imageReader] is initialized which is later
 * used to capture picture. Session properties can be updated dynamically
 * except preview and picture size. If any of those is updated, session
 * must be recreated.
 *
 * Steps to take picture are:
 * 1) Lock focus and trigger AF and AE with
 * [CaptureRequest.CONTROL_AF_TRIGGER_START], [CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START]
 *
 * 2) As soon as AF and AE is triggered, flags must be reset
 *
 * 3) After first callback is received in [captureCallback], repeat capture request until
 * AF and AE are both ready. Once they are ready, trigger picture capture.
 *
 * 4) To capture picture create new request. New request MUST contain [imageReader] surface!
 * After capture is successful, [imageReader] will receive callback. Fetch bitmap from the
 * callback and voila.
 *
 * 5) Restart preview session
 *
 * @see ImageReader
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class PictureSession(
    activity: Activity,
    cameraDevice: CameraDevice,
    config: CameraConfig,
    private val pictureTransformation: PictureTransformation
) : BaseSession(activity, cameraDevice, config) {

    private var imageReader: ImageReader? = null
    private var pictureCallback: PictureCallback? = null
    private var initCallback: InitCallback? = null
    private var locked = false

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            if (result != null) {
                process(result)
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession?, request: CaptureRequest?, partialResult: CaptureResult?) {
            if (partialResult != null) {
                process(partialResult)
            }
        }

        private fun process(result: CaptureResult) {
            if (locked) return

            val aeMode = result.get(CaptureResult.CONTROL_AE_MODE)
            val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
            val afMode = result.get(CaptureResult.CONTROL_AF_MODE)
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            val awbMode = result.get(CaptureResult.CONTROL_AWB_MODE)
            val awbState = result.get(CaptureResult.CONTROL_AWB_STATE)

            /* Wait for all states to be ready, if they are not ready repeat basic capture while camera is preparing for capture */
            if (isExposureReady(aeMode, aeState) && isFocusReady(afMode, afState) && isAwbReady(awbMode, awbState)) {
                /* Take picture */
                locked = true
                capture()
            } else {
                /* Wait while camera is preparing */
                session?.capture(sessionBuilder?.build(), this, AsyncUtils.backgroundHandler)
            }
        }

        private fun capture() {
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                copyParamsFrom(sessionBuilder)
                /* Set picture quality */
                set(CaptureRequest.JPEG_QUALITY, config.jpegQuality.toByte())
                /* Add surface target that will receive capture */
                addTarget(imageReader?.surface)
                session?.apply {
                    /* Freeze preview session */
                    stopRepeating()
                    /* Take dat picture */
                    capture(build(), null, AsyncUtils.backgroundHandler)
                }
            }
        }

        private fun isAwbReady(awbMode: Int?, awbState: Int?) =
            awbMode != CaptureResult.CONTROL_AWB_MODE_AUTO
                || awbState == null
                || awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED
                || awbState == CaptureResult.CONTROL_AWB_STATE_LOCKED

        private fun isExposureReady(aeMode: Int?, aeState: Int?) =
            aeMode == CaptureResult.CONTROL_AE_MODE_OFF
                || aeState == null
                || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
                || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
                || aeState == CaptureResult.CONTROL_AE_STATE_LOCKED

        private fun isFocusReady(afMode: Int?, afState: Int?) =
            afMode == CaptureResult.CONTROL_AF_MODE_OFF
                || afState == null
                || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED
                || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED
    }

    private val stateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            session = cameraCaptureSession
            try {
                sessionBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
                    applyConfig(config)
                    addTarget(surface)
                }
                startSession()
                initCallback?.onActive()
                initCallback = null
            } catch (t: Throwable) {
                LogDelegate.log(t)
                initCallback?.onError(t)
                initCallback = null
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            LogDelegate.log(CameraConfigurationFailedException)
            initCallback?.onError(CameraConfigurationFailedException)
            initCallback = null
        }
    }

    fun createInitialPreviewSession(textureView: TextureView, callback: InitCallback) {
        this.initCallback = callback
        createSession(textureView)
    }

    override fun createSession(textureView: TextureView) {
        try {
            initTextureViewSurface(textureView)
            initImageReader()
            cameraDevice.createCaptureSession(listOf(surface, imageReader?.surface), stateCallback, AsyncUtils.backgroundHandler)
        } catch (t: Throwable) {
            LogDelegate.log(t)
            initCallback?.onError(t)
            initCallback = null
        }
    }

    private fun initImageReader() {
        this.imageReader = ImageReader.newInstance(config.pictureSize.width, config.pictureSize.height, ImageFormat.JPEG, 2)
        imageReader?.setOnImageAvailableListener({ reader ->
            async(
                task = {
                    val image = reader.acquireLatestImage()
                    val bitmap = image.toBitmap()
                    image.close()
                    if (bitmap != null) {
                        val orientationDifference = CameraUtils.calculateDisplayOrientation(activity, config).toFloat()
                        pictureTransformation.transform(bitmap, config, orientationDifference)
                    } else {
                        null
                    }
                },
                onResult = {
                    locked = false
                    startSession()
                    if (it != null) {
                        pictureCallback?.onPictureTaken(it)
                    } else {
                        pictureCallback?.onError(PictureConversionException)
                    }
                }
            )
        }, AsyncUtils.backgroundHandler)
    }

    fun takePicture(callback: PictureCallback) {
        this.pictureCallback = callback
        sessionBuilder?.apply {
            /* Trigger AF and AE */
            set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
            set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            session?.capture(build(), captureCallback, AsyncUtils.backgroundHandler)
            /* Immediately remove trigger flags to avoid recursive triggering */
            set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
            set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE)
        }
    }

    override fun release() {
        super.release()
        try {
            imageReader?.close()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            surface = null
            pictureCallback = null
            initCallback = null
            imageReader = null
        }
    }
}