package co.infinum.goldeneye.config.camera2

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.annotation.RequiresApi
import co.infinum.goldeneye.config.BaseSizeConfig
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.config.VideoConfig
import co.infinum.goldeneye.models.CameraProperty
import co.infinum.goldeneye.models.toInternalSize

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class SizeConfigImpl(
    cameraInfo: CameraInfo,
    videoConfig: VideoConfig,
    onUpdateCallback: (CameraProperty) -> Unit
) : BaseSizeConfig<CameraCharacteristics>(cameraInfo, videoConfig, onUpdateCallback) {

    override val supportedPreviewSizes by lazy {
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java)
            ?.map { it.toInternalSize() }
            /* Preview sizes that are too big can crash the camera. Filter only 1080p and below to keep it in order. */
            ?.filter { it.isOver1080p().not() }
            ?.sorted()
            ?: emptyList()
    }

    override val supportedPictureSizes by lazy {
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(ImageFormat.JPEG)
            ?.map { it.toInternalSize() }
            ?.sorted()
            ?: emptyList()
    }
}