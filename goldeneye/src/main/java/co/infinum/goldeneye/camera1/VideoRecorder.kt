package co.infinum.goldeneye.camera1

import android.app.Activity
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import co.infinum.goldeneye.VideoCallback
import co.infinum.goldeneye.camera1.config.CameraConfigImpl
import co.infinum.goldeneye.extensions.hasAudioPermission
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

internal class VideoRecorder(
    private val activity: Activity,
    private val camera: Camera,
    private val config: CameraConfigImpl
) {

    private var file: File? = null
    private var callback: VideoCallback? = null
    private var mediaRecorder: MediaRecorder? = null

    fun startRecording(file: File, callback: VideoCallback) {
        try {
            this.file = file
            this.callback = callback
            camera.unlock()
            mediaRecorder = MediaRecorder().apply {
                setCamera(camera)
                if (activity.hasAudioPermission()) {
                    setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                } else {
                    LogDelegate.log("Recording video without audio. Missing RECORD_AUDIO permission.")
                }
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                val videoSize = config.videoSize.takeIf { it != Size.UNKNOWN } ?: config.supportedVideoSizes[0]
                setProfile(CamcorderProfile.get(config.videoQuality.key).apply {
                    videoFrameHeight = videoSize.height
                    videoFrameWidth = videoSize.width
                })
                setOutputFile(file.absolutePath)
                setVideoSize(videoSize.width, videoSize.height)
                val cameraOrientation = CameraUtils.calculateDisplayOrientation(activity, config)
                setOrientationHint(if (config.facing == Facing.BACK) cameraOrientation else -cameraOrientation)
                prepare()
                start()
            }
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
        } catch (t: Throwable) {
            callback?.onError(t)
        } finally {
            mediaRecorder?.release()
            camera.reconnect()
            mediaRecorder = null
            callback = null
            file = null
        }
    }
}