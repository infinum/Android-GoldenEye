package co.infinum.goldeneye.extensions

import android.view.View

internal fun View.isMeasured() = height > 0 && width > 0
internal fun View.isNotMeasured() = isMeasured().not()