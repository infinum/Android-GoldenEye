package co.infinum.goldeneye.sessions

import android.app.Activity
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.media.MediaRecorder
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.Surface
import android.view.TextureView
import co.infinum.goldeneye.CameraConfigurationFailedException
import co.infinum.goldeneye.MediaRecorderDeadException
import co.infinum.goldeneye.VideoCallback
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.camera2.Camera2ConfigImpl
import co.infinum.goldeneye.extensions.applyConfig
import co.infinum.goldeneye.extensions.buildCamera2Instance
import co.infinum.goldeneye.extensions.hasAudioPermission
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.utils.AsyncUtils
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

/**
 * Class handles video recording session.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class VideoSession(
    activity: Activity,
    cameraDevice: CameraDevice,
    config: Camera2ConfigImpl
) : BaseSession(activity, cameraDevice, config) {

    private var mediaRecorder: MediaRecorder? = null
    private var callback: VideoCallback? = null
    private var file: File? = null
    private var mediaSurface: Surface? = null

    private val stateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            session = cameraCaptureSession
            try {
                /* Create new recording request */
                sessionBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)?.apply {
                    applyConfig(config)
                    addTarget(surface)
                    /* Important to add media recorder surface as output target */
                    addTarget(mediaSurface)
                }
                /* Start recording */
                session?.setRepeatingRequest(sessionBuilder?.build(), null, AsyncUtils.backgroundHandler)
                mediaRecorder?.start()
            } catch (t: Throwable) {
                callback?.onError(t)
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            callback?.onError(CameraConfigurationFailedException)
        }
    }

    fun startRecording(textureView: TextureView, file: File, callback: VideoCallback) {
        this.file = file
        this.callback = callback
        try {
            createSession(textureView)
        } catch (t: Throwable) {
            callback.onError(t)
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder?.stop()
            ifNotNull(callback, file) { callback, file ->
                callback.onVideoRecorded(file)
            }
            mediaRecorder?.reset()
        } catch (t: Throwable) {
            callback?.onError(t)
        }
    }

    private fun initMediaRecorder(file: File) {
        if (activity.hasAudioPermission().not()) {
            LogDelegate.log("Recording video without audio. Missing RECORD_AUDIO permission.")
        }
        mediaRecorder = MediaRecorder().buildCamera2Instance(activity, config, file).apply {
            setOnErrorListener { _, _, _ -> callback?.onError(MediaRecorderDeadException) }
        }
    }

    override fun createSession(textureView: TextureView) {
        initTextureViewSurface(textureView)
        initMediaRecorder(file!!)
        /*
         * mediaRecorder.getSurface() returns new surface on every getter call.
         * That is why it is a must to save it to a variable and reuse it.
        */
        mediaSurface = mediaRecorder?.surface
        cameraDevice.createCaptureSession(listOf(surface, mediaSurface), stateCallback, AsyncUtils.backgroundHandler)
    }

    override fun release() {
        super.release()
        callback = null
        file = null
        try {
            mediaSurface?.release()
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            mediaRecorder = null
            mediaSurface = null
        }
    }
}