package co.infinum.goldeneye

import android.graphics.Bitmap
import java.io.File

abstract class PictureCallback {
    abstract fun onPictureTaken(picture: Bitmap)
    abstract fun onError(t: Throwable)
    fun onShutter() {
    }
}

interface VideoCallback {
    fun onVideoRecorded(file: File)
    fun onError(t: Throwable)
}

interface InitCallback {
    fun onSuccess()
    fun onError(t: Throwable)
}