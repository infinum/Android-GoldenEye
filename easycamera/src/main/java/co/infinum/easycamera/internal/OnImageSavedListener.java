package co.infinum.easycamera.internal;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Callback listener which will notify the host when the image
 * has been successfully saved. Does not notify any errors.
 */
public interface OnImageSavedListener {

    /**
     * Called when the image has been successfully saved to a file.
     *
     * @param imageFile file in which the image has been saved.
     */
    void onImageSaved(@NonNull File imageFile);

}
