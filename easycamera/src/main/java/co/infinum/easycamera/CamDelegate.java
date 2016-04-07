package co.infinum.easycamera;

import android.util.Log;

import java.util.List;

import co.infinum.easycamera.internal.Size;

/**
 * Delegate class useful for shared logic on any API level.
 */
class CamDelegate {

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

                if (aspect < config.aspectRatio - config.aspectRatioOffset
                        || aspect > config.aspectRatio + config.aspectRatioOffset) {
                    sizes.remove(i--);
                    continue;
                }


                Log.d("CAM_DELEGATE", "height = " + size.getHeight()
                        + " width = " + size.getWidth()
                        + " aspect = " + aspect);
            }
        }
    }
}
