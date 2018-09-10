package co.infinum.goldeneye.config

import android.hardware.Camera
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.LogDelegate

interface FeatureConfig {
    var tapToFocusEnabled: Boolean
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

    var exposureCompensation: Int
    val supportedExposureCompensation: List<Int>
    val isExposureCompensationSupported: Boolean
}

internal class FeatureConfigImpl(
    private val onUpdateListener: (CameraProperty) -> Unit
) : FeatureConfig {

    var params: Camera.Parameters? = null

    fun initialize() {
        this.flashMode = FlashMode.fromString(params?.flashMode)
        this.focusMode = FocusMode.fromString(params?.focusMode)
        this.whiteBalance = WhiteBalance.fromString(params?.whiteBalance)
        this.sceneMode = SceneMode.fromString(params?.sceneMode)
        this.colorEffect = ColorEffect.fromString(params?.colorEffect)
        this.antibanding = Antibanding.fromString(params?.antibanding)
    }

    override var tapToFocusEnabled = true
    override var resetFocusDelay = 7_500L
        set(value) {
            if (value > 0) {
                field = value
            } else {
                LogDelegate.log("Reset focus delay must be bigger than 0.")
            }
        }

    override var flashMode = FlashMode.UNKNOWN
        set(value) {
            if (supportedFlashModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.FLASH)
            } else {
                LogDelegate.log("Unsupported FlashMode [$value]")
            }
        }

    override val supportedFlashModes: List<FlashMode>
        get() = params?.supportedFlashModes
            ?.map { FlashMode.fromString(it) }
            ?.distinct()
            ?.filter { it != FlashMode.UNKNOWN }
            ?: emptyList()

    override var focusMode = FocusMode.UNKNOWN
        set(value) {
            if (supportedFocusModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.FOCUS)
            } else {
                LogDelegate.log("Unsupported FocusMode [$value]")
            }
        }

    override val supportedFocusModes: List<FocusMode>
        get() = params?.supportedFocusModes
            ?.map { FocusMode.fromString(it) }
            ?.distinct()
            ?.filter { it != FocusMode.UNKNOWN }
            ?: emptyList()

    override var whiteBalance = WhiteBalance.UNKNOWN
        set(value) {
            if (supportedWhiteBalance.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.WHITE_BALANCE)
            } else {
                LogDelegate.log("Unsupported WhiteBalance [$value]")
            }
        }

    override val supportedWhiteBalance: List<WhiteBalance>
        get() = params?.supportedWhiteBalance
            ?.map { WhiteBalance.fromString(it) }
            ?.distinct()
            ?.filter { it != WhiteBalance.UNKNOWN }
            ?: emptyList()

    override var sceneMode = SceneMode.UNKNOWN
        set(value) {
            if (supportedSceneModes.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.SCENE_MODE)
            } else {
                LogDelegate.log("Unsupported SceneMode [$value]")
            }
        }

    override val supportedSceneModes: List<SceneMode>
        get() = params?.supportedSceneModes
            ?.map { SceneMode.fromString(it) }
            ?.distinct()
            ?.filter { it != SceneMode.UNKNOWN }
            ?: emptyList()

    override var colorEffect = ColorEffect.UNKNOWN
        set(value) {
            if (supportedColorEffects.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.COLOR_EFFECT)
            } else {
                LogDelegate.log("Unsupported ColorEffect [$value]")
            }
        }

    override val supportedColorEffects: List<ColorEffect>
        get() = params?.supportedColorEffects
            ?.map { ColorEffect.fromString(it) }
            ?.distinct()
            ?.filter { it != ColorEffect.UNKNOWN }
            ?: emptyList()

    override var antibanding = Antibanding.UNKNOWN
        set(value) {
            if (supportedAntibanding.contains(value)) {
                field = value
                onUpdateListener(CameraProperty.ANTIBANDING)
            } else {
                LogDelegate.log("Unsupported Antibanding [$value]")
            }
        }

    override val supportedAntibanding: List<Antibanding>
        get() = params?.supportedAntibanding
            ?.map { Antibanding.fromString(it) }
            ?.distinct()
            ?.filter { it != Antibanding.UNKNOWN }
            ?: emptyList()

    override var exposureCompensation = 0
        set(value) {
            if (value in supportedExposureCompensation) {
                field = value
                onUpdateListener(CameraProperty.EXPOSURE_COMPENSATION)
            } else {
                LogDelegate.log("Unsupported ExposureCompensation [$value]")
            }
        }
    override val supportedExposureCompensation: List<Int>
        get() {
            val min = params?.minExposureCompensation ?: 0
            val max = params?.maxExposureCompensation ?: 0
            return if (min == 0 && max == 0) emptyList() else (min..max).toList()
        }

    override val isExposureCompensationSupported: Boolean
        get() = supportedExposureCompensation.isNotEmpty()
}