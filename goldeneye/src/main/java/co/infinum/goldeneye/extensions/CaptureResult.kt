@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package co.infinum.goldeneye.extensions

import android.hardware.camera2.CaptureResult
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.utils.LogDelegate

fun CaptureResult?.isExposureReady(): Boolean {
    if (this == null) return false

    val aeMode = get(CaptureResult.CONTROL_AE_MODE)
    val aeState = get(CaptureResult.CONTROL_AE_STATE)

    LogDelegate.log(aeState?.toReadable2() ?: "NULL")
    return aeMode == CaptureResult.CONTROL_AE_MODE_OFF
        || aeState == null
        || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
        || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
        || aeState == CaptureResult.CONTROL_AE_STATE_LOCKED
}

fun CaptureResult?.isFocusReady(): Boolean {
    if (this == null) return false

    val afMode = get(CaptureResult.CONTROL_AF_MODE)
    val afState = get(CaptureResult.CONTROL_AF_STATE)

    LogDelegate.log(afState?.toReadable() ?: "NULL")
    return afMode == CaptureResult.CONTROL_AF_MODE_OFF
        || afState == null
        || afState == CaptureResult.CONTROL_AF_STATE_INACTIVE
        || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
        || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
        || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED
        || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED
}

private fun Int.toReadable2(): String {
    return when (this) {
        CaptureResult.CONTROL_AE_STATE_SEARCHING -> "SEARCHING"
        CaptureResult.CONTROL_AE_STATE_CONVERGED -> "CONVERGED"
        CaptureResult.CONTROL_AE_STATE_PRECAPTURE -> "PRECAPTURE"
        CaptureResult.CONTROL_AE_STATE_LOCKED -> "LOCKED"
        CaptureResult.CONTROL_AE_STATE_INACTIVE -> "INACTIVE"
        CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED -> "FLASH_REQUIRED"
        else -> "WTF"
    }
}

private fun Int.toReadable(): String {
    return when (this) {
        CaptureResult.CONTROL_AF_STATE_INACTIVE -> "INACTIVE"
        CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN -> "ACTIVE_SCAN"
        CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED -> "FOCUSED_LOCKED"
        CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> "NOT_FOCUSED_LOCKED"
        CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED -> "PASSIVE_FOCUSED"
        CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN -> "PASSIVE_SCAN"
        CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED -> "PASSIVE_UNFOCUSED"
        else -> "WTF"
    }
}
