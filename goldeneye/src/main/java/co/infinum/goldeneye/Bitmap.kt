@file:JvmName("BitmapUtils")

package co.infinum.goldeneye

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.crop(size: Size): Bitmap {
    val x = if (size.width < width) (width - size.width) / 2 else 0
    val y = if (size.height < height) (height - size.height) / 2 else 0
    if (x == 0 && y == 0) {
        return this
    }

    val newBitmap = Bitmap.createBitmap(this, x, y, size.width, size.height)
    recycle()
    return newBitmap
}

fun Bitmap.rotate(degrees: Int): Bitmap {
    val matrix = Matrix().apply {
        setRotate(degrees.toFloat(), width / 2f, height / 2f)
    }

    return applyMatrix(matrix)
}

fun Bitmap.mirror(): Bitmap {
    val matrix = Matrix().apply {
        setScale(-1f, 1f)
    }

    return applyMatrix(matrix)
}

private fun Bitmap.applyMatrix(matrix: Matrix): Bitmap {
    val newBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    recycle()
    return newBitmap
}