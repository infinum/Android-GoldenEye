package co.infinum.goldeneye.models;

import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public enum FocusMode {
    AUTO(Camera.Parameters.FOCUS_MODE_AUTO),
    INFINITY(Camera.Parameters.FOCUS_MODE_INFINITY),
    MACRO(Camera.Parameters.FOCUS_MODE_MACRO),
    FIXED(Camera.Parameters.FOCUS_MODE_FIXED),
    EDOF(Camera.Parameters.FOCUS_MODE_EDOF),
    CONTINUOUS_VIDEO(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO),
    CONTINUOUS_PICTURE(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

    private final String key;

    FocusMode(String key) {
        this.key = key;
    }

    @NonNull
    public static FocusMode fromString(@Nullable String key) {
        for (FocusMode focusMode : values()) {
            if (focusMode.getKey().equals(key)) {
                return focusMode;
            }
        }
        return AUTO;
    }

    public String getKey() {
        return key;
    }
}
