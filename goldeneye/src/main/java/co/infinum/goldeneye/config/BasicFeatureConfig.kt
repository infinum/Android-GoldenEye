package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.LogDelegate

interface BasicFeatureConfig {

    /**
     * Returns current JPEG quality. The value must be Integer between 1 and 100 (inclusive).
     * It is dynamically applied when capture is triggered.
     *
     * Default value is 100.
     */
    var pictureQuality: Int

    /**
     * Tap to focus toggle. [co.infinum.goldeneye.OnFocusChangedCallback] is triggered
     * every time focus change happens.
     *
     * If tap to focus is supported, default value is true, otherwise false.
     *
     * @see co.infinum.goldeneye.OnFocusChangedCallback
     */
    var tapToFocusEnabled: Boolean

    /**
     * Return whether Tap to focus is even supported for current camera.
     *
     * Some cameras do not support [FocusMode.AUTO], so tap to focus
     * is not supported for those devices.
     */
    val isTapToFocusSupported: Boolean

    /**
     * While [FocusMode.CONTINUOUS_PICTURE] or [FocusMode.CONTINUOUS_VIDEO] is active, tap to focus
     * must hijack its focus. After [tapToFocusResetDelay] milliseconds, Focus handling
     * will be given back to Camera.
     *
     * Delay is measured in milliseconds!
     *
     * Default value is 7_500.
     */
    var tapToFocusResetDelay: Long

    /**
     * Returns currently active Flash mode.
     * When new Flash mode is set, it is automatically updated on current camera.
     *
     * Default value is the first supported mode of [FlashMode.AUTO], [FlashMode.OFF].
     * If none is supported [FlashMode.UNKNOWN] is used.
     *
     * @see FlashMode
     */
    var flashMode: FlashMode

    /**
     * Returns list of available Flash modes. In case Flash is not supported,
     * empty list will be returned.
     *
     * @see FlashMode
     */
    val supportedFlashModes: List<FlashMode>

    /**
     * Returns currently active Focus mode.
     * When new Focus mode is set, it is automatically updated on current camera.
     *
     * Default value is the first supported mode of [FocusMode.CONTINUOUS_PICTURE], [FocusMode.AUTO].
     * If none is supported [FocusMode.UNKNOWN] is used.
     *
     * @see FocusMode
     */
    var focusMode: FocusMode

    /**
     * Returns list of available Focus modes. In case Focus is not supported,
     * empty list will be returned.
     *
     * @see FocusMode
     */
    val supportedFocusModes: List<FocusMode>
}

internal abstract class BaseBasicFeatureConfig<T : Any>(
    private val onUpdateCallback: (CameraProperty) -> Unit
) : BasicFeatureConfig {

    lateinit var characteristics: T

    override var tapToFocusEnabled = true
        get() = field && isTapToFocusSupported
        set(value) {
            if (isTapToFocusSupported) {
                field = value
            } else {
                LogDelegate.log("Unsupported Tap to focus.")
            }
        }

    override var tapToFocusResetDelay = 7_500L
        set(value) {
            if (value > 0) {
                field = value
            } else {
                LogDelegate.log("Reset focus delay must be bigger than 0.")
            }
        }

    override var pictureQuality = 100
        set(value) {
            field = value.coerceIn(1, 100)
        }

    override var flashMode = FlashMode.UNKNOWN
        get() = when {
            field != FlashMode.UNKNOWN -> field
            supportedFlashModes.contains(FlashMode.AUTO) -> FlashMode.AUTO
            supportedFlashModes.contains(FlashMode.OFF) -> FlashMode.OFF
            else -> FlashMode.UNKNOWN
        }
        set(value) {
            if (supportedFlashModes.contains(value)) {
                field = value
                onUpdateCallback(CameraProperty.FLASH)
            } else {
                LogDelegate.log("Unsupported FlashMode [$value]")
            }
        }

    override var focusMode = FocusMode.UNKNOWN
        get() = when {
            field != FocusMode.UNKNOWN -> field
            supportedFocusModes.contains(FocusMode.CONTINUOUS_PICTURE) -> FocusMode.CONTINUOUS_PICTURE
            supportedFocusModes.contains(FocusMode.AUTO) -> FocusMode.AUTO
            else -> FocusMode.UNKNOWN
        }
        set(value) {
            if (supportedFocusModes.contains(value)) {
                field = value
                onUpdateCallback(CameraProperty.FOCUS)
            } else {
                LogDelegate.log("Unsupported FocusMode [$value]")
            }
        }
}