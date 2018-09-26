package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.LogDelegate

interface FeatureConfig {

    /**
     * Returns current JPEG quality. The value must be Integer between 1 and 100 (inclusive).
     * It is dynamically applied to Picture session when capture is triggered.
     *
     * Default value is 100.
     */
    var jpegQuality: Int

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

    /**
     * Returns currently active White balance mode.
     * When new White balance mode is set, it is automatically updated on current camera.
     *
     * Default value is the first supported mode of [WhiteBalanceMode.AUTO], [WhiteBalanceMode.OFF].
     * If none is supported [WhiteBalanceMode.UNKNOWN] is used.
     *
     * @see WhiteBalanceMode
     */
    var whiteBalanceMode: WhiteBalanceMode

    /**
     * Returns list of available White balance modes. In case White balance is not supported,
     * empty list will be returned.
     *
     * @see WhiteBalanceMode
     */
    val supportedWhiteBalanceModes: List<WhiteBalanceMode>

    /**
     * Returns currently active Color effect mode.
     * When new Color effect mode is set, it is automatically updated on current camera.
     *
     * Default value is [ColorEffectMode.NONE] if supported, otherwise [ColorEffectMode.UNKNOWN].
     *
     * @see ColorEffectMode
     */
    var colorEffectMode: ColorEffectMode

    /**
     * Returns list of available Color effect modes. In case Color effect is not supported,
     * empty list will be returned.
     *
     * @see ColorEffectMode
     */
    val supportedColorEffectModes: List<ColorEffectMode>

    /**
     * Returns currently active Antibanding mode.
     * When new Antibanding mode is set, it is automatically updated on current camera.
     *
     * Default value is the first supported mode of [AntibandingMode.AUTO], [AntibandingMode.OFF].
     * If none is supported [AntibandingMode.UNKNOWN] is used.
     *
     * @see AntibandingMode
     */
    var antibandingMode: AntibandingMode

    /**
     * Returns list of available Antibanding modes. In case Antibanding is not supported,
     * empty list will be returned.
     *
     * @see AntibandingMode
     */
    val supportedAntibandingModes: List<AntibandingMode>
}

internal abstract class BaseFeatureConfig<T : Any>(
    private val onUpdateCallback: (CameraProperty) -> Unit
) : FeatureConfig {

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

    override var jpegQuality = 100
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

    override var whiteBalanceMode = WhiteBalanceMode.UNKNOWN
        get() = when {
            field != WhiteBalanceMode.UNKNOWN -> field
            supportedWhiteBalanceModes.contains(WhiteBalanceMode.AUTO) -> WhiteBalanceMode.AUTO
            supportedWhiteBalanceModes.contains(WhiteBalanceMode.OFF) -> WhiteBalanceMode.OFF
            else -> WhiteBalanceMode.UNKNOWN
        }
        set(value) {
            if (supportedWhiteBalanceModes.contains(value)) {
                field = value
                onUpdateCallback(CameraProperty.WHITE_BALANCE)
            } else {
                LogDelegate.log("Unsupported WhiteBalance [$value]")
            }
        }

    override var colorEffectMode = ColorEffectMode.UNKNOWN
        get() = when {
            field != ColorEffectMode.UNKNOWN -> field
            supportedColorEffectModes.contains(ColorEffectMode.NONE) -> ColorEffectMode.NONE
            else -> ColorEffectMode.UNKNOWN
        }
        set(value) {
            if (supportedColorEffectModes.contains(value)) {
                field = value
                onUpdateCallback(CameraProperty.COLOR_EFFECT)
            } else {
                LogDelegate.log("Unsupported ColorEffect [$value]")
            }
        }

    override var antibandingMode = AntibandingMode.UNKNOWN
        get() = when {
            field != AntibandingMode.UNKNOWN -> field
            supportedAntibandingModes.contains(AntibandingMode.AUTO) -> AntibandingMode.AUTO
            supportedAntibandingModes.contains(AntibandingMode.OFF) -> AntibandingMode.OFF
            else -> AntibandingMode.UNKNOWN
        }
        set(value) {
            if (supportedAntibandingModes.contains(value)) {
                field = value
                onUpdateCallback(CameraProperty.ANTIBANDING)
            } else {
                LogDelegate.log("Unsupported Antibanding [$value]")
            }
        }
}