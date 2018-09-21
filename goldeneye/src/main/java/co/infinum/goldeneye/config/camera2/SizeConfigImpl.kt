package co.infinum.goldeneye.config.camera2

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Build
import android.support.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseSizeConfig
import co.infinum.goldeneye.config.SizeConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Size
import co.infinum.goldeneye.models.toInternalSize
import co.infinum.goldeneye.utils.CameraUtils
import co.infinum.goldeneye.utils.LogDelegate

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class SizeConfigImpl(
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseSizeConfig<CameraCharacteristics>(onUpdateCallback) {

    override val supportedPreviewSizes
        get() = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java)
            ?.map { it.toInternalSize() }
            ?.sorted()
            ?: emptyList()

    override val supportedPictureSizes
        get() = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(ImageReader::class.java)
            ?.map { it.toInternalSize() }
            ?.sorted()
            ?: emptyList()

    override val supportedVideoSizes
        get() = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(MediaRecorder::class.java)
            ?.map { it.toInternalSize() }
            ?.sorted()
            ?: emptyList()
}