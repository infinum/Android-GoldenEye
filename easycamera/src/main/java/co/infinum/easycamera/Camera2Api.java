package co.infinum.easycamera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import co.infinum.easycamera.internal.CompareSizesByArea;
import co.infinum.easycamera.internal.ImageSaver;
import co.infinum.easycamera.internal.OnImageSavedListener;
import co.infinum.easycamera.internal.Size;

/**
 * Created by jmarkovic on 26/01/16.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Api implements CameraApi {

    private static final String TAG = "Camera2Api";

    private static final int MAX_IMAGES = 2;

    private static final int MAX_PASSIVE_FOCUSED_PASS_THROUGH = 5;

    /**
     * Lock timeout in millis.
     */
    private static final long LOCK_ACQUIRE_TIMEOUT = 2500;

    private static final int ROTATION_0 = 0;

    private static final int ROTATION_90 = 90;

    private static final int ROTATION_180 = 180;

    private static final int ROTATION_270 = 270;

    private static final int METERING_SIZE = 100;

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked, taking picture after it has been locked
     */
    private static final int STATE_WAITING_LOCK_PIC = 1;

    /**
     * Camera state: waiting for the focus to be locked
     */
    private static final int STATE_WAITING_LOCK = 2;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRE_CAPTURE = 3;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 4;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 5;

    /**
     * Max preview width that is guaranteed by Camera2 API.
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API.
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
     * Whether the current camera device supports Flash or not.
     */
    private boolean flashSupported;

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String cameraId;

    /**
     * Display size in pixel.
     */
    private final Point displaySize = new Point();

    /**
     * Directory in which taken images will be saved.
     */
    private File storageDirectory;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader imageReader;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice cameraDevice;

    /**
     * CameraManager object retrieved from host context.
     */
    private CameraManager cameraManager;

    /**
     * {@link CaptureRequest.Builder} for the camera preview.
     */
    private CaptureRequest.Builder previewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #previewRequestBuilder}.
     */
    private CaptureRequest previewRequest;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession captureSession;

    /**
     * Surface where the actual preview will be drawn.
     */
    private SurfaceTexture surfaceTexture;

    /**
     * The {@link Size} of camera preview.
     */
    private Size previewSize;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread backgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler backgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #captureCallback
     */
    private int state = STATE_PREVIEW;

    /**
     * Rectangle that represents part of the screen on which
     * the camera should be focused on.
     * If this is {@code null}, camera will try to AutoFocus
     * depending on scenery at the point the image is being captured.
     */
    private MeteringRectangle meteringRectangle;

    /**
     * Some devices only report passive focus lock.
     * For these devices, it is best to leave it be
     * and just take the image at the point of user interaction.
     */
    private int passiveFocusedWorkaround = 0;

    /**
     * Flag controlling the state of the camera. If true, {@link #openCamera(int, int)} does nothing.
     * Reset at {@link #closeCamera()}.
     */
    private boolean isCameraActive;

    /**
     * Current flash mode or most recently selected (e.g. after one picture is taken and camera has to be setup for the new shot,
     * flash mode will be the same as for last taken image instead of being reset to default value).
     */
    @FlashDef
    private int currentFlashMode = FLASH_MODE_AUTOMATIC;

    /**
     * This is a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            // This is the output file for our picture.
            File imageFile;
            if (TextUtils.isEmpty(config.filePath)) {
                imageFile = new File(storageDirectory, String.format(Locale.getDefault(), "%d.jpg", System.currentTimeMillis()));
            } else {
                imageFile = new File(config.filePath);
            }

            backgroundHandler.post(new ImageSaver(reader.acquireNextImage(), imageFile, imageSavedListener));
        }
    };

    /**
     * This is a callback object for the {@link ImageSaver}.
     * {@link OnImageSavedListener#onImageSaved(File)}
     * will be called once the image has been successfully saved to a file.
     */
    private final OnImageSavedListener imageSavedListener = new OnImageSavedListener() {
        @Override
        public void onImageSaved(@NonNull File imageFile) {
            config.callbacks.onImageTaken(imageFile);
        }
    };

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraOpenCloseLock.release();
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraOpenCloseLock.release();
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            cameraDevice = null;

            CameraError cameraError;
            switch (error) {
                case ERROR_CAMERA_IN_USE:
                case ERROR_MAX_CAMERAS_IN_USE:
                    cameraError = new CameraError(CameraError.ERROR_CAMERA_IN_USE);
                    break;
                case ERROR_CAMERA_SERVICE:
                case ERROR_CAMERA_DISABLED:
                case ERROR_CAMERA_DEVICE:
                default:
                    cameraError = new CameraError(CameraError.ERROR_MISSING_SYSTEM_FEATURE);
                    break;
            }

            config.callbacks.onCameraError(cameraError);
        }
    };

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

        @WorkerThread
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @WorkerThread
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }

        private void process(CaptureResult result) {
            switch (state) {
                case STATE_WAITING_LOCK:
                case STATE_WAITING_LOCK_PIC:
                    // lock has been requested, time before lock is done depends on the hardware
                    final Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    Log.d(TAG, "afState -> " + afState);
                    if (afState == null || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState) {
                        if (STATE_WAITING_LOCK_PIC == state) {
                            captureStillPicture();
                            state = STATE_PICTURE_TAKEN;
                        } else {
                            // focus has been acquired, release it
                            unlockFocus();
                        }
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        if (STATE_WAITING_LOCK_PIC == state) {
                            final Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                            if (aeState == null || CaptureResult.CONTROL_AE_STATE_CONVERGED == aeState) {
                                state = STATE_PICTURE_TAKEN;
                                captureStillPicture();
                            } else {
                                runPreCaptureSequence();
                            }
                        } else {
                            // focus has been acquired, release it
                            unlockFocus();
                        }
                    } else if (afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED) {
                        if (passiveFocusedWorkaround++ >= MAX_PASSIVE_FOCUSED_PASS_THROUGH) {
                            if (STATE_WAITING_LOCK_PIC == state) {
                                // assume focus lock and take picture
                                state = STATE_PICTURE_TAKEN;
                                captureStillPicture();
                            } else if (STATE_WAITING_LOCK == state) {
                                unlockFocus();

                            }
                        }
                    }
                    break;

                case STATE_WAITING_PRE_CAPTURE:
                    // CONTROL_AE_STATE can be null on some devices
                    final Integer aeStatePreCapture = result.get(CaptureResult.CONTROL_AE_STATE);
                    Log.d(TAG, "aeStatePreCapture -> " + String.valueOf(aeStatePreCapture));
                    if (aeStatePreCapture == null
                            || CaptureResult.CONTROL_AE_STATE_PRECAPTURE == aeStatePreCapture
                            || CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED == aeStatePreCapture) {
                        // for special set of devices, other will come to the proper state
                        state = STATE_WAITING_NON_PRE_CAPTURE;
                    } else if (aeStatePreCapture == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                        state = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;

                case STATE_WAITING_NON_PRE_CAPTURE:
                    // CONTROL_AE_STATE can be null on some devices
                    final Integer aeStateNonPreCapture = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeStateNonPreCapture == null || aeStateNonPreCapture != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;

                case STATE_PICTURE_TAKEN:
                    // picture taken, onImageAvailableListener will be triggered at this point
                    Log.d(TAG, "picture taken");
                    unlockFocus();
                    break;

                default:
                case STATE_PREVIEW:
                    // no op - cameraApi is in preview mode, nothing has to be done
                    break;
            }
        }

    };

    Camera2Api(Config config) {
        this.config = config;
        this.camDelegate = new CamDelegate(config);
    }

    @Override
    public CameraApi init(Activity activity) {
        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            orientation = activity.getResources().getConfiguration().orientation;
            storageDirectory = activity.getExternalFilesDir(null);
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        } else {
            // feature is unavailable, quit everything
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

        setUpCameraOutputs(desiredWidth, desiredHeight);
        configureTransform(desiredWidth, desiredHeight);

        try {
            if (!cameraOpenCloseLock.tryAcquire(LOCK_ACQUIRE_TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            // noinspection ResourceType
            cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
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

                passiveFocusedWorkaround = 0;
                if (null != captureSession) {
                    captureSession.close();
                    captureSession = null;
                }
                if (null != cameraDevice) {
                    cameraDevice.close();
                    cameraDevice = null;
                }
                if (null != imageReader) {
                    imageReader.close();
                    imageReader = null;
                }
                if (null != meteringRectangle) {
                    meteringRectangle = null;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
            } finally {
                cameraOpenCloseLock.release();
                stopBackgroundThreads();
                isCameraActive = false;
            }
        }
    }

    @Override
    public boolean isCameraActive() {
        return this.isCameraActive;
    }

    @Override
    public void updatePreviewDimensions(int width, int height) {
        configureTransform(width, height);
    }

    @Override
    public void setFlashMode(@FlashDef int flashMode) {
        setFlashModeToRequestBuilder(previewRequestBuilder, flashMode);
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
        try {
            Log.d(TAG, "acquire focus");

            this.meteringRectangle = new MeteringRectangle(
                    x - METERING_SIZE / 2,
                    y - METERING_SIZE / 2,
                    x + METERING_SIZE / 2,
                    y + METERING_SIZE / 2, 500);
            Log.d(TAG, "metering rect -> " + String.valueOf(this.meteringRectangle));
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS,
                    new MeteringRectangle[]{this.meteringRectangle});
            // Tell the camera to lock focus.
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_AUTO);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            state = STATE_WAITING_LOCK;
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasCameraFacing(@CameraFacingDef int cameraFacing) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null) {
                    if (cameraFacing == CAMERA_FACING_BACK && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        return true;
                    } else if (cameraFacing == CAMERA_FACING_FRONT && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        return true;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
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
        int desiredWidth = previewSize.getWidth();
        int desiredHeight = previewSize.getHeight();

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
        lockFocus();
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null) {
                    if (facing == CameraCharacteristics.LENS_FACING_BACK && config.cameraFacing == CAMERA_FACING_BACK
                            || facing == CameraCharacteristics.LENS_FACING_FRONT && config.cameraFacing == CAMERA_FACING_FRONT) {
                        // stream configuration; if this camera does not support it, skip it
                        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        if (map == null) {
                            continue;
                        }

                        android.util.Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);
                        List<Size> internalOutputSizes = new ArrayList<>(outputSizes.length);
                        convertUtilSizeArrayToInternalSizeArray(outputSizes, internalOutputSizes);

                        // filter sizes per aspect ratio only if
                        camDelegate.filterAspect(internalOutputSizes);

                        // For still image captures, we use the largest available size.
                        Size largest = Collections.max(internalOutputSizes, new CompareSizesByArea());
                        imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, MAX_IMAGES);
                        imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler);

                        // Find out if we need to swap dimension to get the preview size relative to sensor coordinate.
                        // If sensor orientation and screen orientation are not aligned, properly align them.
                        final int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                        boolean swappedDimensions = false;
                        switch (displayRotation) {
                            case Surface.ROTATION_0:
                            case Surface.ROTATION_180:
                                if (sensorOrientation == ROTATION_90 || sensorOrientation == ROTATION_270) {
                                    swappedDimensions = true;
                                }
                                break;
                            case Surface.ROTATION_90:
                            case Surface.ROTATION_270:
                                if (sensorOrientation == ROTATION_0 || sensorOrientation == ROTATION_180) {
                                    swappedDimensions = true;
                                }
                                break;
                            default:
                                Log.e(TAG, String.format("Display rotation is invalid: %d", displayRotation));
                        }

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

                        // Attempting to use too large a preview size could  exceed the camera
                        // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                        // garbage capture data.
                        previewSize = chooseOptimalSize(internalOutputSizes,
                                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                                maxPreviewHeight, largest);

                        // We fit the aspect ratio of TextureView to the size of preview we picked.
                        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            config.callbacks.onResolvedPreviewSize(previewSize.getWidth(), previewSize.getHeight());
                        } else {
                            config.callbacks.onResolvedPreviewSize(previewSize.getHeight(), previewSize.getWidth());
                        }

                        flashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) != null;

                        this.cameraId = cameraId;
                        return;
                    }
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            Log.e(TAG, String.format(
                    "NPE thrown when trying to setup Camera2 API. Current device probably does not have the API. SDK_INT -> %d",
                    Build.VERSION.SDK_INT));
            config.callbacks.onCameraError(new CameraError(CameraError.ERROR_MISSING_SYSTEM_FEATURE));
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
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

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            // We configure the size of default buffer to be the size of camera preview we want.
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(surfaceTexture);

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                setFlashModeToRequestBuilder(previewRequestBuilder, currentFlashMode);

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                            config.callbacks.onCameraError(new CameraError(CameraError.ERROR_CAMERA_CONFIGURATION));
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setFlashModeToRequestBuilder(CaptureRequest.Builder requestBuilder, @FlashDef int flashMode) {
        if (flashSupported) {
            int controlAeMode;
            switch (flashMode) {
                case FLASH_MODE_AUTOMATIC:
                    controlAeMode = CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;
                    break;

                case FLASH_MODE_OFF:
                    controlAeMode = CaptureRequest.CONTROL_AE_MODE_ON;
                    break;

                case FLASH_MODE_ON:
                    controlAeMode = CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
                    break;

                default:
                    Log.e(TAG,
                            String.format("Unrecognized flash mode set, skipping this option and using default. [mode -> %d]", flashMode));
                    controlAeMode = CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;
                    break;
            }

            currentFlashMode = flashMode;
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, controlAeMode);
        }
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        Log.d(TAG, "lockFocus");
        try {
            if (this.meteringRectangle == null) {
                // Tell the camera to lock focus.
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                        CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START);
                // Tell #mCaptureCallback to wait for the lock.
                state = STATE_WAITING_LOCK_PIC;
                captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
            } else {
                runPreCaptureSequence();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            Log.d(TAG, "unlockFocus");
            // Reset the auto-focus trigger
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            state = STATE_PREVIEW;
            captureSession.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the pre-capture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #captureCallback} from {@link #lockFocus()}.
     */
    private void runPreCaptureSequence() {
        try {
            // Trigger camera pre-capture sequence
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell captureCallback to wait for the pre-capture sequence to be set.
            state = STATE_WAITING_PRE_CAPTURE;
            captureSession.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param cameraCharacteristics a {@link CameraCharacteristics object for the target camera}
     * @param deviceOrientation     device orientation in degrees
     * @return The clockwise rotation angle in degrees, relative to the orientation
     * to the camera, that the JPEG picture needs to be rotated by, to be viewed
     * upright. See {@link CaptureRequest#JPEG_ORIENTATION}.
     */
    private int getJpegOrientation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) {
            return 0;
        }
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) {
            deviceOrientation = -deviceOrientation;
        }

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #captureCallback} from both {@link #lockFocus()}.
     */
    @WorkerThread
    private void captureStillPicture() {
        try {
            if (null == cameraDevice) {
                return;
            }
            // CaptureRequest.Builder used to take a picture.
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, previewRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE));
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, previewRequestBuilder.get(CaptureRequest.CONTROL_AE_MODE));

            // Orientation
            int jpegOrientation;
            int displayRotationDegrees;

            switch (displayRotation) {
                case Surface.ROTATION_0:
                    displayRotationDegrees = ROTATION_0;
                    break;

                case Surface.ROTATION_180:
                    displayRotationDegrees = ROTATION_180;
                    break;

                /**
                 * 90 and 270 are switched because {@link Display#getRotation()} returns the
                 * 'rotation of the drawn graphics on the screen, which is the opposite direction of the physical rotation of the device'
                 */
                case Surface.ROTATION_90:
                    displayRotationDegrees = ROTATION_270;
                    break;

                case Surface.ROTATION_270:
                    displayRotationDegrees = ROTATION_90;
                    break;

                default:
                    Log.e(TAG, String.format("Unknown display rotation, canceling procedure. [displayRotation -> %d]", displayRotation));
                    return;
            }

            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            jpegOrientation = getJpegOrientation(characteristics, displayRotationDegrees);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);

            captureSession.stopRepeating();
            captureSession.capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThreads() {
        backgroundThread = new HandlerThread("camera-2-api");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThreads() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts non-supported size object from Camera2 api to supported internal size object.
     *
     * @param utilSize     {@link android.util.Size} array
     * @param internalSize {@link Size} array
     */
    private void convertUtilSizeArrayToInternalSizeArray(android.util.Size[] utilSize,
            List<co.infinum.easycamera.internal.Size> internalSize) {
        for (int i = 0; i < utilSize.length; i++) {
            internalSize.add(convertUtilSizeToInternalSize(utilSize[i]));
        }
    }

    private co.infinum.easycamera.internal.Size convertUtilSizeToInternalSize(android.util.Size utilSize) {
        return new co.infinum.easycamera.internal.Size(utilSize.getWidth(), utilSize.getHeight());
    }

}
