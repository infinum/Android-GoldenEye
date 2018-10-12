package co.infinum.goldeneye.sessions

import android.app.Activity
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.*
import co.infinum.goldeneye.config.camera2.Camera2ConfigImpl
import co.infinum.goldeneye.extensions.*
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
    config: Camera2ConfigImpl,
    private val pictureTransformation: PictureTransformation?
) : BaseSession(activity, cameraDevice, config) {

    private var imageReader: ImageReader? = null
    private var pictureCallback: PictureCallback? = null
    private var initCallback: InitCallback? = null
    private var locked = false

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
            if (result != null) {
                process(result, true)
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            process(partialResult, false)
        }

        private fun process(result: CaptureResult, isCompleted: Boolean) {
            try {
                if (locked) return

                /* Wait for all states to be ready, if they are not ready repeat basic capture while camera is preparing for capture */
                if (result.isLocked()) {
                    /* Take picture */
                    locked = true
                    capture()
                } else if (isCompleted) {
                    /* Wait while camera is preparing */
                    session?.capture(sessionBuilder?.build()!!, this, AsyncUtils.backgroundHandler)
                }
            } catch (t: Throwable) {
                LogDelegate.log("Failed to take picture.", t)
                pictureCallback?.onError(t)
            }
        }

        private fun capture() {
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                copyParamsFrom(sessionBuilder)
                /* Set picture quality */
                set(CaptureRequest.JPEG_QUALITY, config.jpegQuality.toByte())
                /* Add surface target that will receive capture */
                addTarget(imageReader?.surface!!)
                session?.apply {
                    /* Freeze preview session */
                    stopRepeating()
                    /* Take dat picture */
                    capture(build(), null, AsyncUtils.backgroundHandler)
                }
            }
        }
    }

    private val stateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            session = cameraCaptureSession
            try {
                sessionBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    applyConfig(config)
                    addTarget(surface!!)
                }
                startSession()
                initCallback?.onActive()
                initCallback = null
            } catch (t: Throwable) {
                LogDelegate.log("Failed to open camera preview.", t)
                initCallback?.onError(t)
                initCallback = null
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            LogDelegate.log("Failed to configure camera.", CameraConfigurationFailedException)
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
            LogDelegate.log("Failed to create session.", t)
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
                        pictureTransformation?.transform(bitmap, config, orientationDifference) ?: bitmap
                    } else {
                        null
                    }
                },
                onResult = {
                    locked = false
                    try {
                        startSession()
                    } catch (t: Throwable) {
                        LogDelegate.log("Failed to restart camera preview.", t)
                    }
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
            if (config.isHardwareAtLeastLimited()) {
                set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            }
            session?.capture(build(), captureCallback, AsyncUtils.backgroundHandler)
            set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
            if (config.isHardwareAtLeastLimited()) {
                set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE)
            }
        }
    }

    override fun release() {
        super.release()
        surface = null
        pictureCallback = null
        initCallback = null
        try {
            imageReader?.close()
        } catch (t: Throwable) {
            LogDelegate.log("Failed to release picture session.", t)
        } finally {
            imageReader = null
        }
    }
}