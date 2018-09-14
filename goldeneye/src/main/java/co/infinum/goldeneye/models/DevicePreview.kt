package co.infinum.goldeneye.models

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class DevicePreview(
    private val cameraDevice: CameraDevice,
    private val onSessionStarted: () -> Unit,
    private val onSessionEnded: () -> Unit
) {

    private var requestBuilder: CaptureRequest.Builder? = null
    private var session: CameraCaptureSession? = null

    fun startSession(surface: Surface) {
        try {
            cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    session = cameraCaptureSession
                    try {
                        requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
                            addTarget(surface)
                        }
                        applyConfig()
                        cameraCaptureSession.setRepeatingRequest(requestBuilder?.build(), null, null)
                        onSessionStarted()
                    } catch (t: Throwable) {
                        onSessionEnded()
                        LogDelegate.log(t)
                    }
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    onSessionEnded()
                }
            }, null)
        } catch (t: Throwable) {
            LogDelegate.log(t)
            onSessionEnded()
        }
    }

    private fun applyConfig() {
        requestBuilder?.apply {
        }
    }

    fun updateRequest(update: CaptureRequest.Builder.() -> Unit) {
        try {
            val request = requestBuilder?.apply(update)?.build()
            session?.setRepeatingRequest(request, null, null)
        } catch (t: Throwable) {
            LogDelegate.log(t)
            onSessionEnded()
        }
    }

    fun stopSession() {
        session?.stopRepeating()
        onSessionEnded()
    }
}

