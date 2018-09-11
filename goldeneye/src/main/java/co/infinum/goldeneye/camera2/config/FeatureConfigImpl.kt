package co.infinum.goldeneye.camera2.config

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.FeatureConfig
import co.infinum.goldeneye.config.SizeConfig
import co.infinum.goldeneye.models.*
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

internal class FeatureConfigImpl(
    private val onUpdateListener: (CameraProperty) -> Unit
): FeatureConfig {

    override var tapToFocusEnabled: Boolean
        get() = TODO("not implemented")
        set(value) {}
    override var resetFocusDelay: Long
        get() = TODO("not implemented")
        set(value) {}
    override var flashMode: FlashMode
        get() = TODO("not implemented")
        set(value) {}
    override val supportedFlashModes: List<FlashMode>
        get() = TODO("not implemented")
    override var focusMode: FocusMode
        get() = TODO("not implemented")
        set(value) {}
    override val supportedFocusModes: List<FocusMode>
        get() = TODO("not implemented")
    override var whiteBalance: WhiteBalance
        get() = TODO("not implemented")
        set(value) {}
    override val supportedWhiteBalance: List<WhiteBalance>
        get() = TODO("not implemented")
    override var sceneMode: SceneMode
        get() = TODO("not implemented")
        set(value) {}
    override val supportedSceneModes: List<SceneMode>
        get() = TODO("not implemented")
    override var colorEffect: ColorEffect
        get() = TODO("not implemented")
        set(value) {}
    override val supportedColorEffects: List<ColorEffect>
        get() = TODO("not implemented")
    override var antibanding: Antibanding
        get() = TODO("not implemented")
        set(value) {}
    override val supportedAntibanding: List<Antibanding>
        get() = TODO("not implemented")
}