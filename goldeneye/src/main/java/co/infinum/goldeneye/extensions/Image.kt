package co.infinum.goldeneye.extensions

import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun Image.toBitmap(): Bitmap? {
    val buffer = planes[0].buffer
    val byteArray = ByteArray(buffer.remaining())
    buffer.get(byteArray)
    return byteArray.toBitmap()
}