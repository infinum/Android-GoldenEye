package co.infinum.goldeneye

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

internal object PictureFactory {
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun createBitmap(byteArray: ByteArray, onBitmapCreated: (Bitmap?) -> Unit) {
        executor.submit {
            val bitmap =
                try {
                    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                } catch (t: Throwable) {
                    LogDelegate.log(t)
                    null
                }

            mainHandler.post { onBitmapCreated(bitmap) }
        }
    }
}