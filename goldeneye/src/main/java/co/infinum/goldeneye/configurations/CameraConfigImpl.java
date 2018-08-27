package co.infinum.goldeneye.configurations;

import android.hardware.Camera;
import android.support.annotation.NonNull;

import java.util.List;

import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.models.FlashMode;
import co.infinum.goldeneye.models.FocusMode;
import co.infinum.goldeneye.models.Size;
import co.infinum.goldeneye.utils.CollectionUtils;

import static co.infinum.goldeneye.utils.LoggingUtils.log;

public class CameraConfigImpl implements CameraConfig {

    private Camera.Parameters cameraParameters;
    private final Facing facing;
    private FlashMode flashMode;
    private FocusMode focusMode;
    private final int id;
    private Size imageSize;
    private final int orientation;
    private Size videoSize;

    public CameraConfigImpl(int id, int orientation, Facing facing) {
        this.id = id;
        this.orientation = orientation;
        this.facing = facing;
    }

    @Override
    public int getCameraId() {
        return id;
    }

    @Override
    public int getCameraOrientation() {
        return orientation;
    }

    @NonNull
    @Override
    public Facing getFacing() {
        return facing;
    }

    @NonNull
    @Override
    public FlashMode getFlashMode() {
        return flashMode != null ? flashMode : FlashMode.OFF;
    }

    @NonNull
    @Override
    public FocusMode getFocusMode() {
        return focusMode != null ? focusMode : FocusMode.AUTO;
    }

    @NonNull
    @Override
    public Size getImageSize() {
        return imageSize != null ? imageSize : Size.UNKNOWN;
    }

    @NonNull
    @Override
    public List<FlashMode> getSupportedFlashModes() {
        return CollectionUtils.toInternalFlashModeList(cameraParameters.getSupportedFlashModes());
    }

    @NonNull
    @Override
    public List<FocusMode> getSupportedFocusModes() {
        return CollectionUtils.toInternalFocusModeList(cameraParameters.getSupportedFocusModes());
    }

    @NonNull
    @Override
    public List<Size> getSupportedImageSizes() {
        return CollectionUtils.toSortedInternalSizeList(cameraParameters.getSupportedPictureSizes());
    }

    @NonNull
    @Override
    public List<Size> getSupportedVideoSizes() {
        return CollectionUtils.toSortedInternalSizeList(cameraParameters.getSupportedVideoSizes());
    }

    @NonNull
    @Override
    public Size getVideoSize() {
        return videoSize != null ? videoSize : Size.UNKNOWN;
    }

    public void setCameraParameters(Camera.Parameters cameraParameters) {
        this.cameraParameters = cameraParameters;
    }

    @Override
    public void setFlashMode(FlashMode flashMode) {
        if (getSupportedFlashModes().contains(flashMode)) {
            this.flashMode = flashMode;
        } else {
            log("Unsupported FlashMode [%s]", flashMode);
        }
    }

    @Override
    public void setFocusMode(@NonNull FocusMode focusMode) {
        if (getSupportedFocusModes().contains(focusMode)) {
            this.focusMode = focusMode;
        } else {
            log("Unsupported FocusMode [%s]", focusMode);
        }
    }

    @Override
    public void setImageSize(@NonNull Size size) {
        if (getSupportedImageSizes().contains(size)) {
            this.imageSize = size;
        } else {
            log("Unsupported ImageSize [%s]", size.toString());
        }
    }

    @Override
    public void setVideoSize(@NonNull Size size) {
        if (getSupportedVideoSizes().contains(size)) {
            this.videoSize = size;
        } else {
            log("Unsupported VideoSize [%s]", size.toString());
        }
    }
}
