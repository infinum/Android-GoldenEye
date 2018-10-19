@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.extensions

import android.app.Activity
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.utils.CameraUtils
import java.io.File

internal fun MediaRecorder.buildCamera1Instance(
    activity: Activity,
    camera: Camera,
    config: CameraConfig,
    file: File
): MediaRecorder {
    setCamera(camera)
    setVideoSource(MediaRecorder.VideoSource.CAMERA)
    return buildInstance(activity, config, file)
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal fun MediaRecorder.buildCamera2Instance(
    activity: Activity,
    config: CameraConfig,
    file: File
): MediaRecorder {
    setVideoSource(MediaRecorder.VideoSource.SURFACE)
    return buildInstance(activity, config, file)
}

/**
 * Reusable build instance method to apply given config to [MediaRecorder].
 */
private fun MediaRecorder.buildInstance(activity: Activity, config: CameraConfig, file: File): MediaRecorder {
    val profile = CamcorderProfile.get(config.id.toInt(), config.videoQuality.key)
    if (activity.hasAudioPermission()) {
        setAudioSource(MediaRecorder.AudioSource.DEFAULT)
    }
    setOutputFormat(profile.fileFormat)
    setVideoFrameRate(profile.videoFrameRate)
    setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight)
    setVideoEncodingBitRate(profile.videoBitRate)
    setVideoEncoder(profile.videoCodec)

    if (activity.hasAudioPermission()) {
        setAudioEncodingBitRate(profile.audioBitRate)
        setAudioChannels(profile.audioChannels)
        setAudioSamplingRate(profile.audioSampleRate)
        setAudioEncoder(profile.audioCodec)
    }

    setOutputFile(file.absolutePath)
    val cameraOrientation = CameraUtils.calculateDisplayOrientation(activity, config)
    setOrientationHint(if (config.facing == Facing.FRONT) (360 - cameraOrientation) % 360 else cameraOrientation)
    prepare()
    return this
}