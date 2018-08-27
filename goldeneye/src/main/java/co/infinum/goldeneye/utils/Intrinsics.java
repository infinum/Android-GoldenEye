package co.infinum.goldeneye.utils;

import android.hardware.Camera;

import java.util.List;

import co.infinum.goldeneye.configurations.CameraConfig;
import co.infinum.goldeneye.exceptions.CameraNotOpenedException;
import co.infinum.goldeneye.exceptions.CameraNotAvailableException;

public class Intrinsics {

    private Intrinsics() {
        throw new RuntimeException("Utility class should never be instantiated.");
    }

    public static <T extends CameraConfig> void checkCameraAvailable(List<T> availableCameras) throws
        CameraNotAvailableException {
        if (availableCameras.size() == 0) {
            throw new CameraNotAvailableException();
        }
    }

    public static void checkCameraOpened(Camera camera) throws CameraNotOpenedException {
        if (camera == null) {
            throw new CameraNotOpenedException();
        }
    }
}
