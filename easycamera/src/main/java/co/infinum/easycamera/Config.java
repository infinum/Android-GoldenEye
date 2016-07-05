package co.infinum.easycamera;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import static co.infinum.easycamera.CameraApi.CAMERA_FACING_BACK;

/**
 * Used as a config object for {@link CameraApi}.
 */
public class Config {

    final double aspectRatio;
    final double aspectRatioOffset;
    final String filePath;
    final CameraApiCallbacks callbacks;

    @CameraApi.CameraFacingDef
    final int cameraFacing;

    private Config(CameraApiCallbacks callbacks, double aspectRatio, double aspectRatioOffset, String filePath,
            @CameraApi.CameraFacingDef int cameraFacing) {
        this.aspectRatio = aspectRatio;
        this.aspectRatioOffset = aspectRatioOffset;
        this.filePath = filePath;
        this.callbacks = callbacks;
        this.cameraFacing = cameraFacing;
    }

    public static class Builder {

        private CameraApiCallbacks callbacks;
        private double aspectRatio;
        private double aspectRatioOffset;
        private String imagePath;

        @CameraApi.CameraFacingDef
        private int cameraFacing = CAMERA_FACING_BACK;

        public Builder(CameraApiCallbacks callbacks) {
            this.callbacks = callbacks;
        }

        public Builder(Config config) {
            this.aspectRatio = config.aspectRatio;
            this.aspectRatioOffset = config.aspectRatioOffset;
            this.imagePath = config.filePath;
            this.callbacks = config.callbacks;
            this.cameraFacing = config.cameraFacing;
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

        public Builder cameraFacing(@CameraApi.CameraFacingDef int cameraFacing) {
            this.cameraFacing = cameraFacing;
            return this;
        }

        public Config build() {
            return new Config(callbacks, aspectRatio, aspectRatioOffset, imagePath, cameraFacing);
        }
    }
}
