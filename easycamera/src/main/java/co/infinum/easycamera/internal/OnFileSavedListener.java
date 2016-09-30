package co.infinum.easycamera.internal;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Callback listener which will notify the host when the file
 * has been successfully saved. Does not notify any errors.
 */
public interface OnFileSavedListener {

    /**
     * Called when the image has been successfully saved to a file.
     *
     * @param file in which the image has been saved.
     */
    void onImageSaved(@NonNull File file);

    /**
     * Called when the video has been successfully saved to a file.
     *
     * @param file in which the video has been saved.
     */
    void onVideoSaved(@NonNull File file);
}
