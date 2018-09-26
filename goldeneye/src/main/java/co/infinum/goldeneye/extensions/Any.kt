package co.infinum.goldeneye.extensions

import android.os.Handler
import android.os.Looper
import co.infinum.goldeneye.utils.AsyncUtils

internal val MAIN_HANDLER = Handler(Looper.getMainLooper())

internal fun <T1, T2> ifNotNull(p1: T1?, p2: T2?, action: (T1, T2) -> Unit) {
    if (p1 != null && p2 != null) {
        action(p1, p2)
    }
}

internal fun <T> async(task: () -> T?, onResult: (T?) -> Unit) {
    AsyncUtils.backgroundHandler.post {
        val result = task()
        MAIN_HANDLER.post { onResult(result) }
    }
}