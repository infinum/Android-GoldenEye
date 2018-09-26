package co.infinum.goldeneye.extensions

import android.graphics.Matrix

internal fun Matrix.rotate(degrees: Float, cx: Float, cy: Float) = apply { setRotate(degrees, cx, cy) }

internal fun Matrix.mirror() = apply { postScale(-1f, 1f) }