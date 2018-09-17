package co.infinum.goldeneye.config

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

    var whiteBalance: WhiteBalance
    val supportedWhiteBalance: List<WhiteBalance>

    var sceneMode: SceneMode
    val supportedSceneModes: List<SceneMode>

    var colorEffect: ColorEffect
    val supportedColorEffects: List<ColorEffect>

    var antibanding: Antibanding
    val supportedAntibanding: List<Antibanding>
}

internal abstract class BaseFeatureConfig<T>(
    private val onUpdateListener: (CameraProperty) -> Unit
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
                onUpdateListener(CameraProperty.FLASH)
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
                onUpdateListener(CameraProperty.FOCUS)
            } else {
                LogDelegate.log("Unsupported FocusMode [$value]")
            }
        }

    override var whiteBalance = WhiteBalance.UNKNOWN
        get() = when {
            field != WhiteBalance.UNKNOWN -> field
            supportedWhiteBalance.contains(WhiteBalance.AUTO) -> WhiteBalance.AUTO
            supportedWhiteBalance.contains(WhiteBalance.OFF) -> WhiteBalance.OFF
            else -> WhiteBalance.UNKNOWN
        }
        set(value) {
            if (supportedWhiteBalance.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.WHITE_BALANCE)
            } else {
                LogDelegate.log("Unsupported WhiteBalance [$value]")
            }
        }

    override var sceneMode = SceneMode.UNKNOWN
        get() = when {
            field != SceneMode.UNKNOWN -> field
            supportedSceneModes.contains(SceneMode.AUTO) -> SceneMode.AUTO
            supportedSceneModes.contains(SceneMode.OFF) -> SceneMode.OFF
            else -> SceneMode.UNKNOWN
        }
        set(value) {
            if (supportedSceneModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.SCENE_MODE)
            } else {
                LogDelegate.log("Unsupported SceneMode [$value]")
            }
        }

    override var colorEffect = ColorEffect.UNKNOWN
        get() = when {
            field != ColorEffect.UNKNOWN -> field
            supportedColorEffects.contains(ColorEffect.NONE) -> ColorEffect.NONE
            else -> ColorEffect.UNKNOWN
        }
        set(value) {
            if (supportedColorEffects.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.COLOR_EFFECT)
            } else {
                LogDelegate.log("Unsupported ColorEffect [$value]")
            }
        }

    override var antibanding = Antibanding.UNKNOWN
        get() = when {
            field != Antibanding.UNKNOWN -> field
            supportedAntibanding.contains(Antibanding.AUTO) -> Antibanding.AUTO
            supportedAntibanding.contains(Antibanding.OFF) -> Antibanding.OFF
            else -> Antibanding.UNKNOWN
        }
        set(value) {
            if (supportedAntibanding.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.ANTIBANDING)
            } else {
                LogDelegate.log("Unsupported Antibanding [$value]")
            }
        }
}