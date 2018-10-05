package co.infinum.example

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.models.*

fun FocusMode.convertToString() = name.toLowerCase()
fun FlashMode.convertToString() = name.toLowerCase()
fun PreviewScale.convertToString() = name.toLowerCase()
fun WhiteBalanceMode.convertToString() = name.toLowerCase()
fun Size.convertToString() = "$width x $height"
fun Int.toPercentage() = "%.02fx".format(this / 100f)
fun Boolean.convertToString() = if (this) "Enabled" else "Disabled"
fun AntibandingMode.convertToString() = name.toLowerCase()
fun ColorEffectMode.convertToString() = name.toLowerCase()
fun VideoQuality.convertToString() = name.toLowerCase()
fun boolList() = listOf(
    ListItem(true, "Enabled"),
    ListItem(false, "Disabled")
)

fun Context.toast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

fun CameraConfig.prepareItems(context: Context): List<SettingsItem> {
    return listOf(
        SettingsItem("Preview size:", previewSize.convertToString()) {
            if (previewScale == PreviewScale.MANUAL
                || previewScale == PreviewScale.MANUAL_FIT
                || previewScale == PreviewScale.MANUAL_FILL
            ) {
                displayDialog(
                    context = context,
                    config = this,
                    title = "Preview size",
                    listItems = supportedPreviewSizes.map { ListItem(it, it.convertToString()) },
                    onClick = { previewSize = it }
                )
            } else {
                context.toast("Preview scale is automatic so it picks preview size on its own.")
            }
        },
        SettingsItem("Picture size:", pictureSize.convertToString()) {
            displayDialog(
                context = context,
                config = this,
                title = "Picture size",
                listItems = supportedPictureSizes.map { ListItem(it, it.convertToString()) },
                onClick = { pictureSize = it }
            )
        },
        SettingsItem("Preview scale", previewScale.convertToString()) {
            displayDialog(
                context = context,
                config = this,
                title = "Preview scale",
                listItems = PreviewScale.values().map { ListItem(it, it.convertToString()) },
                onClick = { previewScale = it }
            )
        },
        SettingsItem("Video quality:", videoQuality.convertToString()) {
            displayDialog(
                context = context,
                config = this,
                title = "Video quality",
                listItems = supportedVideoQualities.map { ListItem(it, it.convertToString()) },
                onClick = { videoQuality = it }
            )
        },
        SettingsItem("Video stabilization:", videoStabilizationEnabled.convertToString()) {
            if (isVideoStabilizationSupported) {
                displayDialog(
                    context = context,
                    config = this,
                    title = "Video stabilization",
                    listItems = boolList(),
                    onClick = { videoStabilizationEnabled = it }
                )
            } else {
                context.toast("Video stabilization not supported")
            }
        },
        SettingsItem("Flash mode:", flashMode.convertToString()) {
            displayDialog(
                context = context,
                config = this,
                title = "Flash mode",
                listItems = supportedFlashModes.map { ListItem(it, it.convertToString()) },
                onClick = { flashMode = it }
            )
        },
        SettingsItem("Focus mode:", focusMode.convertToString()) {
            displayDialog(
                context = context,
                config = this,
                title = "Focus mode",
                listItems = supportedFocusModes.map { ListItem(it, it.convertToString()) },
                onClick = { focusMode = it }
            )
        },
        SettingsItem("White Balance:", whiteBalanceMode.convertToString()) {
            displayDialog(
                context = context,
                config = this,
                title = "White Balance",
                listItems = supportedWhiteBalanceModes.map { ListItem(it, it.convertToString()) },
                onClick = { whiteBalanceMode = it }
            )
        },
        SettingsItem("Color Effect:", colorEffectMode.convertToString()) {
            displayDialog(
                context = context,
                config = this,
                title = "Color Effect",
                listItems = supportedColorEffectModes.map { ListItem(it, it.convertToString()) },
                onClick = { colorEffectMode = it }
            )
        },
        SettingsItem("Antibanding:", antibandingMode.convertToString()) {
            displayDialog(
                context = context,
                config = this,
                title = "Antibanding",
                listItems = supportedAntibandingModes.map { ListItem(it, it.convertToString()) },
                onClick = { antibandingMode = it }
            )
        },
        SettingsItem("Tap to focus:", tapToFocusEnabled.convertToString()) {
            if (isTapToFocusSupported) {
                displayDialog(
                    context = context,
                    config = this,
                    title = "Tap to focus",
                    listItems = boolList(),
                    onClick = { tapToFocusEnabled = it }
                )
            } else {
                context.toast("Tap to focus not supported.")
            }
        },
        SettingsItem("Tap to focus - reset focus delay:", tapToFocusResetDelay.toString()) {
            if (isTapToFocusSupported) {
                displayDialog(
                    context = context,
                    config = this,
                    title = "Reset delay",
                    listItems = listOf(
                        ListItem(2_500L, "2500"),
                        ListItem(5_000L, "5000"),
                        ListItem(7_500L, "7500")
                    ),
                    onClick = { tapToFocusResetDelay = it }
                )
            } else {
                context.toast("Tap to focus not supported.")
            }
        },
        SettingsItem("Pinch to zoom:", pinchToZoomEnabled.convertToString()) {
            if (isZoomSupported) {
                displayDialog(
                    context = context,
                    config = this,
                    title = "Pinch to zoom",
                    listItems = boolList(),
                    onClick = { pinchToZoomEnabled = it }
                )
            } else {
                context.toast("Pinch to zoom not supported.")
            }
        },
        SettingsItem("Pinch to zoom friction:", "%.02f".format(pinchToZoomFriction)) {
            if (isZoomSupported) {
                displayDialog(
                    context = context,
                    config = this,
                    title = "Friction",
                    listItems = listOf(
                        ListItem(0.5f, "0.50"),
                        ListItem(1f, "1.00"),
                        ListItem(2f, "2.00")
                    ),
                    onClick = { pinchToZoomFriction = it }
                )
            } else {
                context.toast("Pinch to zoom not supported.")
            }
        }
    )
}

fun <T> displayDialog(context: Context, config: CameraConfig, title: String, listItems: List<ListItem<T>>, onClick: (T) -> Unit) {
    if (listItems.isEmpty()) {
        context.toast("$title not supported")
        return
    }

    val dialog = AlertDialog.Builder(context)
        .setView(R.layout.list)
        .setCancelable(true)
        .setTitle(title)
        .show()

    dialog.findViewById<RecyclerView>(R.id.recyclerView)?.apply {
        layoutManager = LinearLayoutManager(context)
        adapter = ListItemAdapter(listItems) {
            onClick(it)
            dialog.dismiss()
            config.prepareItems(context)
        }
    }
}