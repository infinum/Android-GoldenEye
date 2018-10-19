@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.recorders

import android.app.Activity
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import co.infinum.goldeneye.MediaRecorderDeadException
import co.infinum.goldeneye.VideoCallback
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.buildCamera1Instance
import co.infinum.goldeneye.extensions.hasAudioPermission
import co.infinum.goldeneye.extensions.ifNotNull
import co.infinum.goldeneye.extensions.updateParams
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

/**
 * Camera1 wrapper around video recording logic. The only reason
 * this is used is to not have this whole implementation inside
 * [co.infinum.goldeneye.GoldenEye1Impl] class.
 */
internal class VideoRecorder(
    private val activity: Activity,
    private val camera: Camera,
    private val config: CameraConfig
) {

    private var file: File? = null
    private var callback: VideoCallback? = null
    private var mediaRecorder: MediaRecorder? = null

    fun startRecording(file: File, callback: VideoCallback) {
        this.file = file
        this.callback = callback
        if (activity.hasAudioPermission().not()) {
            LogDelegate.log("Recording video without audio. Missing RECORD_AUDIO permission.")
        }
        try {
            mediaRecorder = MediaRecorder().buildCamera1Instance(activity, camera, config, file).also {
                it.setOnErrorListener { _, _, _ ->
                    this.callback?.onError(MediaRecorderDeadException)
                }
            }

            mediaRecorder?.start()
        } catch (t: Throwable) {
            callback.onError(t)
            release()
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

    fun release() {
        try {
            mediaRecorder?.release()
        } catch (t: Throwable) {
            LogDelegate.log("Failed to release media recorder.", t)
        } finally {
            mediaRecorder = null
            callback = null
            file = null
        }
    }
}