package co.infinum.goldeneye;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.TextureView;

import java.io.File;

import co.infinum.goldeneye.configurations.CameraConfig;
import co.infinum.goldeneye.configurations.PreviewConfig;
import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.utils.LoggingUtils;

public interface GoldenEye {

    void initialize(Facing facing, InitializationCallback callback);
    void startPreview(@NonNull TextureView textureView);
    void startPreview(@NonNull TextureView textureView, PreviewConfig previewConfig);
    void stopPreview();
    void takeImage(@NonNull ImageCallback callback);
    void startRecording(@NonNull File file, @NonNull VideoCallback callback);
    void stopRecording();
    @NonNull
    CameraConfig getConfig();

    class Builder {

        private Activity activity;
        private Logger logger;

        public Builder(@NonNull Activity activity) {
            this.activity = activity;
        }

        @NonNull
        public GoldenEye build() {
            LoggingUtils.setLogger(logger);
            return new GoldenEyeImpl(activity);
        }

        @NonNull
        public Builder setLogger(@Nullable Logger logger) {
            this.logger = logger;
            return this;
        }
    }

    interface ImageCallback {

        void onImageTaken(@NonNull Bitmap image);
        void onError(@NonNull Throwable t);
    }

    interface InitializationCallback {

        void onSuccess(@NonNull CameraConfig config);
        void onError(@NonNull Throwable t);
    }

    interface VideoCallback {

        void onVideoRecorded(@NonNull File file);
        void onError(@NonNull Throwable t);
    }
}
