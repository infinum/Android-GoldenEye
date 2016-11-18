package co.infinum.easycamera;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import co.infinum.easycamera.internal.ByteImageSaver;
import co.infinum.easycamera.internal.CameraInitializer;
import co.infinum.easycamera.internal.CompareSizesByArea;
import co.infinum.easycamera.internal.OnImageSavedListener;
import co.infinum.easycamera.internal.Size;

/**
 * Created by jmarkovic on 26/01/16.
 */
class Camera1Api implements CameraApi {

    private static final String TAG = "Camera1Api";

    /**
     * Lock timeout in millis.
     */
    private static final long LOCK_ACQUIRE_TIMEOUT = 2500;

    private static final int JPEG_QUALITY = 100;

    private static final int DEGREES_FULL_CIRCLE = 360;

    private static final int ROTATION_0 = 0;

    private static final int ROTATION_90 = 90;

    private static final int ROTATION_180 = 180;

    private static final int ROTATION_270 = 270;

    private static final int UNDEFINED_CAMERA_ID = -1;

    /**
     * Logic is in preview state, displaying camera input on surface.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera is trying to AutoFocus.
     */
    private static final int STATE_TAKING_FOCUS = 1;

    /**
     * Camera is preparing to take a picture and save it.
     */
    private static final int STATE_TAKING_PICTURE = 2;

    /**
     * Max preview width that is expected from Camera1 API.
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is expected from Camera1 API.
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * Configuration object with callbacks and optional settings.
     */
    private Config config;

    /**
     * Shared logic delegate.
     */
    private CamDelegate camDelegate;

    /**
     * Screen rotation. Either {@link Surface#ROTATION_0}, {@link Surface#ROTATION_90}, {@link Surface#ROTATION_180}
     * or {@link Surface#ROTATION_270}.
     */
    private int displayRotation;

    /**
     * Screen orientation. Either {@link Configuration#ORIENTATION_LANDSCAPE} or {@link Configuration#ORIENTATION_PORTRAIT}.
     * Any other value should be ignored or treated as default.
     */
    private int orientation;

    /**
     * Directory in which taken images will be saved.
     */
    private File storageDirectory;

    /**
     * ID of the current {@link Camera}.
     */
    private int cameraId;

    /**
     * Display size in pixel.
     */
    private final Point displaySize = new Point();

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread backgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler backgroundHandler;

    /**
     * A {@link Handler} for tasks returning values back to main thread.
     */
    private Handler mainHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    /**
     * The current state of camera state for taking pictures.
     */
    private int state = STATE_PREVIEW;

    /**
     * Camera object that controls hardware settings, previews and image snapping.
     */
    private Camera camera;

    /**
     * Surface where the actual preview will be drawn.
     */
    private SurfaceTexture surfaceTexture;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size previewSize;

    /**
     * Flag controlling the state of the camera. If true, {@link #openCamera(int, int)} does nothing.
     * Reset at {@link #closeCamera()}.
     */
    private boolean isCameraActive;

    private boolean flashSupported;

    @FlashDef
    private int currentFlashMode = FLASH_MODE_AUTOMATIC;

    /**
     * This callback is called once the camera has been opened.
     */
    private CameraInitializer.Callback cameraInitializerCallback = new CameraInitializer.Callback() {
        @Override
        public void onCameraInitialized(Camera openedCamera, Size size) {
            cameraOpenCloseLock.release();
            if (openedCamera != null) {
                camera = openedCamera;
                setUpCameraParameters(size.getWidth(), size.getHeight());
            } else {
                config.callbacks.onCameraError(new CameraError(CameraError.ERROR_MISSING_SYSTEM_FEATURE));
            }
        }
    };

    /**
     * Auto Focus callback is called whenever AutoFocus finishes.
     */
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (STATE_TAKING_PICTURE == state) {
                camera.takePicture(null, null, pictureCallback);
            } else if (STATE_TAKING_FOCUS == state) {
                state = STATE_PREVIEW;
            }
        }
    };

    /**
     * Picture callback which will be called when camera has taken a picture.
     */
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // This is the output file for our picture.
            File imageFile;
            if (TextUtils.isEmpty(config.filePath)) {
                imageFile = new File(storageDirectory, String.format(Locale.getDefault(), "%d.jpg", System.currentTimeMillis()));
            } else {
                imageFile = new File(config.filePath);
            }
            backgroundHandler.post(new ByteImageSaver(data, imageFile, imageSavedListener, config.cameraFacing));
        }
    };

    private OnImageSavedListener imageSavedListener = new OnImageSavedListener() {
        @Override
        public void onImageSaved(@NonNull File imageFile) {
            config.callbacks.onImageTaken(imageFile);
            // restart preview
            state = STATE_PREVIEW;
            // todo make start and stop preview configurable
//            camera.startPreview();
        }
    };

    Camera1Api(Config config) {
        this.config = config;
        this.camDelegate = new CamDelegate(config);
    }

    @Override
    public CameraApi init(Activity activity) {
        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            orientation = activity.getResources().getConfiguration().orientation;
            storageDirectory = activity.getExternalFilesDir(null);
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        } else {
            config.callbacks.onCameraError(new CameraError(CameraError.ERROR_MISSING_SYSTEM_FEATURE));
        }
        return this;
    }

    @Override
    public void openCamera(int desiredWidth, int desiredHeight) {
        if (isCameraActive) {
            Log.w(TAG, "Camera is already opened. Did you really mean to open the camera again?");
            return;
        }
        isCameraActive = true;

        startBackgroundThreads();

        try {
            if (!cameraOpenCloseLock.tryAcquire(LOCK_ACQUIRE_TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            if (Camera.getNumberOfCameras() == 0) {
                // no camera hardware available
                throw new UnsupportedOperationException("No camera hardware available.");
            }
            // 0 is the first camera and that should always be rear facing one
            cameraId = getCameraId(config.cameraFacing);
            backgroundHandler.post(new CameraInitializer(cameraId,
                    new Size(desiredWidth, desiredHeight), mainHandler, cameraInitializerCallback));
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        } catch (Exception e) {
            e.printStackTrace();
            config.callbacks.onCameraError(new CameraError(CameraError.ERROR_CAMERA_CONFIGURATION));
        }
    }

    @Override
    public void openCamera(@NonNull TextureView textureView) {
        setSurfaceTexture(textureView.getSurfaceTexture());
        // noinspection ResourceType
        openCamera(textureView.getWidth(), textureView.getHeight());
    }

    @Override
    public void closeCamera() {
        if (isCameraActive) {
            try {
                cameraOpenCloseLock.acquire();
                if (null != camera) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
                if (null != surfaceTexture) {
                    surfaceTexture = null;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
            } finally {
                cameraOpenCloseLock.release();
                stopBackgroundThreads();
                isCameraActive = false;
            }
        } else {
            Log.w(TAG, "Camera already closed. Did you really mean to close the camera again?");
        }

    }

    @Override
    public boolean isCameraActive() {
        return this.isCameraActive;
    }

    @Override
    public void updatePreviewDimensions(int width, int height) {
//        configureTransform(width, height);
    }

    @Override
    public void setFlashMode(@FlashDef int flashMode) {
        Camera.Parameters params = camera.getParameters();
        setFlashModeForCameraParams(params, flashMode);
        camera.setParameters(params);
    }

    @Override
    public int getFlashMode() {
        return currentFlashMode;
    }

    @Override
    public void changeFlashMode() {
        switch(currentFlashMode) {
            case FLASH_MODE_AUTOMATIC:
                currentFlashMode = FLASH_MODE_ON;
                break;
            case FLASH_MODE_ON:
                currentFlashMode = FLASH_MODE_OFF;
                break;
            case FLASH_MODE_OFF:
                currentFlashMode = FLASH_MODE_AUTOMATIC;
                break;
            default:
                break;
        }
        setFlashMode(currentFlashMode);
    }

    @Override
    public void acquireFocus(final int x, final int y) {
        state = STATE_TAKING_FOCUS;
        camera.cancelAutoFocus();
        try {
            Camera.Parameters params = this.camera.getParameters();
            if (params.getMaxNumFocusAreas() > 0) {
                final float previewToDisplayRatio = (float) displaySize.x / (float) previewSize.getHeight();
                final int width = (int) (previewSize.getHeight() * previewToDisplayRatio);
                final int height = (int) (previewSize.getWidth() * previewToDisplayRatio);
                final float relativeX = (float) x / (float) width;
                final float relativeY = (float) y / (float) height;
                int areaX = (int) (2000 * relativeX) - 1000;
                if (areaX > 800) {
                    areaX = 800;
                } else if (areaX < -1000) {
                    areaX = -1000;
                }
                int areaY = (int) (2000 * relativeY) - 1000;
                if (areaY > 800) {
                    areaY = 800;
                } else if (areaY < -1000) {
                    areaY = -1000;
                }
                final Rect areaRect = new Rect(areaX, areaY, areaX + 200, areaY + 200);
                Log.d(TAG, "focus area rect -> " + String.valueOf(areaRect));
                Camera.Area focusArea = new Camera.Area(areaRect, 500);
                params.setFocusAreas(Collections.singletonList(focusArea));
                camera.setParameters(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera.autoFocus(autoFocusCallback);
    }

    @Override
    public boolean hasCameraFacing(@CameraFacingDef int cameraFacing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (cameraFacing == CAMERA_FACING_BACK && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return true;
            } else if (cameraFacing == CAMERA_FACING_FRONT && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void switchCameraFacing() {
        if (surfaceTexture == null) {
            throw new RuntimeException("TextureView hasn't been set yet.");
        }

        // Create a new copy of the Config with a different camera facing.
        @CameraFacingDef int cameraFacing = config.cameraFacing == CAMERA_FACING_BACK ? CAMERA_FACING_FRONT : CAMERA_FACING_BACK;
        this.config = new Config.Builder(config)
                .cameraFacing(cameraFacing)
                .build();

        SurfaceTexture surfTexture = this.surfaceTexture;
        int desiredWidth = camera.getParameters().getPreviewSize().width;
        int desiredHeight = camera.getParameters().getPreviewSize().height;

        closeCamera();

        setSurfaceTexture(surfTexture);
        // noinspection ResourceType
        openCamera(desiredWidth, desiredHeight);
    }

    @Override
    public int getCurrentCameraFacing() {
        return config.cameraFacing;
    }

    @Override
    public void setSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
        this.surfaceTexture = surfaceTexture;
    }

    @Override
    public void takePicture() {
        state = STATE_TAKING_PICTURE;
        camera.autoFocus(autoFocusCallback);
    }

    private void setUpCameraParameters(int width, int height) {
        try {

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(this.cameraId, info);

            camera.setDisplayOrientation(resolveCameraOrientation(info));

            boolean swappedDimensions = Configuration.ORIENTATION_LANDSCAPE == orientation;

            // depending on the orientation check, define sizes
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            // set camera properties
            Camera.Parameters params = camera.getParameters();
            params.setJpegQuality(JPEG_QUALITY);
            params.setPictureFormat(ImageFormat.JPEG);

            List<Camera.Size> cameraSizes = params.getSupportedPictureSizes();
            List<Size> convertedCameraSizes = new ArrayList<>(cameraSizes.size());
            convertCameraSizeListToInternalSizeList(cameraSizes, convertedCameraSizes);

            // filter sizes per aspect ratio only if
            camDelegate.filterAspect(convertedCameraSizes);

            Size largest = Collections.max(convertedCameraSizes, new CompareSizesByArea());
            params.setPictureSize(largest.getWidth(), largest.getHeight());

            // image needs to know screen orientation to properly rotate when saved
            params.setRotation(resolveCameraOrientation(info));

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            flashSupported = getFlashSupportedInfo();
            setFlashModeForCameraParams(params, currentFlashMode);

            // apply parameters
            camera.setParameters(params);

            // Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            previewSize = chooseOptimalSize(convertedCameraSizes,
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest);

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                config.callbacks.onResolvedPreviewSize(previewSize.getWidth(), previewSize.getHeight());
            } else {
                config.callbacks.onResolvedPreviewSize(previewSize.getHeight(), previewSize.getWidth());
            }

            configureTransform(width, height);

            camera.setPreviewTexture(this.surfaceTexture);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates current camera parameters {@link android.hardware.Camera.Parameters} and current flash mode
     * with the new flash mode {@link FlashDef}.
     */
    private void setFlashModeForCameraParams(Camera.Parameters params, @FlashDef int flashMode) {
        if (flashSupported) {
            String controlAeMode;
            switch (flashMode) {
                case FLASH_MODE_AUTOMATIC:
                    controlAeMode = Camera.Parameters.FLASH_MODE_AUTO;
                    break;

                case FLASH_MODE_OFF:
                    controlAeMode = Camera.Parameters.FLASH_MODE_OFF;
                    break;

                case FLASH_MODE_ON:
                    controlAeMode = Camera.Parameters.FLASH_MODE_ON;
                    break;

                default:
                    Log.e(TAG,
                            String.format("Unrecognized flash mode set, skipping this option and using default. [mode -> %d]", flashMode));
                    controlAeMode = Camera.Parameters.FLASH_MODE_AUTO;
                    break;
            }

            currentFlashMode = flashMode;
            params.setFlashMode(controlAeMode);
        }
    }

    /**
     * Improved version of flash check that should work on all devices (http://stackoverflow.com/a/19599365).
     *
     * @return is flash supported
     */
    private boolean getFlashSupportedInfo() {
        if (camera == null) {
            return false;
        }

        Camera.Parameters cameraParams = camera.getParameters();
        if (cameraParams.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = cameraParams.getSupportedFlashModes();
        //noinspection RedundantIfStatement
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || isOnlyOffMode(supportedFlashModes)) {
            return false;
        }
        return true;
    }

    private boolean isOnlyOffMode(List<String> supportedFlashModes) {
        return supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF);
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `textureView`.
     * This method should be called after the camera preview size is determined in
     * openCamera and also the size of `textureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == previewSize) {
            return;
        }
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == displayRotation || Surface.ROTATION_270 == displayRotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(ROTATION_90 * (displayRotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == displayRotation) {
            matrix.postRotate(ROTATION_180, centerX, centerY);
        }
        config.callbacks.onTransformChanged(matrix);
    }

    private int resolveCameraOrientation(Camera.CameraInfo info) {
        int degrees;
        switch (displayRotation) {
            case Surface.ROTATION_0:
                degrees = ROTATION_0;
                break;

            case Surface.ROTATION_90:
                degrees = ROTATION_90;
                break;

            case Surface.ROTATION_180:
                degrees = ROTATION_180;
                break;

            case Surface.ROTATION_270:
                degrees = ROTATION_270;
                break;

            default:
                Log.w(TAG, String.format("Unknown display rotation [displayRotation -> %d]", displayRotation));
                degrees = ROTATION_0;
                break;
        }

        int result;
        if (Camera.CameraInfo.CAMERA_FACING_FRONT == info.facing) {
            result = (info.orientation + degrees) % DEGREES_FULL_CIRCLE;
            result = (DEGREES_FULL_CIRCLE - result) % DEGREES_FULL_CIRCLE;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREES_FULL_CIRCLE) % DEGREES_FULL_CIRCLE;
        }

        return result;
    }

    // todo copy of CameraApi2, think of elegant way to share code

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private Size chooseOptimalSize(List<Size> choices, int textureViewWidth,
            int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        final int width = aspectRatio.getWidth();
        final int height = aspectRatio.getHeight();

        for (Size option : choices) {
            final int aspectResult = camDelegate.isAspectWithinBounds((double) width / (double) height);
            final boolean correctAspect = aspectResult == CamDelegate.ASPECT_UNKNOWN
                    ? option.getHeight() == option.getWidth() * height / width
                    : aspectResult == CamDelegate.ASPECT_WITHIN_BOUNDS;

            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight
                    && correctAspect) {
                if (option.getWidth() >= textureViewWidth
                        && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            final Size defaultSize = choices.get(0);
            Log.e(TAG, "Couldn't find any suitable preview size, returning default size -> " + String.valueOf(defaultSize));
            return defaultSize;
        }
    }

    private void startBackgroundThreads() {
        backgroundThread = new HandlerThread("camera-1-api");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        mainHandler = new Handler(Looper.getMainLooper());
    }

    private void stopBackgroundThreads() {
        // send quit signal and then join the thread
        backgroundThread.getLooper().quit();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
            mainHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Size convertCameraSizeToInternalSize(Camera.Size cameraSize) {
        return new Size(cameraSize.width, cameraSize.height);
    }

    private void convertCameraSizeListToInternalSizeList(List<Camera.Size> cameraSizes, List<Size> internalSizes) {
        for (Camera.Size cameraSize : cameraSizes) {
            internalSizes.add(convertCameraSizeToInternalSize(cameraSize));
        }
    }

    private int getCameraId(@CameraFacingDef int cameraFacing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (cameraFacing == CAMERA_FACING_BACK && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            } else if (cameraFacing == CAMERA_FACING_FRONT && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return UNDEFINED_CAMERA_ID;
    }
}
