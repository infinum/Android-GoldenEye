package co.infinum.easycamera;

import android.graphics.SurfaceTexture;
import android.support.annotation.CallSuper;
import android.view.TextureView;

/**
 * Created by jmarkovic on 06/04/16.
 */
public abstract class SimpleSurfaceTextureListener implements TextureView.SurfaceTextureListener {

    private CameraApi cameraApi;

    public SimpleSurfaceTextureListener(CameraApi cameraApi) {
        this.cameraApi = cameraApi;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    @CallSuper
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        cameraApi.updatePreviewDimensions(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
