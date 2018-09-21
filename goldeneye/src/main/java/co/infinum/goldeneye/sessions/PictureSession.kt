package co.infinum.goldeneye.sessions

import android.app.Activity
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.CameraLockedException
import co.infinum.goldeneye.GoldenEye2Impl
import co.infinum.goldeneye.PictureCallback
import co.infinum.goldeneye.PictureConversionException
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.*
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class PictureSession(
    private val activity: Activity,
    private val config: CameraConfig,
    private val cameraDevice: CameraDevice
) {

    private var imageReader: ImageReader? = null
    private var callback: PictureCallback? = null

    private var sessionBuilder: CaptureRequest.Builder? = null
    private var session: CameraCaptureSession? = null
    private var surface: Surface? = null
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

            if (isExposureReady(aeMode, aeState) && isFocusReady(afMode, afState) && isAwbReady(awbMode, awbState)) {
                locked = true
                capture()
            } else {
                session?.capture(sessionBuilder?.build(), this, GoldenEye2Impl.backgroundHandler)
            }
        }

        private fun capture() {
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                copyParamsFrom(sessionBuilder)
                addTarget(imageReader?.surface)
                session?.apply {
                    stopRepeating()
                    capture(build(), null, GoldenEye2Impl.backgroundHandler)
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
            } catch (t: Throwable) {
                LogDelegate.log(t)
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            LogDelegate.log("Session configuration failed.")
        }
    }

    fun createSession(textureView: TextureView) {
        try {
            initTextureViewSurface(textureView)
            initImageReader()
            cameraDevice.createCaptureSession(listOf(surface, imageReader?.surface), stateCallback, GoldenEye2Impl.backgroundHandler)
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    fun updateRequest(update: CaptureRequest.Builder.() -> Unit) {
        try {
            sessionBuilder?.apply(update)
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    fun startSession() {
        session?.setRepeatingRequest(sessionBuilder?.build(), null, GoldenEye2Impl.backgroundHandler)
    }

    fun singleCapture() {
        session?.capture(sessionBuilder?.build(), null, GoldenEye2Impl.backgroundHandler)
    }

    private fun initTextureViewSurface(textureView: TextureView) {
        val texture = textureView.surfaceTexture?.apply {
            setDefaultBufferSize(config.previewSize.width, config.previewSize.height)
        }
        textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
        this.surface = Surface(texture)
    }

    private fun initImageReader() {
        this.imageReader = ImageReader.newInstance(config.pictureSize.width, config.pictureSize.height, ImageFormat.JPEG, 2)
        imageReader?.setOnImageAvailableListener({ reader ->
            async(
                task = {
                    val image = reader.acquireLatestImage()
                    val bitmap = image.toBitmap()
                    image.close()
                    bitmap?.mutate {
                        reverseCameraRotation(
                            activity = activity,
                            info = config,
                            cx = bitmap.width / 2f,
                            cy = bitmap.height / 2f
                        )
                        if (config.facing == Facing.FRONT) {
                            mirror()
                        }
                    }
                },
                onResult = {
                    locked = false
                    startSession()
                    if (it != null) {
                        callback?.onPictureTaken(it)
                    } else {
                        callback?.onError(PictureConversionException)
                    }
                }
            )
        }, GoldenEye2Impl.backgroundHandler)
    }

    fun takePicture(callback: PictureCallback) {
        this.callback = callback
        sessionBuilder?.apply {
            set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
            set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            session?.capture(build(), captureCallback, GoldenEye2Impl.backgroundHandler)
            set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
            set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE)
        }
    }

    fun releaseSession() {
        try {
            session?.stopRepeating()
            session?.abortCaptures()
            session?.close()
            imageReader?.close()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            sessionBuilder = null
            session = null
            callback = null
            imageReader = null
        }
    }

    private enum class State {
        READY, PRECAPTURE, CAPTURE
    }
}