package co.infinum.easycamera.internal;

import android.annotation.TargetApi;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Saves a JPEG {@link Image} into the specified {@link File}.
 * <br /> <br />
 * To be used with {@link Camera2Api}.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ImageSaver implements Runnable {

    /**
     * The JPEG image.
     */
    private final Image image;

    /**
     * The file we save the image into.
     */
    private final File imageFile;

    /**
     * Save callback listener.
     */
    private OnImageSavedListener listener;

    public ImageSaver(Image image, File file) {
        this.image = image;
        imageFile = file;
    }

    public ImageSaver(Image image, File file, OnImageSavedListener listener) {
        this(image, file);
        this.listener = listener;
    }

    @Override
    public void run() {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(imageFile);
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            image.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // notify that file is available after all streams are closed
            if (listener != null) {
                // todo make handler configurable?
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onImageSaved(imageFile);
                    }
                });
            }
        }
    }

}
