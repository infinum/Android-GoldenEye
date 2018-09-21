package co.infinum.goldeneye.sessions

import android.app.Activity
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.CameraConfig

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class VideoSession(
    private val activity: Activity,
    private val config: CameraConfig
) {

//    private var file: File? = null
//    private var callback: VideoCallback? = null
//    private var mediaRecorder: MediaRecorder? = null
//
//    fun startRecording(file: File, callback: VideoCallback) {
//        try {
//            this.file = file
//            this.callback = callback
//            mediaRecorder = MediaRecorder().apply {
//                if (activity.hasAudioPermission()) {
//                    setAudioSource(MediaRecorder.AudioSource.DEFAULT)
//                } else {
//                    LogDelegate.log("Recording video without audio. Missing RECORD_AUDIO permission.")
//                }
//                setVideoSource(MediaRecorder.VideoSource.SURFACE)
//                val videoSize = config.videoSize.takeIf { it != Size.UNKNOWN } ?: config.supportedVideoSizes[0]
//                setProfile(CamcorderProfile.get(config.videoQuality.key).apply {
//                    videoFrameHeight = videoSize.height
//                    videoFrameWidth = videoSize.width
//                })
//                setOutputFile(file.absolutePath)
//                setVideoSize(videoSize.width, videoSize.height)
//                val cameraOrientation = CameraUtils.calculateDisplayOrientation(activity, config)
//                setOrientationHint(if (config.facing == Facing.BACK) cameraOrientation else -cameraOrientation)
//                prepare()
//                start()
//            }
//        } catch (t: Throwable) {
//            callback.onError(t)
//        }
//    }
//
//    fun stopRecording() {
//        try {
//            mediaRecorder?.stop()
//            ifNotNull(callback, file) { callback, file ->
//                callback.onVideoRecorded(file)
//            }
//        } catch (t: Throwable) {
//            callback?.onError(t)
//        } finally {
//            mediaRecorder?.release()
//            camera.reconnect()
//            mediaRecorder = null
//            callback = null
//            file = null
//        }
//    }
}