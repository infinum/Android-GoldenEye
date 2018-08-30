@file:Suppress("DEPRECATION")

package co.infinum.goldeneye

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.view.TextureView
import android.view.View

internal fun Camera.Size.toInternalSize() = Size(width, height)

internal fun TextureView.onSurfaceUpdate(
    onAvailable: (TextureView) -> Unit,
    onSizeChanged: (TextureView) -> Unit
) {
    if (isAvailable) {
        onAvailable(this)
    }

    surfaceTextureListener = object : SimpleTextureListener() {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            onSizeChanged(this@onSurfaceUpdate)
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            onAvailable(this@onSurfaceUpdate)
        }
    }
}

internal fun View.isMeasured() = height > 0 && width > 0
internal fun View.isNotMeasured() = isMeasured().not()

internal fun <T1, T2> ifNotNull(p1: T1?, p2: T2?, action: (T1, T2) -> Unit) {
    if (p1 != null && p2 != null) {
        action(p1, p2)
    }
}

internal fun Camera.updateParams(update: Camera.Parameters.() -> Unit) {
    parameters = parameters?.apply(update)
}

internal fun Camera.takePicture(
    pictureFactory: PictureFactory,
    onShutter: () -> Unit,
    onPicture: (Bitmap) -> Unit,
    onError: (Throwable) -> Unit
) {
    val shutterCallback = Camera.ShutterCallback { onShutter() }
    val pictureCallback = Camera.PictureCallback { data, _ ->
        pictureFactory.byteArrayToBitmap(data) {
            if (it != null) {
                onPicture(it)
            } else {
                onError(PictureConversionException)
            }
        }
    }
    takePicture(shutterCallback, null, pictureCallback)
}