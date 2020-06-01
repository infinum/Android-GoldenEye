package co.infinum.goldeneye.utils

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.RequiresApi
import co.infinum.goldeneye.ThreadNotStartedException

/**
 * Background thread handler.
 */
@Suppress("ObjectPropertyName")
internal object AsyncUtils {

    private var backgroundThread: HandlerThread? = null

    private var _backgroundHandler: Handler? = null
    val backgroundHandler: Handler
        get() = _backgroundHandler ?: throw ThreadNotStartedException

    fun startBackgroundThread() {
        if (_backgroundHandler != null) return

        backgroundThread = HandlerThread("GoldenEye")
        backgroundThread?.start()
        _backgroundHandler = Handler(backgroundThread?.looper)
    }

    fun stopBackgroundThread() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                backgroundThread?.quitSafely()
            } else {
                backgroundThread?.quit()
            }
            backgroundThread?.join()
        } catch (t: Throwable) {
            LogDelegate.log("Failed to stop background threads." ,t)
        } finally {
            _backgroundHandler = null
            backgroundThread = null
        }
    }
}