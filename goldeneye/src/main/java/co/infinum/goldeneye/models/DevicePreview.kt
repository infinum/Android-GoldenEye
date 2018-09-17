package co.infinum.goldeneye.models

import android.app.Activity
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class DevicePreview(
    private val activity: Activity,
    private val config: CameraConfig,
    private val cameraDevice: CameraDevice,
    private val onStarted: () -> Unit,
    private val onEnded: () -> Unit
) {

    var requestBuilder: CaptureRequest.Builder? = null
    var session: CameraCaptureSession? = null
    private var surface: Surface? = null

    private val stateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            session = cameraCaptureSession
            try {
                requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
                    addTarget(surface)
                }
                applyConfig()
                cameraCaptureSession.setRepeatingRequest(requestBuilder?.build(), null, null)
                onStarted()
            } catch (t: Throwable) {
                onEnded()
                LogDelegate.log(t)
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            onEnded()
        }
    }

    fun startPreview(textureView: TextureView) {
        try {
            val texture = textureView.surfaceTexture?.apply {
                setDefaultBufferSize(config.previewSize.width, config.previewSize.height)
            }
            textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, config, textureView))
            val surface = Surface(texture)
            cameraDevice.createCaptureSession(listOf(surface), stateCallback, null)
        } catch (t: Throwable) {
            LogDelegate.log(t)
            onEnded()
        }
    }

    private fun applyConfig() {
        requestBuilder?.apply {
            //TODO
        }
    }

    fun updateRequest(update: CaptureRequest.Builder.() -> Unit) {
        try {
            val request = requestBuilder?.apply(update)?.build()
            session?.setRepeatingRequest(request, null, null)
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    fun release() {
        try {
            session?.close()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            requestBuilder = null
            session = null
            onEnded()
        }
    }
}

