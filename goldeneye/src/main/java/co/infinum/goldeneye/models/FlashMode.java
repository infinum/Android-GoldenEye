package co.infinum.goldeneye.models;

import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public enum FlashMode {
    OFF(Camera.Parameters.FLASH_MODE_OFF),
    ON(Camera.Parameters.FLASH_MODE_ON),
    AUTO(Camera.Parameters.FLASH_MODE_AUTO),
    TORCH(Camera.Parameters.FLASH_MODE_TORCH),
    RED_EYE(Camera.Parameters.FLASH_MODE_RED_EYE);

    private final String key;

    FlashMode(String key) {
        this.key = key;
    }

    @NonNull
    public static FlashMode fromString(@Nullable String key) {
        for (FlashMode flashMode : values()) {
            if (flashMode.getKey().equals(key)) {
                return flashMode;
            }
        }
        return OFF;
    }

    public String getKey() {
        return key;
    }
}
