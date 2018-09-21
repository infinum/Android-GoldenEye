package co.infinum.goldeneye.sessions

import android.app.Activity
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.media.MediaRecorder
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.VideoCallback
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.applyConfig
import co.infinum.goldeneye.extensions.buildCamera2Instance
import co.infinum.goldeneye.extensions.hasAudioPermission
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.utils.AsyncUtils
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class VideoSession(
    activity: Activity,
    cameraDevice: CameraDevice,
    config: CameraConfig
) : BaseSession(activity, cameraDevice, config) {

    private var mediaRecorder: MediaRecorder? = null
    private var callback: VideoCallback? = null
    private var file: File? = null
    private var mediaSurface: Surface? = null

    private val stateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            session = cameraCaptureSession
            try {
                sessionBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)?.apply {
                    applyConfig(config)
                    addTarget(surface)
                    addTarget(mediaSurface)
                }
                session?.setRepeatingRequest(sessionBuilder?.build(), null, AsyncUtils.backgroundHandler)
                mediaRecorder?.start()
            } catch (t: Throwable) {
                LogDelegate.log(t)
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            LogDelegate.log("Session configuration failed.")
        }
    }

    fun startRecording(textureView: TextureView, file: File, callback: VideoCallback) {
        this.file = file
        this.callback = callback
        try {
            createSession(textureView)
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            ifNotNull(callback, file) { callback, file ->
                callback.onVideoRecorded(file)
            }
        } catch (t: Throwable) {
            callback?.onError(t)
        } finally {
            release()
        }
    }

    private fun initMediaRecorder(file: File) {
        if (activity.hasAudioPermission().not()) {
            LogDelegate.log("Recording video without audio. Missing RECORD_AUDIO permission.")
        }
        mediaRecorder = MediaRecorder().buildCamera2Instance(activity, config, file)
    }

    override fun createSession(textureView: TextureView) {
        initTextureViewSurface(textureView)
        initMediaRecorder(file!!)
        mediaSurface = mediaRecorder?.surface
        cameraDevice.createCaptureSession(listOf(surface, mediaSurface), stateCallback, AsyncUtils.backgroundHandler)
    }

    override fun release() {
        super.release()
        try {
            mediaRecorder?.release()
            mediaSurface?.release()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            callback = null
            file = null
            mediaRecorder = null
            mediaSurface = null
        }
    }
}