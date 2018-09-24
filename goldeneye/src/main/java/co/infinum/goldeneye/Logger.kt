package co.infinum.goldeneye

/**
 * Logging interface that must be implemented in case you want
 * to see GoldenEye's logs.
 *
 * Logging is disabled by default.
 */
interface Logger {
    fun log(message: String)
    fun log(t: Throwable)
}