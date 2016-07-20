package co.infinum.easycamera;

import android.Manifest;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.view.TextureView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jmarkovic on 26/01/16.
 */
public interface CameraApi {

    /**
     * When set, flash will never occur.
     */
    int FLASH_MODE_OFF = 0x0010;

    /**
     * When set, flash will always lit, regardless of the luminosity of the scene.
     */
    int FLASH_MODE_ON = 0x0011;

    /**
     * When set, flash will automatically turn on if the scene is too dark.
     */
    int FLASH_MODE_AUTOMATIC = 0x0012;

    /**
     * The facing of the camera is opposite to that of the screen.
     */
    int CAMERA_FACING_BACK = 0x0013;

    /**
     * The facing of the camera is the same as that of the screen.
     */
    int CAMERA_FACING_FRONT = 0x0014;

    /**
     * Initializes all fields required for using camera API.
     * Returns the calling object for convenience.
     */
    CameraApi init(Activity activity);

    /**
     * Provides the surface on to which the preview will be drawn.
     * This method must not be called before the view holding this texture is attached
     * to the window. Therefore, it is best to call it from a {@link android.view.TextureView.SurfaceTextureListener}.
     */
    void setSurfaceTexture(@NonNull SurfaceTexture surfaceTexture);

    /**
     * Runs the camera API setup and provides a way to set a camera video stream
     * to preview.
     *
     * Calls {@link CameraApiCallbacks#onResolvedPreviewSize(int, int)} once all fields and
     * measures have been initialized.
     *
     * Starts background thread that will handle most of the hardware callbacks.
     *
     * @param desiredWidth  desired width of the preview, may be 0 for default
     * @param desiredHeight desired height of the preview, may be 0 for default
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    void openCamera(final int desiredWidth, final int desiredHeight);

    /**
     * Sets the surface view for camera API and opens the camera
     * with surface view width and height. This is a convenience method
     * for {@link #setSurfaceTexture(SurfaceTexture)}
     * and {@link #openCamera(int, int)}.
     * @param textureView view with surface texture.
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    void openCamera(@NonNull TextureView textureView);

    /**
     * Safely closes the hardware camera, dealing with all callbacks and cleanups
     * needed.
     *
     * Stops background thread as it is no longer needed.
     */
    void closeCamera();

    /**
     * Performs a simple check to see if camera API has a camera open and available.
     * This may be useful if {@link #closeCamera()} is being called from multiple
     * lifecycle callbacks and you want to avoid issue by trying to close the same camera
     * multiple times.
     *
     * @return true if camera is open.
     */
    boolean isCameraActive();

    /**
     * Runs a calculation with pre-initialized data and calculates the optimal
     * preview size, if supported by the API.
     *
     * Calls {@link CameraApiCallbacks#onTransformChanged(Matrix)} once all calculations
     * have been done.
     *
     * This should typically only be called from
     * {@link android.view.TextureView.SurfaceTextureListener#onSurfaceTextureSizeChanged(SurfaceTexture, int, int)}.
     *
     * @param width  new width of the preview
     * @param height new height of the preview
     */
    void updatePreviewDimensions(final int width, final int height);

    /**
     * <b>Currently not implemented</b>
     */
    void setFlashMode(@FlashDef int flashMode);

    /**
     * Starts an AutoFocus process which will try to lock focus on
     * a most dominant object in a visible screen.
     * It is up to hardware to decide which object is the dominant
     * object.
     */
    void acquireFocus(int x, int y);

    /**
     * Starts an asynchronous sequence of operations for taking a single picture.
     * End result should be an image in a file.
     * Picture will be stored in external storage, meaning
     * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE} permission is required.
     */
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void takePicture();

    /**
     * Reopens the camera with a different facing, either CAMERA_FACING_BACK or CAMERA_FACING_FRONT depending on which is currently active.
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    void switchCameraFacing();

    /**
     * Checks whether the device has the specified camera.
     * @param cameraFacing
     * @return true if the cameraFacing has the specified camera.
     */
    boolean hasCameraFacing(@CameraFacingDef int cameraFacing);

    /**
     * Returns the currently active camera facing.
     * @return {@link CameraFacingDef} value
     */
    @CameraFacingDef int getCurrentCameraFacing();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLASH_MODE_OFF, FLASH_MODE_ON, FLASH_MODE_AUTOMATIC})
    @interface FlashDef { }
}
