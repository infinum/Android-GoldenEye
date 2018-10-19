package co.infinum.goldeneye.utils

import android.content.Context
import android.os.Looper
import co.infinum.goldeneye.MissingCameraPermissionException
import co.infinum.goldeneye.TaskOnMainThreadException
import co.infinum.goldeneye.extensions.hasCameraPermission

internal object Intrinsics {

    @Throws(MissingCameraPermissionException::class)
    fun checkCameraPermission(context: Context) {
        if (context.hasCameraPermission().not()) {
            throw MissingCameraPermissionException
        }
    }

    fun checkMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw TaskOnMainThreadException
        }
    }
}