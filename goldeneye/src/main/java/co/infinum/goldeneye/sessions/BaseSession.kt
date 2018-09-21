package co.infinum.goldeneye.sessions

import android.app.Activity
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.CallSuper
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.utils.AsyncUtils
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal abstract class BaseSession(
    protected val activity: Activity,
    protected val cameraDevice: CameraDevice,
    protected val config: CameraConfig
) {

    protected var sessionBuilder: CaptureRequest.Builder? = null
    protected var session: CameraCaptureSession? = null
    protected var surface: Surface? = null

    abstract fun createSession(textureView: TextureView)

    fun updateRequest(update: CaptureRequest.Builder.() -> Unit) {
        try {
            sessionBuilder?.apply(update)
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    protected fun initTextureViewSurface(textureView: TextureView) {
        val texture = textureView.surfaceTexture?.apply {
            val previewSize = config.previewSize
            setDefaultBufferSize(previewSize.width, previewSize.height)
        }
        textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, textureView, config))
        this.surface = Surface(texture)
    }

    fun startSession() {
        session?.setRepeatingRequest(sessionBuilder?.build(), null, AsyncUtils.backgroundHandler)
    }

    fun cancelFocus() {
        sessionBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
        session?.capture(sessionBuilder?.build(), null, AsyncUtils.backgroundHandler)
    }

    @CallSuper
    open fun release() {

        try {
            session?.apply {
                stopRepeating()
                abortCaptures()
                close()
            }
            surface?.release()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            sessionBuilder = null
            session = null
            surface = null
        }
    }
}