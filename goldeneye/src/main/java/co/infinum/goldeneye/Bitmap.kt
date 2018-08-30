@file:JvmName("BitmapUtils")

package co.infinum.goldeneye

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * Crops original bitmap to reduce it to given size from center.
 *
 * @throws TaskOnMainThreadException if this method is called from Main thread.
 */
fun Bitmap.crop(size: Size): Bitmap {
    Intrinsics.checkMainThread()
    val x = if (size.width < width) (width - size.width) / 2 else 0
    val y = if (size.height < height) (height - size.height) / 2 else 0
    if (x == 0 && y == 0) {
        return this
    }

    return Bitmap.createBitmap(this, x, y, size.width, size.height)
}

/**
 * Rotates original bitmap for given degrees.
 *
 * @throws TaskOnMainThreadException if this method is called from Main thread.
 */
fun Bitmap.rotate(degrees: Int): Bitmap {
    Intrinsics.checkMainThread()
    val matrix = Matrix().apply {
        setRotate(degrees.toFloat(), width / 2f, height / 2f)
    }

    return applyMatrix(matrix)
}

/**
 * Every Camera has its own rotation that is not the same as
 * device orientation. For that reason, Images returned in
 * ImageCallback can be rotated. This method will reverse
 * that rotation and returned Bitmap will have same orientation
 * as device.
 *
 * @throws TaskOnMainThreadException if this method is called from Main thread.
 */
fun Bitmap.reverseCameraRotation(activity: Activity, config: CameraConfig): Bitmap {
    val cameraRotation = CameraUtils.calculateDisplayOrientation(activity, config)
    return if (config.facing == Facing.BACK) rotate(cameraRotation) else rotate(-cameraRotation)
}

/**
 * Mirror original bitmap vertically. Can be useful for front camera images.
 *
 * @throws TaskOnMainThreadException if this method is called from Main thread.
 */
fun Bitmap.mirror(): Bitmap {
    Intrinsics.checkMainThread()
    val matrix = Matrix().apply {
        setScale(-1f, 1f)
    }

    return applyMatrix(matrix)
}

private fun Bitmap.applyMatrix(matrix: Matrix) = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)