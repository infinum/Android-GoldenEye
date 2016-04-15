package co.infinum.easycamera;

import android.support.annotation.IntDef;

import java.util.List;

import co.infinum.easycamera.internal.Size;

/**
 * Delegate class useful for shared logic on any API level.
 */
class CamDelegate {

    public static final int ASPECT_UNKNOWN = 0;
    public static final int ASPECT_WITHIN_BOUNDS = -1;
    public static final int ASPECT_OUT_OF_BOUNDS = -2;

    @IntDef({
            ASPECT_UNKNOWN,
            ASPECT_WITHIN_BOUNDS,
            ASPECT_OUT_OF_BOUNDS
    })
    public @interface AspectRatioDef { }

    private final Config config;

    public CamDelegate(Config config) {
        this.config = config;
    }

    /**
     * Filters {@code sizes} so that only sizes ... TODO
     */
    public void filterAspect(List<Size> sizes) {
        if (config.aspectRatio > 0) {
            for (int i = 0; i < sizes.size(); i++) {
                final Size size = sizes.get(i);
                final int bigger = size.getHeight() > size.getWidth() ? size.getHeight() : size.getWidth();
                final int smaller = size.getHeight() < size.getWidth() ? size.getHeight() : size.getWidth();

                final double aspect = (double) bigger / (double) smaller;

                if (aspect <= config.aspectRatio - config.aspectRatioOffset
                        || aspect >= config.aspectRatio + config.aspectRatioOffset) {
                    sizes.remove(i--);
                }
            }
        }
    }

    /**
     * todo
     */
    @AspectRatioDef
    public int isAspectWithinBounds(final double aspectRatio) {
        if (config.aspectRatio > 0) {
            if (aspectRatio >= config.aspectRatio - config.aspectRatioOffset
                    && aspectRatio <= config.aspectRatio + config.aspectRatioOffset) {
                return ASPECT_WITHIN_BOUNDS;
            } else {
                return ASPECT_OUT_OF_BOUNDS;
            }
        } else {
            return ASPECT_UNKNOWN;
        }
    }
}
