package co.infinum.goldeneye.models;

import android.hardware.Camera;
import android.support.annotation.NonNull;

public class Size implements Comparable<Size> {

    public static final Size UNKNOWN = new Size(0, 0);
    private static final float EPSILON = 0.0001f;
    private final int height;
    private final int width;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Size(Camera.Size size) {
        this(size.width, size.height);
    }

    @Override
    public int compareTo(@NonNull Size o) {
        return o.height * o.width - width * height;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Size
            && ((Size) obj).height == height
            && ((Size) obj).width == width;
    }

    public float getAspectRatio() {
        return ((float) width) / height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isSameAspectRatio(Size other) {
        return Float.compare(getAspectRatio(), other.getAspectRatio()) == 0;
    }
}
