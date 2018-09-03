package co.infinum.goldeneye.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import co.infinum.goldeneye.MissingCameraPermissionException
import co.infinum.goldeneye.TaskOnMainThreadException

internal object Intrinsics {

    @Throws(MissingCameraPermissionException::class)
    fun checkCameraPermission(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw MissingCameraPermissionException
        }
    }

    fun checkMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw TaskOnMainThreadException
        }
    }
}