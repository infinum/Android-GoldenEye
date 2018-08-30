package co.infinum.goldeneye.extensions

import android.graphics.SurfaceTexture
import android.view.TextureView
import co.infinum.goldeneye.SimpleTextureListener

internal fun TextureView.onSurfaceUpdate(onAvailable: (TextureView) -> Unit, onSizeChanged: (TextureView) -> Unit) {
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