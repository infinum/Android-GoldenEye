package co.infinum.goldeneye;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.infinum.goldeneye.configurations.CameraConfig;
import co.infinum.goldeneye.configurations.CameraConfigImpl;
import co.infinum.goldeneye.configurations.ConfigDelegateImpl;
import co.infinum.goldeneye.configurations.PreviewConfig;
import co.infinum.goldeneye.exceptions.CameraNotAvailableException;
import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.models.Info;
import co.infinum.goldeneye.models.PreviewType;
import co.infinum.goldeneye.models.Size;
import co.infinum.goldeneye.utils.CameraUtils;
import co.infinum.goldeneye.utils.CollectionUtils;
import co.infinum.goldeneye.utils.Intrinsics;

import static co.infinum.goldeneye.utils.Intrinsics.checkCameraOpened;

public class GoldenEyeImpl implements GoldenEye {

    private static final PreviewConfig DEFAULT_PREVIEW_CONFIG = new PreviewConfig.Builder().build();
    private final Activity activity;
    private List<CameraConfigImpl> availableCameras;
    private Camera camera;
    private ConfigDelegateImpl configDelegate;
    private Logger logger;
    private TextureView textureView;

    public GoldenEyeImpl(Activity activity) {
        this.activity = activity;
        this.configDelegate = new ConfigDelegateImpl();
    }

    @Override
    public CameraConfig getConfig() {
        return configDelegate;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void init(Facing facing, InitializationCallback initializationCallback) {
        try {
            initAvailableCameras();

            CameraConfigImpl currentCameraConfig;
            if (CameraUtils.hasFacing(availableCameras, facing)) {
                currentCameraConfig = CameraUtils.findCamera(availableCameras, facing);
            } else {
                logger.info(Info.FACING_NOT_AVAILABLE, facing.toString());
                currentCameraConfig = availableCameras.get(0);
            }

            camera = Camera.open(currentCameraConfig.getCameraId());
            checkCameraOpened(camera);
            currentCameraConfig.setCameraParameters(camera.getParameters());

            configDelegate.setCurrentCameraConfig(currentCameraConfig);

            initializationCallback.onSuccess(configDelegate);
        } catch (Exception e) {
            initializationCallback.onError(e);
        }
    }

    @Override
    public void startPreview(@NonNull final TextureView textureView, PreviewConfig previewConfig) {
        if (isPreviewStarted()) {
            logger.info(Info.PREVIEW_ALREADY_STARTED);
            return;
        }

        if (configDelegate.getCurrentCameraConfig() == null) {
            logger.info(Info.CAMERA_NOT_INITIALIZED);
            return;
        }

        this.textureView = textureView;
        this.configDelegate.setPreviewConfig(previewConfig);

        if (textureView.isAvailable()) {
            initAndStartPreview(camera, textureView);
        } else {
            textureView.setSurfaceTextureListener(new SimpleTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    initAndStartPreview(camera, textureView);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    //apply matrix
                }
            });
        }
    }

    @Override
    public void startPreview(@NonNull TextureView textureView) {
        startPreview(textureView, DEFAULT_PREVIEW_CONFIG);
    }

    @Override
    public void startRecording(File file, VideoCallback callback) {
    }

    @Override
    public void stopPreview() {
        if (!isPreviewStarted()) {
            logger.info(Info.PREVIEW_ALREADY_STARTED);
        }

        releaseCamera();
        textureView = null;
    }

    @Override
    public void stopRecording() {

    }

    @Override
    public void takeImage(ImageCallback callback) {

    }

    private void applyConfig() {
        Camera.Parameters params = camera.getParameters();
        Size imageSize = configDelegate.getImageSize().equals(Size.UNKNOWN)
            ? configDelegate.getSupportedImageSizes().get(0)
            : configDelegate.getImageSize();
        Size videoSize = configDelegate.getVideoSize().equals(Size.UNKNOWN)
            ? configDelegate.getSupportedVideoSizes().get(0)
            : configDelegate.getVideoSize();

        Size referenceSize = configDelegate.getPreviewType() == PreviewType.IMAGE ? imageSize : videoSize;
        Size previewSize = CollectionUtils.findFirstSize(params.getSupportedPreviewSizes(), referenceSize);
        if (previewSize != null) {
            params.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
            params.setPictureSize(imageSize.getWidth(), imageSize.getHeight());

            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            //            if (cameraOptions.focusModes.contains(currentCameraConfiguration().focusMode)) {
            //                params.focusMode = currentCameraConfiguration().focusMode.camera1Key
            //            }
            //            if (cameraOptions.flashModes.contains(currentCameraConfiguration().flashMode)) {
            //                params.flashMode = currentCameraConfiguration().flashMode.camera1Key
            //            }

            camera.setParameters(params);
            textureView.setTransform(
                CameraUtils.calculateTextureMatrix(
                    textureView,
                    configDelegate,
                    previewSize,
                    CameraUtils.calculateDisplayOrientation(activity, configDelegate)
                )
            );
        } else {
            Log.e("Goldeneye", "Test");
        }
    }

    private void initAndStartPreview(Camera camera, TextureView textureView) {
        try {
            camera.stopPreview();
            camera.setPreviewTexture(textureView.getSurfaceTexture());
            camera.setDisplayOrientation(CameraUtils.calculateDisplayOrientation(activity, configDelegate));
            applyConfig();
            camera.startPreview();
        } catch (IOException e) {
            logger.info(Info.PREVIEW_FAILED_TO_START);
        }
    }

    private void initAvailableCameras() throws CameraNotAvailableException {
        this.availableCameras = new ArrayList<>();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            Facing cameraFacing = info.facing == Camera.CameraInfo.CAMERA_FACING_BACK ? Facing.BACK : Facing.FRONT;
            availableCameras.add(new CameraConfigImpl(i, info.orientation, cameraFacing));
        }
        Intrinsics.checkCameraAvailable(availableCameras);
    }

    private boolean isPreviewStarted() {
        return textureView != null;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
