package co.infinum.goldeneye.extensions

import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.models.FlashMode

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun CaptureRequest.Builder?.copyParamsFrom(other: CaptureRequest.Builder?) {
    if (other == null || this == null) {
        return
    }

    set(CaptureRequest.CONTROL_AF_MODE, other[CaptureRequest.CONTROL_AF_MODE])
    set(CaptureRequest.CONTROL_EFFECT_MODE, other[CaptureRequest.CONTROL_EFFECT_MODE])
    set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, other[CaptureRequest.CONTROL_AE_ANTIBANDING_MODE])
    set(CaptureRequest.CONTROL_SCENE_MODE, other[CaptureRequest.CONTROL_SCENE_MODE])
    set(CaptureRequest.CONTROL_AWB_MODE, other[CaptureRequest.CONTROL_AWB_MODE])
    set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, other[CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE])
    set(CaptureRequest.SCALER_CROP_REGION, other[CaptureRequest.SCALER_CROP_REGION])
    set(CaptureRequest.CONTROL_AE_MODE, other[CaptureRequest.CONTROL_AE_MODE])
    set(CaptureRequest.FLASH_MODE, other[CaptureRequest.FLASH_MODE])
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun CaptureRequest.Builder?.applyConfig(config: CameraConfig?) {
    if (this == null || config == null) {
        return
    }

    set(CaptureRequest.CONTROL_AF_MODE, config.focusMode.toCamera2())
    set(CaptureRequest.CONTROL_EFFECT_MODE, config.colorEffect.toCamera2())
    set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, config.antibanding.toCamera2())
    set(CaptureRequest.CONTROL_SCENE_MODE, config.sceneMode.toCamera2())
    set(CaptureRequest.CONTROL_AWB_MODE, config.whiteBalance.toCamera2())
    set(
        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
        if (config.videoStabilizationEnabled) {
            CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
        } else {
            CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF
        }
    )

    if (config.flashMode == FlashMode.TORCH) {
        set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
        set(CaptureRequest.FLASH_MODE, config.flashMode.toCamera2())
    } else {
        set(CaptureRequest.CONTROL_AE_MODE, config.flashMode.toCamera2())
        set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
    }
}