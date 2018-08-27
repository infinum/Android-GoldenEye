package co.infinum.goldeneye;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

public abstract class SimpleTextureListener implements TextureView.SurfaceTextureListener {

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}
