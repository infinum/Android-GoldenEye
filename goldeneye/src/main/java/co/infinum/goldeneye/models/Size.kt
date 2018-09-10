@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.models

import android.hardware.Camera

data class Size internal constructor(
    val width: Int,
    val height: Int
) : Comparable<Size> {
    companion object {
        val UNKNOWN = Size(0, 0)
    }

    override fun compareTo(other: Size): Int {
        return other.height * other.width - width * height
    }

    val aspectRatio = if (this != Size.UNKNOWN) this.width / this.height else -1
}

internal fun Camera.Size.toInternalSize() = Size(width, height)