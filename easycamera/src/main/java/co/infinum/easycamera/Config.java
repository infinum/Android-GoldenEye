package co.infinum.easycamera;

import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static co.infinum.easycamera.CameraApi.CAMERA_FACING_BACK;

/**
 * Used as a config object for {@link CameraApi}.
 */
public class Config {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FRAME_RATE_30, FRAME_RATE_60})
    @interface FrameRate{}
    public static final int FRAME_RATE_30 = 30;
    public static final int FRAME_RATE_60 = 60;


    final double aspectRatio;
    final double aspectRatioOffset;
    final String imagePath;
    final String videoPath;
    final CameraApiCallbacks callbacks;
    @CameraFacingDef final int cameraFacing;
    @FrameRate final int frameRate;


    private Config(CameraApiCallbacks callbacks, double aspectRatio, double aspectRatioOffset,
            String imagePath, String videoPath, @CameraFacingDef int cameraFacing, @FrameRate int frameRate) {

        this.aspectRatio = aspectRatio;
        this.aspectRatioOffset = aspectRatioOffset;
        this.imagePath = imagePath;
        this.callbacks = callbacks;
        this.videoPath = videoPath;
        this.cameraFacing = cameraFacing;
        this.frameRate = frameRate;
    }

    public static class Builder {

        private CameraApiCallbacks callbacks;
        private double aspectRatio;
        private double aspectRatioOffset;
        private String imagePath;
        private String videoPath;
        private @FrameRate int frameRate = FRAME_RATE_30;

        @CameraFacingDef
        private int cameraFacing = CAMERA_FACING_BACK;

        public Builder (CameraApiCallbacks callbacks) {
            if (callbacks == null) {
                throw new IllegalStateException("Callback provided to Config.Builder cannot be null!");
            }
            this.callbacks = callbacks;
        }

        public Builder(Config config) {
            this.aspectRatio = config.aspectRatio;
            this.aspectRatioOffset = config.aspectRatioOffset;
            this.imagePath = config.imagePath;
            this.callbacks = config.callbacks;
            this.cameraFacing = config.cameraFacing;
            this.frameRate = config.frameRate;
        }

        /**
         * Set requested {@code aspectRatio}. Double value here is
         * a result of larger value divided by smaller value.
         * In, for example, 4:3 aspect ratio, 4 has to be divided by 3.
         * Double value <b>must</b> not be smaller than 1.
         * There is no default value, meaning no filtering will be done.
         */
        public Builder aspectRatio(@FloatRange(from = 1.0, fromInclusive = true) double aspectRatio) {
            if (aspectRatio < 1) {
                throw new IllegalArgumentException("AspectRatio must be larger or equal to 1");
            }

            this.aspectRatio = aspectRatio;
            return this;
        }

        /**
         * Set the {@code offset} which will be tolerable when
         * calculating aspect ratio. Some devices do not have perfect ratio,
         * but are near enough to most used ratios. Given percentage
         */
        public Builder aspectRatioOffset(@FloatRange(from = 0.0, fromInclusive = true) double offset) {
            if (offset < 0) {
                throw new IllegalArgumentException("Offset cannot be a negative.");
            }

            this.aspectRatioOffset = offset;
            return this;
        }

        /**
         * Set fully qualified {@code imagePath} and directory. The path
         * needs a file name and {@code .jpg} extension at the end.
         * Be careful you have full permission to write to the given path.
         * Path should not be null. By default, image is saved
         * to external storage that belongs to the app.
         */
        public Builder imagePath(@NonNull String imagePath) {
            if (TextUtils.isEmpty(imagePath)) {
                throw new NullPointerException("ImagePath cannot be null or empty String");
            }

            this.imagePath = imagePath;
            return this;
        }

        /**
         * Set fully qualified {@code videoPath} and directory. The path
         * needs a file name and {@code .mp4} extension at the end.
         * Be careful you have full permission to write to the given path.
         * Path should not be null. By default, video is saved
         * to external storage that belongs to the app.
         */
        public Builder videoPath(@NonNull String videoPath) {
            if (TextUtils.isEmpty(videoPath)) {
                throw new NullPointerException("Video path cannot be null or empty String");
            }

            this.videoPath = videoPath;
            return this;
        }

        /**
         * Set the {@code cameraFacing} which defines which camera will be opened.
         * @param cameraFacing one of the {@link CameraFacingDef} values
         */
        public Builder cameraFacing(@CameraFacingDef int cameraFacing) {
            this.cameraFacing = cameraFacing;
            return this;
        }

        public Builder videoFrameRate(@FrameRate int frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        public Config build() {
            return new Config(callbacks, aspectRatio, aspectRatioOffset, imagePath, videoPath, cameraFacing, frameRate);
        }
    }
}
