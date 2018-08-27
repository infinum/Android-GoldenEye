package co.infinum.goldeneye.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import java.util.List;

import co.infinum.goldeneye.configurations.CameraConfig;
import co.infinum.goldeneye.exceptions.CameraNotAvailableException;
import co.infinum.goldeneye.exceptions.CameraNotOpenedException;
import co.infinum.goldeneye.exceptions.NoCameraPermissionException;

public class Intrinsics {

    private Intrinsics() {
        throw new RuntimeException("Utility class should never be instantiated.");
    }

    public static <T extends CameraConfig> void checkCameraAvailable(
        @Nullable List<T> availableCameras
    ) throws CameraNotAvailableException {

        if (availableCameras == null || availableCameras.size() == 0) {
            throw new CameraNotAvailableException();
        }
    }

    public static void checkCameraOpened(@Nullable Camera camera) throws CameraNotOpenedException {
        if (camera == null) {
            throw new CameraNotOpenedException();
        }
    }

    public static void checkCameraPermission(Activity activity) throws NoCameraPermissionException {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw new NoCameraPermissionException();
        }
    }
}
