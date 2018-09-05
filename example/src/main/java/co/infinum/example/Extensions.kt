package co.infinum.example

import co.infinum.goldeneye.models.*

fun FocusMode.convertToString() = name.toLowerCase()
fun FlashMode.convertToString() = name.toLowerCase()
fun PreviewScale.convertToString() = name.toLowerCase()
fun WhiteBalance.convertToString() = name.toLowerCase()
fun Size.convertToString() = "$width x $height"
fun Int.toPercentage() = "%.02fx".format(this / 100f)
fun Boolean.convertToString() = if (this) "Enabled" else "Disabled"
fun Antibanding.convertToString() = name.toLowerCase()
fun ColorEffect.convertToString() = name.toLowerCase()
fun SceneMode.convertToString() = name.toLowerCase()
fun Float.convertToString() = "%.02f".format(this)
fun VideoQuality.convertToString() = name.toLowerCase()
fun boolList() = listOf(
    ListItem(true, "Enabled"),
    ListItem(false, "Disabled")
)