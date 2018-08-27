package co.infinum.goldeneye.utils;

import android.app.Activity;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.TextureView;

import java.util.List;

import co.infinum.goldeneye.configurations.CameraConfig;
import co.infinum.goldeneye.configurations.PreviewConfig;
import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.models.PreviewScale;
import co.infinum.goldeneye.models.Size;

public class CameraUtils {

    private CameraUtils() {
        throw new RuntimeException("Utility class should never be instantiated.");
    }

    public static <T extends CameraConfig> int calculateDisplayOrientation(Activity activity, T cameraConfig) {
        int deviceOrientation = getDeviceOrientation(activity);
        int cameraOrientation = cameraConfig.getCameraOrientation();

        if (cameraConfig.getFacing() == Facing.FRONT) {
            return (360 - ((cameraOrientation + deviceOrientation) % 360)) % 360;
        } else {
            return (cameraOrientation - deviceOrientation + 360) % 360;
        }
    }

    @NonNull
    public static Matrix calculateTextureMatrix(
        @NonNull TextureView textureView,
        @NonNull PreviewConfig previewConfig,
        @NonNull Size previewSize,
        int cameraDisplayOrientation
    ) {
        if (textureView.getWidth() == 0 || textureView.getHeight() == 0
            || previewSize.getWidth() == 0 || previewSize.getHeight() == 0) {
            return new Matrix();
        }

        Matrix matrix = new Matrix();
        float viewWidth = ((float) textureView.getWidth());
        float viewHeight = ((float) textureView.getHeight());

        float scaleX;
        float scaleY;
        if (previewConfig.getPreviewScale() == PreviewScale.FILL) {
            scaleX = cameraDisplayOrientation % 180 == 0 ? viewHeight / previewSize.getHeight() : viewHeight / previewSize.getWidth();
            scaleY = cameraDisplayOrientation % 180 == 0 ? viewWidth / previewSize.getWidth() : viewWidth / previewSize.getHeight();
            if (scaleX < 1) {
                scaleY *= 1 / scaleX;
                scaleX = 1f;
            }
            if (scaleY < 1) {
                scaleX *= 1 / scaleY;
                scaleY = 1f;
            }
        } else {
            scaleX = cameraDisplayOrientation % 180 == 0 ? previewSize.getWidth() / viewWidth : previewSize.getHeight() / viewWidth;
            scaleY = cameraDisplayOrientation % 180 == 0 ? previewSize.getHeight() / viewHeight : previewSize.getWidth() / viewHeight;
        }

        matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f);
        return matrix;
    }

    @Nullable
    public static <T extends CameraConfig> T findCamera(@NonNull List<T> availableCameras, @NonNull Facing facing) {
        for (T cameraConfig : availableCameras) {
            if (cameraConfig.getFacing() == facing) {
                return cameraConfig;
            }
        }
        return null;
    }

    public static <T extends CameraConfig> boolean hasFacing(@NonNull List<T> availableCameras, @NonNull Facing facing) {
        return findCamera(availableCameras, facing) != null;
    }

    @Nullable
    public static <T extends CameraConfig> T nextCamera(@NonNull List<T> availableCameras, @NonNull T currentCameraConfig) {
        int currentIndex = availableCameras.indexOf(currentCameraConfig);
        return currentIndex != -1 ? availableCameras.get((currentIndex + 1) % availableCameras.size()) : null;
    }

    private static int getDeviceOrientation(Activity activity) {
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_0:
            default:
                return 0;
        }
    }
}
