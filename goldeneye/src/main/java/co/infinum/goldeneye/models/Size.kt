@file:Suppress("DEPRECATION")

package co.infinum.goldeneye.models

import android.hardware.Camera
import android.os.Build
import android.support.annotation.RequiresApi

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

    val aspectRatio = if (this.height != 0 && this.width != 0) this.width.toFloat() / this.height.toFloat() else -1f
    fun isOver1080p() = this.width > 1080 && this.height > 1080
}

internal fun Camera.Size.toInternalSize() = Size(width, height)

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal fun android.util.Size.toInternalSize() = Size(width, height)