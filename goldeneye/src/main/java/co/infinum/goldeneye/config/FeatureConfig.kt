package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.*

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
}