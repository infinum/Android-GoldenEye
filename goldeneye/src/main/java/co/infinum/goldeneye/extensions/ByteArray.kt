package co.infinum.goldeneye.extensions

import android.graphics.BitmapFactory

internal fun ByteArray.toBitmap() =
    try {
        BitmapFactory.decodeByteArray(this, 0, size)
    } catch (t: Throwable) {
        null
    }