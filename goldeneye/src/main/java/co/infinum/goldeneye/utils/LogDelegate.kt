package co.infinum.goldeneye.utils

import co.infinum.goldeneye.GoldenEye

internal object LogDelegate {

    var logger: GoldenEye.Logger? = null

    fun log(message: String) = logger?.log(message)
    fun log(t: Throwable) = logger?.log(t)
}