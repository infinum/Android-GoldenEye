package co.infinum.goldeneye

import android.Manifest
import android.graphics.Bitmap
import android.support.annotation.RequiresPermission
import android.view.TextureView
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.models.CameraState
import java.io.File

internal abstract class BaseGoldenEyeImpl : GoldenEye {
    companion object {
        var state = CameraState.CLOSED
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    override fun open(textureView: TextureView, cameraInfo: CameraInfo, onSuccess: () -> Unit, onError: (Throwable) -> Unit) {
        open(textureView, cameraInfo, object : InitCallback {
            override fun onConfigReady() {
                onSuccess()
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