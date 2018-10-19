package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.AntibandingMode
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.ColorEffectMode
import co.infinum.goldeneye.models.WhiteBalanceMode
import co.infinum.goldeneye.utils.LogDelegate.log

interface AdvancedFeatureConfig {
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

internal abstract class BaseAdvancedFeatureConfig<T : Any>(
    private val advancedFeaturesEnabled: Boolean,
    private val onUpdateCallback: (CameraProperty) -> Unit
) : AdvancedFeatureConfig {

    lateinit var characteristics: T

    override var whiteBalanceMode = WhiteBalanceMode.UNKNOWN
        get() = when {
            field != WhiteBalanceMode.UNKNOWN -> field
            supportedWhiteBalanceModes.contains(WhiteBalanceMode.AUTO) -> WhiteBalanceMode.AUTO
            else -> WhiteBalanceMode.UNKNOWN
        }
        set(value) {
            when {
                advancedFeaturesEnabled.not() -> logAdvancedFeaturesDisabled()
                supportedWhiteBalanceModes.contains(value) -> {
                    field = value
                    onUpdateCallback(CameraProperty.WHITE_BALANCE)
                }
                else -> log("Unsupported WhiteBalance [$value]")
            }
        }

    override var colorEffectMode = ColorEffectMode.UNKNOWN
        get() = when {
            field != ColorEffectMode.UNKNOWN -> field
            else -> ColorEffectMode.UNKNOWN
        }
        set(value) {
            when {
                advancedFeaturesEnabled.not() -> logAdvancedFeaturesDisabled()
                supportedColorEffectModes.contains(value) -> {
                    field = value
                    onUpdateCallback(CameraProperty.COLOR_EFFECT)
                }
                else -> log("Unsupported ColorEffect [$value]")
            }
        }

    override var antibandingMode = AntibandingMode.UNKNOWN
        get() = when {
            field != AntibandingMode.UNKNOWN -> field
            supportedAntibandingModes.contains(AntibandingMode.AUTO) -> AntibandingMode.AUTO
            else -> AntibandingMode.UNKNOWN
        }
        set(value) {
            when {
                advancedFeaturesEnabled.not() -> logAdvancedFeaturesDisabled()
                supportedAntibandingModes.contains(value) -> {
                    field = value
                    onUpdateCallback(CameraProperty.ANTIBANDING)
                }
                else -> log("Unsupported Antibanding [$value]")
            }
        }

    private fun logAdvancedFeaturesDisabled() {
        log("Advanced features disabled. Use GoldenEye#Builder.withAdvancedFeatures() method to activate them.")
    }
}