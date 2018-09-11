package co.infinum.goldeneye

import android.graphics.Bitmap
import android.view.TextureView
import co.infinum.goldeneye.camera1.config.CameraInfo
import java.io.File

abstract class BaseGoldenEyeImpl : GoldenEye {
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