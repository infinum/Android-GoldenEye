package co.infinum.goldeneye

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

internal object Intrinsics {

    @Throws(MissingCameraPermissionException::class)
    fun checkCameraPermission(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw MissingCameraPermissionException
        }
    }
}