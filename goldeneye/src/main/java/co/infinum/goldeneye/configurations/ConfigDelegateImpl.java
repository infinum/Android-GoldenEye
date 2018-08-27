package co.infinum.goldeneye.configurations;

import android.support.annotation.NonNull;

import java.util.List;

import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.models.FlashMode;
import co.infinum.goldeneye.models.FocusMode;
import co.infinum.goldeneye.models.PreviewScale;
import co.infinum.goldeneye.models.PreviewType;
import co.infinum.goldeneye.models.Size;

public class ConfigDelegateImpl implements ConfigDelegate {

    private CameraConfig currentCameraConfig;
    private PreviewConfig previewConfig;

    public int getCameraId() {
        return currentCameraConfig.getCameraId();
    }

    @Override
    public int getCameraOrientation() {
        return currentCameraConfig.getCameraOrientation();
    }

    public CameraConfig getCurrentCameraConfig() {
        return currentCameraConfig;
    }

    @NonNull
    @Override
    public Facing getFacing() {
        return currentCameraConfig.getFacing();
    }

    @NonNull
    @Override
    public FlashMode getFlashMode() {
        return currentCameraConfig.getFlashMode();
    }

    @NonNull
    @Override
    public FocusMode getFocusMode() {
        return currentCameraConfig.getFocusMode();
    }

    @NonNull
    @Override
    public Size getImageSize() {
        return currentCameraConfig.getImageSize();
    }

    @NonNull
    @Override
    public PreviewScale getPreviewScale() {
        return previewConfig.getPreviewScale();
    }

    @NonNull
    @Override
    public PreviewType getPreviewType() {
        return previewConfig.getPreviewType();
    }

    @NonNull
    @Override
    public List<FlashMode> getSupportedFlashModes() {
        return currentCameraConfig.getSupportedFlashModes();
    }

    @NonNull
    @Override
    public List<FocusMode> getSupportedFocusModes() {
        return currentCameraConfig.getSupportedFocusModes();
    }

    @NonNull
    @Override
    public List<Size> getSupportedImageSizes() {
        return currentCameraConfig.getSupportedImageSizes();
    }

    @NonNull
    @Override
    public List<Size> getSupportedVideoSizes() {
        return currentCameraConfig.getSupportedVideoSizes();
    }

    @NonNull
    @Override
    public Size getVideoSize() {
        return currentCameraConfig.getVideoSize();
    }

    public void setCurrentCameraConfig(CameraConfigImpl currentCameraConfig) {
        this.currentCameraConfig = currentCameraConfig;
    }

    @Override
    public void setFlashMode(FlashMode flashMode) {
        currentCameraConfig.setFlashMode(flashMode);
    }

    @Override
    public void setFocusMode(@NonNull FocusMode focusMode) {
        currentCameraConfig.setFocusMode(focusMode);
    }

    @Override
    public void setImageSize(@NonNull Size size) {
        currentCameraConfig.setImageSize(size);
    }

    public void setPreviewConfig(PreviewConfig previewConfig) {
        this.previewConfig = previewConfig;
    }

    @Override
    public void setPreviewScale(@NonNull PreviewScale previewScale) {
        previewConfig.setPreviewScale(previewScale);
    }

    @Override
    public void setPreviewType(@NonNull PreviewType previewType) {
        previewConfig.setPreviewType(previewType);
    }

    @Override
    public void setVideoSize(@NonNull Size size) {
        currentCameraConfig.setVideoSize(size);
    }
}
