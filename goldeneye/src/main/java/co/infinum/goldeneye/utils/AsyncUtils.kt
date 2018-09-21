package co.infinum.goldeneye.utils

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.ThreadNotStartedException

@Suppress("ObjectPropertyName")
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
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
            backgroundThread?.quitSafely()
            backgroundThread?.join()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        } finally {
            _backgroundHandler = null
            backgroundThread = null
        }
    }
}