package co.infinum.goldeneye.configurations;

import android.support.annotation.NonNull;

import java.util.List;

import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.models.FlashMode;
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
    public Size getImageSize() {
        return currentCameraConfig.getImageSize();
    }

    @Override
    public PreviewScale getPreviewScale() {
        return previewConfig.getPreviewScale();
    }

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
    public void setImageSize(Size size) {
        currentCameraConfig.setImageSize(size);
    }

    public void setPreviewConfig(PreviewConfig previewConfig) {
        this.previewConfig = previewConfig;
    }

    @Override
    public void setPreviewScale(PreviewScale previewScale) {
        previewConfig.setPreviewScale(previewScale);
    }

    @Override
    public void setPreviewType(PreviewType previewType) {
        previewConfig.setPreviewType(previewType);
    }

    @Override
    public void setVideoSize(Size size) {
        currentCameraConfig.setVideoSize(size);
    }
}
