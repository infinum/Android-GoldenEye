package co.infinum.goldeneye.configurations;

import android.support.annotation.NonNull;

import java.util.List;

import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.models.FlashMode;
import co.infinum.goldeneye.models.Size;

public interface CameraConfig {

    int getCameraId();

    int getCameraOrientation();

    @NonNull
    Facing getFacing();

    @NonNull
    Size getImageSize();

    @NonNull
    Size getVideoSize();

    void setVideoSize(Size size);

    void setImageSize(Size size);

    @NonNull
    List<Size> getSupportedImageSizes();

    @NonNull
    List<Size> getSupportedVideoSizes();

    @NonNull
    List<FlashMode> getSupportedFlashModes();

    void setFlashMode(FlashMode flashMode);

    @NonNull
    FlashMode getFlashMode();
}
