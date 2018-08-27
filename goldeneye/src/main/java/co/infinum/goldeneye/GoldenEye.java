package co.infinum.goldeneye;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.TextureView;

import java.io.File;

import co.infinum.goldeneye.configurations.CameraConfig;
import co.infinum.goldeneye.configurations.PreviewConfig;
import co.infinum.goldeneye.models.Facing;

public interface GoldenEye {

    void init(Facing facing, InitializationCallback callback);
    void startPreview(@NonNull TextureView textureView);
    void startPreview(@NonNull TextureView textureView, PreviewConfig previewConfig);
    void stopPreview();
    void takeImage(@NonNull ImageCallback callback);
    void startRecording(@NonNull File file, @NonNull VideoCallback callback);
    void stopRecording();
    CameraConfig getConfig();

    interface ImageCallback {

        void onImageTaken(Bitmap image);
        void onError(Throwable t);
    }

    interface InitializationCallback {

        void onSuccess(CameraConfig configuration);
        void onError(Throwable t);
    }

    interface VideoCallback {

        void onVideoRecorded(File file);
        void onError(Throwable t);
    }
}
