@file:JvmName("BitmapUtils")

package co.infinum.goldeneye

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.crop(size: Size): Bitmap {
    Intrinsics.checkMainThread()
    val x = if (size.width < width) (width - size.width) / 2 else 0
    val y = if (size.height < height) (height - size.height) / 2 else 0
    if (x == 0 && y == 0) {
        return this
    }

    return Bitmap.createBitmap(this, x, y, size.width, size.height)
}

fun Bitmap.rotate(degrees: Int): Bitmap {
    Intrinsics.checkMainThread()
    val matrix = Matrix().apply {
        setRotate(degrees.toFloat(), width / 2f, height / 2f)
    }

    return applyMatrix(matrix)
}

fun Bitmap.reverseCameraRotation(activity: Activity, config: CameraConfig): Bitmap {
    val cameraRotation = CameraUtils.calculateDisplayOrientation(activity, config)
    return if (config.facing == Facing.BACK) rotate(cameraRotation) else rotate(-cameraRotation)
}

fun Bitmap.mirror(): Bitmap {
    Intrinsics.checkMainThread()
    val matrix = Matrix().apply {
        setScale(-1f, 1f)
    }

    return applyMatrix(matrix)
}

private fun Bitmap.applyMatrix(matrix: Matrix) = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)