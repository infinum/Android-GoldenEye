package co.infinum.goldeneye

import android.Manifest
import android.graphics.Bitmap
import android.support.annotation.RequiresPermission
import android.view.TextureView
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.models.CameraApi
import co.infinum.goldeneye.models.CameraState
import java.io.File

internal abstract class BaseGoldenEyeImpl(
    version: CameraApi
) : GoldenEye {
    companion object {
        lateinit var version: CameraApi
        var state = CameraState.CLOSED
    }

    init {
        BaseGoldenEyeImpl.version = version
    }

    protected val isConfigAvailable: Boolean
        get() = when (state) {
            CameraState.CLOSED,
            CameraState.INITIALIZING -> false
            CameraState.READY,
            CameraState.ACTIVE,
            CameraState.TAKING_PICTURE,
            CameraState.RECORDING_VIDEO -> true
        }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun open(textureView: TextureView, cameraInfo: CameraInfo,
        onReady: ((CameraConfig) -> Unit)?, onActive: (() -> Unit)?, onError: (Throwable) -> Unit
    ) {
        open(textureView, cameraInfo, object : InitCallback() {

            override fun onReady(config: CameraConfig) {
                onReady?.invoke(config)
            }

            override fun onActive() {
                onActive?.invoke()
            }

            override fun onError(t: Throwable) {
                onError(t)
            }
        })
    }

    override fun takePicture(onPictureTaken: (Bitmap) -> Unit, onError: (Throwable) -> Unit, onShutter: (() -> Unit)?) {
        takePicture(object : PictureCallback() {
            override fun onPictureTaken(picture: Bitmap) {
                onPictureTaken(picture)
            }

            override fun onError(t: Throwable) {
                onError(t)
            }

            override fun onShutter() {
                onShutter?.invoke()
            }
        })
    }

    override fun startRecording(file: File, onVideoRecorded: (File) -> Unit, onError: (Throwable) -> Unit) {
        startRecording(file, object : VideoCallback {
            override fun onVideoRecorded(file: File) {
                onVideoRecorded(file)
            }

            override fun onError(t: Throwable) {
                onError(t)
            }
        })
    }
}