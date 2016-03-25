package co.infinum.easycamera;

import android.graphics.Matrix;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Callback interface for {@link CameraApi} operations.
 * Since most operations are asynchronous, data resolved
 * by these operations are provided through method in this callback
 * interface.<br/> <br/>
 *
 * Created by jmarkovic on 25/03/16.
 */
public interface CameraApiCallbacks {

    /**
     * Called when preview size has been resolved. Use this method to set
     * the size of your preview view, or forward the parameters to
     * {@link AutoFitTextureView} if you're using it.
     *
     * @param width  horizontal size of the preview
     * @param height vertical size of the preview
     */
    void onResolvedPreviewSize(final int width, final int height);

    /**
     * Called when surface {@link Matrix} has been configured.
     */
    void onTransformChanged(Matrix matrix);

    /**
     * Triggered whenever there is an error. Provides a descriptive
     * {@code cameraError} object.
     */
    void onCameraError(CameraError cameraError);

    /**
     * Triggered when image has been successfully taken. Provides a file
     * which represents that image. Parameter {@code imageFile} can
     * never be {@code null} - this callback method will not be called
     * if image was not successfully taken.
     */
    void onImageTaken(@NonNull File imageFile);

}
