package co.infinum.goldeneye.sessions

import android.annotation.SuppressLint
import android.app.Activity
import android.hardware.camera2.*
import android.hardware.camera2.params.MeteringRectangle
import android.os.Build
import android.support.annotation.CallSuper
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.config.camera2.Camera2ConfigImpl
import co.infinum.goldeneye.extensions.isLocked
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.utils.AsyncUtils
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal abstract class BaseSession(
    protected val activity: Activity,
    protected val cameraDevice: CameraDevice,
    protected val config: Camera2ConfigImpl
) {

    protected var sessionBuilder: CaptureRequest.Builder? = null
    protected var session: CameraCaptureSession? = null
    protected var surface: Surface? = null

    abstract fun createSession(textureView: TextureView)

    /**
     * Apply config changes to [sessionBuilder].
     */
    fun updateRequest(update: CaptureRequest.Builder.() -> Unit) {
        try {
            sessionBuilder?.apply(update)
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    @SuppressLint("Recycle")
    protected fun initTextureViewSurface(textureView: TextureView) {
        textureView.setTransform(CameraUtils.calculateTextureMatrix(activity, textureView, config))
        val texture = textureView.surfaceTexture?.apply {
            val previewSize = config.previewSize
            setDefaultBufferSize(previewSize.width, previewSize.height)
        }
        this.surface = Surface(texture)
    }

    fun startSession() {
        session?.setRepeatingRequest(sessionBuilder?.build(), null, AsyncUtils.backgroundHandler)
    }

    /**
     * Cancel existing focus with [CameraMetadata.CONTROL_AF_TRIGGER_CANCEL] flag.
     *
     * This method is used before locking focus with tap to focus functionality.
     */
    fun lockFocus(region: Array<MeteringRectangle>) {
        cancelFocus()
        sessionBuilder?.apply {
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
            set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            set(CaptureRequest.CONTROL_AF_REGIONS, region)
        }
        session?.stopRepeating()
        session?.capture(sessionBuilder?.build(), object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
                if (result?.isLocked() == true) {
                } else {
                    session?.capture(sessionBuilder?.build(), this, AsyncUtils.backgroundHandler)
                }
            }

            override fun onCaptureFailed(session: CameraCaptureSession?, request: CaptureRequest?, failure: CaptureFailure?) {
            }
        }, AsyncUtils.backgroundHandler)
        sessionBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
        startSession()
    }

    fun cancelFocus() {
        sessionBuilder?.apply {
            set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL)
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
        }
        session?.capture(sessionBuilder?.build(), null, AsyncUtils.backgroundHandler)
    }

    @CallSuper
    open fun release() {
        try {
            surface?.release()
            session?.apply {
                stopRepeating()
                abortCaptures()
                close()
            }
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            sessionBuilder = null
            session = null
            surface = null
        }
    }

    fun unlockFocus(focus: FocusMode) {
        cancelFocus()
        sessionBuilder?.apply {
            set(CaptureRequest.CONTROL_AF_MODE, focus.toCamera2())
            set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
            set(CaptureRequest.CONTROL_AF_REGIONS, null)
        }
        startSession()
    }
}