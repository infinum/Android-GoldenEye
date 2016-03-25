package co.infinum.easycamera.internal;

import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Saves a JPEG expressed as {@code byte} array into the specified {@link File}.
 * To be used with {@code Camera1Api}, but can in practice be used with {@code Camera2Api}.
 */
public class ByteImageSaver implements Runnable {

    /**
     * Byte array containing image data.
     */
    private final byte[] imageBytes;

    /**
     * The file we save the image into.
     */
    private final File imageFile;

    /**
     * Save callback listener.
     */
    private OnImageSavedListener listener;

    public ByteImageSaver(byte[] imageBytes, File imageFile) {
        this.imageBytes = imageBytes;
        this.imageFile = imageFile;
    }

    public ByteImageSaver(byte[] imageBytes, File imageFile, OnImageSavedListener listener) {
        this(imageBytes, imageFile);
        this.listener = listener;
    }

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.wrap(this.imageBytes);
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(imageFile);
            output.write(bytes);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            // todo dynamically set orientation based on provided parameters (provide parameter through constructor)
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (listener != null) {
                // send callback back to main thread
                // todo make this configurable?
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
