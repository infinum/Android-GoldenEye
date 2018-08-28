package co.infinum.example

import co.infinum.goldeneye.*

fun FocusMode.convertToString() = name.toLowerCase()
fun FlashMode.convertToString() = name.toLowerCase()
fun PreviewType.convertToString() = name.toLowerCase()
fun PreviewScale.convertToString() = name.toLowerCase()
fun Size.convertToString() = "$width x $height"