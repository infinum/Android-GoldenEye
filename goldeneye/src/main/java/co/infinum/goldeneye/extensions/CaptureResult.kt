@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package co.infinum.goldeneye.extensions

import android.hardware.camera2.CaptureResult
import android.os.Build
import androidx.annotation.RequiresApi

fun CaptureResult?.isLocked() = isFocusReady() && isExposureReady()

private fun CaptureResult?.isExposureReady(): Boolean {
    if (this == null) return false

    val aeMode = get(CaptureResult.CONTROL_AE_MODE)
    val aeState = get(CaptureResult.CONTROL_AE_STATE)

    return aeMode == CaptureResult.CONTROL_AE_MODE_OFF
        || aeState == null
        || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
        || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
        || aeState == CaptureResult.CONTROL_AE_STATE_LOCKED
}

fun CaptureResult?.isFocusReady(): Boolean {
    if (this == null) return false

    val afState = get(CaptureResult.CONTROL_AF_STATE)
    val afMode = get(CaptureResult.CONTROL_AF_MODE)

    return afMode == CaptureResult.CONTROL_AF_MODE_OFF
        || afState == CaptureResult.CONTROL_AF_STATE_INACTIVE
        || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
        || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
        || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED
        || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED
}