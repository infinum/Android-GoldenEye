package co.infinum.goldeneye.extensions

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

internal val backgroundExecutor = Executors.newSingleThreadExecutor()
internal val mainHandler = Handler(Looper.getMainLooper())

internal fun <T1, T2> ifNotNull(p1: T1?, p2: T2?, action: (T1, T2) -> Unit) {
    if (p1 != null && p2 != null) {
        action(p1, p2)
    }
}

internal fun <T1, T2, T3> ifNotNull(p1: T1?, p2: T2?, p3: T3?, action: (T1, T2, T3) -> Unit) {
    if (p1 != null && p2 != null && p3 != null) {
        action(p1, p2, p3)
    }
}

internal fun <T> async(task: () -> T?, onResult: (T?) -> Unit) {
    backgroundExecutor.execute {
        val result = task()
        mainHandler.post { onResult(result) }
    }
}