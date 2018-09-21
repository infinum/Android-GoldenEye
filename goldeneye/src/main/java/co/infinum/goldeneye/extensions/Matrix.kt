package co.infinum.goldeneye.extensions

import android.app.Activity
import android.graphics.Matrix
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.utils.CameraUtils

internal fun Matrix.rotate(degrees: Float, cx: Float, cy: Float) = apply { setRotate(degrees, cx, cy) }

internal fun Matrix.mirror() = apply { postScale(-1f, 1f) }

internal fun Matrix.reverseCameraRotation(activity: Activity, info: CameraInfo, cx: Float, cy: Float): Matrix {
    val cameraRotation = CameraUtils.calculateDisplayOrientation(activity, info).toFloat()
    return rotate(if (info.facing == Facing.BACK) cameraRotation else -cameraRotation, cx, cy)
}