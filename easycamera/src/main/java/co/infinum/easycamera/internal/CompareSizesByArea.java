package co.infinum.easycamera.internal;

import java.util.Comparator;

/**
 * Created by jmarkovic on 28/01/16.
 */
public class CompareSizesByArea implements Comparator<Size> {

    @Override
    public int compare(Size lhs, Size rhs) {
        // We cast here to ensure the multiplications won't overflow
        return Long.signum((long) lhs.getWidth() * lhs.getHeight()
                - (long) rhs.getWidth() * rhs.getHeight());
    }

}
