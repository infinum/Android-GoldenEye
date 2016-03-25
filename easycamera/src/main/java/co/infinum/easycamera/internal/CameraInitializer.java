package co.infinum.easycamera.internal;

import android.hardware.Camera;
import android.os.Handler;

/**
 * Initializes the {@link Camera} object.
 * This process may take some time and should not be done on the main thread.
 */
public class CameraInitializer implements Runnable {

    private final int cameraId;

    private final Size requestedSize;

    private final Handler handler;

    private final Callback callback;

    private Camera camera;

    public CameraInitializer(final int cameraId, Size requestedSize, Handler handler, Callback callback) {
        this.cameraId = cameraId;
        this.requestedSize = requestedSize;
        this.handler = handler;
        this.callback = callback;
    }

    @Override
    public void run() {
        camera = Camera.open(this.cameraId);

        if (callback != null) {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCameraInitialized(camera, requestedSize);
                    }
                });
            } else {
                callback.onCameraInitialized(camera, requestedSize);
            }
        }
    }

    public interface Callback {

        void onCameraInitialized(Camera camera, Size size);

    }

}
