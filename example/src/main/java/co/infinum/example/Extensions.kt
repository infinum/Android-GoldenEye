package co.infinum.example

import co.infinum.goldeneye.models.FlashMode
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size

fun FocusMode.convertToString() = name.toLowerCase()
fun FlashMode.convertToString() = name.toLowerCase()
fun PreviewScale.convertToString() = name.toLowerCase()
fun Size.convertToString() = "$width x $height"