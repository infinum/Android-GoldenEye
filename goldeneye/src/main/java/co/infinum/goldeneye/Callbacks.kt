package co.infinum.goldeneye

import android.graphics.Bitmap
import java.io.File

interface PictureCallback {
    fun onPictureTaken(picture: Bitmap)
    fun onError(t: Throwable)
}

interface VideoCallback {
    fun onVideoRecorded(file: File)
    fun onError(t: Throwable)
}

interface InitCallback {
    fun onSuccess()
    fun onError(t: Throwable)
}