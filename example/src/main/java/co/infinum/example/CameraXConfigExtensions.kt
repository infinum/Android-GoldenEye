package co.infinum.example

import android.content.Context
import android.util.Size
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageCapture
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

val FOCUS_MODE_AUTO = 0
val FOCUS_MODE_TAP = 1
val FOCUS_MODE_OFF = 2

fun suportedPictureSizes() = listOf<Size?>(
    null,
    Size(8000, 6000),
    Size(4000, 3000),
    Size(4000, 2250),
    Size(3840, 2160),
    Size(1440, 1440)
)

fun supportedFlashModes() = listOf<Int>(ImageCapture.FLASH_MODE_AUTO, ImageCapture.FLASH_MODE_ON, ImageCapture.FLASH_MODE_OFF)

fun supportedFocusModes() = listOf<Int>(FOCUS_MODE_AUTO, FOCUS_MODE_TAP, FOCUS_MODE_OFF)

fun Size.convertToString() = "$width x $height"
fun flashModeToString(flashMode: Int) = when (flashMode) {
    ImageCapture.FLASH_MODE_AUTO -> "auto"
    ImageCapture.FLASH_MODE_ON -> "on"
    else -> "off"
}

fun Int.aspectRatioToString() = if (this == 1) "16x9" else "4x3"

fun View.afterMeasured(block: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                block()
            }
        }
    })
}

data class CameraXConfig(
    val previewSize: Size = Size(1440, 1080),
    @AspectRatio.Ratio var aspectRatio: Int = AspectRatio.RATIO_16_9,
    var pictureSize: Size? = null,
//    var videoFrameRate: Int? = null,
//    var videoStabilicationEnabled: Boolean = false,
    var pinchToZoomEnabled: Boolean = true,
    @ImageCapture.FlashMode var flashMode: Int = ImageCapture.FLASH_MODE_AUTO,
    var focusMode: Int = FOCUS_MODE_AUTO
)

fun ImageCapture.Builder.applyConfigs(
    @ImageCapture.FlashMode flashMode: Int? = null,
    @AspectRatio.Ratio aspectRatio: Int? = null
) {
    apply {
        flashMode?.let { setFlashMode(it) }
        aspectRatio?.let { setTargetAspectRatio(it) }
    }
}

fun CameraXConfig.prepareItems(context: Context, adapter: SettingsAdapter) {
    val settingsItems = listOf(
        SettingsItem(name = "Basic features", type = 1),
        // TODO preview size is screen size
        SettingsItem("Preview size:", "YYYY x YYYY") {
            context.toast("Preview scale is automatic so it picks preview size on its own.")
        },
        SettingsItem("Picture size:", pictureSize?.convertToString() ?: "null") {
            displayDialog(
                context = context,
                config = this,
                settingsAdapter = adapter,
                title = "Picture size",
                listItems = suportedPictureSizes().map { ListItem(it, it?.convertToString() ?: "null") },
                onClick = {
                    pictureSize = it
                }
            )
        },
        SettingsItem("Preview scale", aspectRatio.aspectRatioToString()) {
            displayDialog(
                context = context,
                config = this,
                settingsAdapter = adapter,
                title = "Preview size",
                listItems = listOf(
                    ListItem(AspectRatio.RATIO_16_9, AspectRatio.RATIO_16_9.aspectRatioToString()),
                    ListItem(AspectRatio.RATIO_4_3, AspectRatio.RATIO_4_3.aspectRatioToString())
                ),
                onClick = {
                    aspectRatio = it
                }
            )
        },
//        SettingsItem("Video quality:", videoQuality.convertToString()) {
//            displayDialog(
//                context = context,
//                config = this,
//                settingsAdapter = adapter,
//                title = "Video quality",
//                listItems = supportedVideoQualities.map { ListItem(it, it.convertToString()) },
//                onClick = { videoQuality = it }
//            )
//        },
//        SettingsItem("Video stabilization:", videoStabilizationEnabled.convertToString()) {
//            if (isVideoStabilizationSupported) {
//                displayDialog(
//                    context = context,
//                    config = this,
//                    settingsAdapter = adapter,
//                    title = "Video stabilization",
//                    listItems = boolList(),
//                    onClick = { videoStabilizationEnabled = it }
//                )
//            } else {
//                context.toast("Video stabilization not supported")
//            }
//        },
        SettingsItem("Flash mode:", flashModeToString(flashMode)) {
            displayDialog(
                context = context,
                config = this,
                settingsAdapter = adapter,
                title = "Flash mode",
                listItems = supportedFlashModes().map { ListItem(it, flashModeToString(it)) },
                onClick = {
                    flashMode = it
                }
            )
        },
        SettingsItem("Focus mode:", focusMode.toString()) {
            displayDialog(
                context = context,
                config = this,
                settingsAdapter = adapter,
                title = "Focus mode",
                listItems = supportedFocusModes().map { ListItem(it, it.toString()) },
                onClick = { focusMode = it }
            )
        },
//        SettingsItem("Tap to focus - reset focus delay:", tapToFocusResetDelay.toString()) {
//            if (isTapToFocusSupported) {
//                displayDialog(
//                    context = context,
//                    config = this,
//                    settingsAdapter = adapter,
//                    title = "Reset delay",
//                    listItems = listOf(
//                        ListItem(2_500L, "2500"),
//                        ListItem(5_000L, "5000"),
//                        ListItem(7_500L, "7500")
//                    ),
//                    onClick = { tapToFocusResetDelay = it }
//                )
//            } else {
//                context.toast("Tap to focus not supported.")
//            }
//        },
        SettingsItem("Pinch to zoom:", pinchToZoomEnabled.toString()) {
            displayDialog(
                context = context,
                config = this,
                settingsAdapter = adapter,
                title = "Pinch to zoom",
                listItems = boolList(),
                onClick = { pinchToZoomEnabled = it }
            )
        }
//        SettingsItem(name = "Advanced features", type = 1),
//        SettingsItem("White Balance:", whiteBalanceMode.convertToString()) {
//            displayDialog(
//                context = context,
//                config = this,
//                settingsAdapter = adapter,
//                title = "White Balance",
//                listItems = supportedWhiteBalanceModes.map { ListItem(it, it.convertToString()) },
//                onClick = { whiteBalanceMode = it }
//            )
//        },
//        SettingsItem("Color Effect:", colorEffectMode.convertToString()) {
//            displayDialog(
//                context = context,
//                config = this,
//                settingsAdapter = adapter,
//                title = "Color Effect",
//                listItems = supportedColorEffectModes.map { ListItem(it, it.convertToString()) },
//                onClick = { colorEffectMode = it }
//            )
//        },
//        SettingsItem("Antibanding:", antibandingMode.convertToString()) {
//            displayDialog(
//                context = context,
//                config = this,
//                settingsAdapter = adapter,
//                title = "Antibanding",
//                listItems = supportedAntibandingModes.map { ListItem(it, it.convertToString()) },
//                onClick = { antibandingMode = it }
//            )
//        }
    )
    adapter.updateDataSet(settingsItems)
}

fun <T> displayDialog(
    context: Context, config: CameraXConfig, settingsAdapter: SettingsAdapter,
    title: String, listItems: List<ListItem<T>>, onClick: (T) -> Unit
) {
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
            config.prepareItems(context, settingsAdapter)
        }
    }
}