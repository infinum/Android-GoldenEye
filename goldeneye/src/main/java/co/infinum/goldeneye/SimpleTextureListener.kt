package co.infinum.goldeneye

import android.graphics.SurfaceTexture
import android.view.TextureView

internal abstract class SimpleTextureListener : TextureView.SurfaceTextureListener {
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = true
}