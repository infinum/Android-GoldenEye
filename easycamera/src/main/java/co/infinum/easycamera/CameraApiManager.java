package co.infinum.easycamera;

import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import co.infinum.easycamera.internal.DeviceInfo;

/**
 * Created by jmarkovic on 26/01/16.
 */
public final class CameraApiManager {

    private static final List<DeviceInfo> camera2NonFunctionalDevices = new ArrayList<>();

    static {
        camera2NonFunctionalDevices.add(new DeviceInfo("samsung", "SM-G930*"));
        camera2NonFunctionalDevices.add(new DeviceInfo("samsung", "SM-G935*"));
    }

    private CameraApiManager() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " cannot be instantiated.");
    }

    /**
     * Builds a new instance of SDK specific {@link CameraApi} implementation.
     */
    public static CameraApi newInstance(Config config) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isCamera2ApiFunctional()) {
            return new Camera2Api(config);
        } else {
            return new Camera1Api(config);
        }
    }

    private static boolean isCamera2ApiFunctional() {
        return !camera2NonFunctionalDevices.contains(new DeviceInfo(Build.MANUFACTURER, Build.MODEL));
    }
}
