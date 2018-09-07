package co.infinum.goldeneye

interface Logger {
    fun log(message: String)
    fun log(t: Throwable)
}