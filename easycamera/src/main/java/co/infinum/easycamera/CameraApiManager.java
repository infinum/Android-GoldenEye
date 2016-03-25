package co.infinum.easycamera;

import android.os.Build;

/**
 * Created by jmarkovic on 26/01/16.
 */
public final class CameraApiManager {

    private CameraApiManager() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " cannot be instantiated.");
    }

    /**
     * Builds a new instance of SDK specific {@link CameraApi} implementation.
     */
    public static CameraApi newInstance(CameraApiCallbacks callbacks) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new Camera2Api(callbacks);
        } else {
            return new Camera1Api(callbacks);
        }
    }

}
