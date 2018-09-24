@file:JvmName("BitmapUtils")

package co.infinum.goldeneye.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.utils.Intrinsics

internal fun Bitmap.applyMatrix(configure: Matrix.() -> Unit): Bitmap {
    Intrinsics.checkMainThread()
    val newBitmap = Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply(configure), false)
    safeRecycle(newBitmap)
    return newBitmap
}

internal fun Bitmap.safeRecycle(newBitmap: Bitmap) {
    if (this != newBitmap) {
        recycle()
    }
}

internal fun ByteArray.toBitmap() =
    try {
        BitmapFactory.decodeByteArray(this, 0, size)
    } catch (t: Throwable) {
        null
    }

@RequiresApi(Build.VERSION_CODES.KITKAT)
internal fun Image.toBitmap(): Bitmap? {
    val buffer = planes[0].buffer
    val byteArray = ByteArray(buffer.remaining())
    buffer.get(byteArray)
    return byteArray.toBitmap()
}