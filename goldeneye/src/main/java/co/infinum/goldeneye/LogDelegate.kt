package co.infinum.goldeneye

internal object LogDelegate {

    var logger: GoldenEye.Logger? = null

    fun log(message: String) = logger?.log(message)
    fun log(t: Throwable) = logger?.log(t)
}