package co.infinum.goldeneye.utils

import co.infinum.goldeneye.Logger

internal object LogDelegate {

    var logger: Logger? = null

    fun log(message: String) = logger?.log(message)
    fun log(message: String, t: Throwable) {
        logger?.log(message)
        logger?.log(t)
    }
}