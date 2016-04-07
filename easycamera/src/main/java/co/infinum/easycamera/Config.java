package co.infinum.easycamera;

import android.support.annotation.FloatRange;
import android.text.TextUtils;

/**
 * Used as a config object for {@link CameraApi}.
 */
public class Config {

    final double aspectRatio;
    final double aspectRatioOffset;
    final String filePath;
    final CameraApiCallbacks callbacks;

    private Config(CameraApiCallbacks callbacks, double aspectRatio, double aspectRatioOffset, String filePath) {
        this.aspectRatio = aspectRatio;
        this.aspectRatioOffset = aspectRatioOffset;
        this.filePath = filePath;
        this.callbacks = callbacks;
    }

    public static class Builder {

        private CameraApiCallbacks callbacks;
        private double aspectRatio;
        private double aspectRatioOffset;
        private String imagePath;

        public Builder(CameraApiCallbacks callbacks) {
            this.callbacks = callbacks;
        }

        /**
         * Set requested {@code aspectRatio}. Double value here is
         * a result of larger value divided by smaller value.
         * In, for example, 4:3 aspect ratio, 4 has to be divided by 3.
         * Double value <b>must</b> not be smaller than 1.
         * Default value is 4:3.
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

        public Builder imagePath(String imagePath) {
            if (TextUtils.isEmpty(imagePath)) {
                throw new NullPointerException("ImagePath cannot be null or empty String");
            }

            this.imagePath = imagePath;
            return this;
        }

        public Config build() {
            if (aspectRatio == 0) {
                aspectRatio = 4d / 3d;
            }

            return new Config(callbacks, aspectRatio, aspectRatioOffset, imagePath);
        }
    }
}
