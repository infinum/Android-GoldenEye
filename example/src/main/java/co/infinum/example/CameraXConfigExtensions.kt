package co.infinum.example

import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageCapture

data class CameraXConfig(
    @ImageCapture.FlashMode val flashMode: Int? = null,
    @AspectRatio.Ratio val aspectRatio: Int? = null
)

fun ImageCapture.Builder.applyConfigs(
    @ImageCapture.FlashMode flashMode: Int? = null,
    @AspectRatio.Ratio aspectRatio: Int? = null
) {
    apply {
        flashMode?.let { setFlashMode(it) }
        aspectRatio?.let { setTargetAspectRatio(it) }
    }
}