package co.infinum.goldeneye.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

internal fun Context.hasCameraPermission() = hasPermission(Manifest.permission.CAMERA)
internal fun Context.hasAudioPermission() = hasPermission(Manifest.permission.RECORD_AUDIO)

private fun Context.hasPermission(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED