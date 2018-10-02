@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package co.infinum.goldeneye.extensions

import android.hardware.camera2.CaptureResult
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.utils.LogDelegate

fun CaptureResult.isLocked(): Boolean {
    val aeMode = get(CaptureResult.CONTROL_AE_MODE)
    val aeState = get(CaptureResult.CONTROL_AE_STATE)
    val afMode = get(CaptureResult.CONTROL_AF_MODE)
    val afState = get(CaptureResult.CONTROL_AF_STATE)
    val awbMode = get(CaptureResult.CONTROL_AWB_MODE)
    val awbState = get(CaptureResult.CONTROL_AWB_STATE)

    /* Wait for all states to be ready, if they are not ready repeat basic capture while camera is preparing for capture */
    LogDelegate.log("${isExposureReady(aeMode, aeState)}; ${isFocusReady(afMode, afState)} = $afState; ${isAwbReady(awbMode, awbState)}")
    return isExposureReady(aeMode, aeState) && isFocusReady(afMode, afState) && isAwbReady(awbMode, awbState)
}

private fun isAwbReady(awbMode: Int?, awbState: Int?) =
    awbMode != CaptureResult.CONTROL_AWB_MODE_AUTO
        || awbState == null
        || awbState == CaptureResult.CONTROL_AWB_STATE_CONVERGED
        || awbState == CaptureResult.CONTROL_AWB_STATE_LOCKED

private fun isExposureReady(aeMode: Int?, aeState: Int?) =
    aeMode == CaptureResult.CONTROL_AE_MODE_OFF
        || aeState == null
        || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
        || aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
        || aeState == CaptureResult.CONTROL_AE_STATE_LOCKED

private fun isFocusReady(afMode: Int?, afState: Int?) =
    afMode == CaptureResult.CONTROL_AF_MODE_OFF
        || afState == null
        || afState == CaptureResult.CONTROL_AF_STATE_INACTIVE
        || afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
        || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
        || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED
        || afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED