@file:JvmName("BitmapUtils")

package co.infinum.goldeneye.extensions

import android.graphics.Bitmap
import android.graphics.Matrix
import co.infinum.goldeneye.TaskOnMainThreadException
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.utils.Intrinsics

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

fun Bitmap.mirror() = mutate { mirror() }

fun Bitmap.rotate(degrees: Float) = mutate { rotate(degrees, width / 2f, height / 2f) }

internal fun Bitmap.mutate(updateMatrix: Matrix.() -> Unit): Bitmap {
    Intrinsics.checkMainThread()
    val newBitmap = Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply(updateMatrix), true)
    safeRecycle(newBitmap)
    return newBitmap
}

internal fun Bitmap.safeRecycle(newBitmap: Bitmap) {
    if (this != newBitmap) {
        recycle()
    }
}