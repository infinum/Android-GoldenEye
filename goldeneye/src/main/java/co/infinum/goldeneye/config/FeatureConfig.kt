package co.infinum.goldeneye.config

import co.infinum.goldeneye.BaseGoldenEyeImpl
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.LogDelegate

interface FeatureConfig {
    var tapToFocusEnabled: Boolean
    val isTapToFocusSupported: Boolean
    var resetFocusDelay: Long

    var flashMode: FlashMode
    val supportedFlashModes: List<FlashMode>

    var focusMode: FocusMode
    val supportedFocusModes: List<FocusMode>

    var whiteBalanceMode: WhiteBalanceMode
    val supportedWhiteBalanceModes: List<WhiteBalanceMode>

    var colorEffectMode: ColorEffectMode
    val supportedColorEffectModes: List<ColorEffectMode>

    var antibandingMode: AntibandingMode
    val supportedAntibandingModes: List<AntibandingMode>

    var jpegQuality: Int
}

internal abstract class BaseFeatureConfig<T>(
    private val onUpdateCallback: (CameraProperty) -> Unit
) : FeatureConfig {

    var characteristics: T? = null

    override var tapToFocusEnabled = true
        get() = field && isTapToFocusSupported
        set(value) {
            if (isTapToFocusSupported) {
                field = value
            } else {
                LogDelegate.log("Unsupported Tap to focus.")
            }
        }

    override var resetFocusDelay = 7_500L
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