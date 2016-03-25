package co.infinum.easycamera;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes an error in any sequence while using CameraApi.
 */
public final class CameraError {

    public static final String ERROR_MISSING_SYSTEM_FEATURE = "Device is missing CAMERA system feature";

    public static final String ERROR_CAMERA_IN_USE = "Camera is already in use";

    public static final String ERROR_CAMERA_CONFIGURATION = "Camera failed in configuration";

    private final String error;

    CameraError(@ErrorDef final String error) {
        this.error = error;
    }

    @ErrorDef
    public String getError() {
        return this.error;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ERROR_MISSING_SYSTEM_FEATURE,
            ERROR_CAMERA_IN_USE,
            ERROR_CAMERA_CONFIGURATION
    })
    public @interface ErrorDef {

    }

}
