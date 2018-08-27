package co.infinum.goldeneye.models;

import android.hardware.Camera;
import android.support.annotation.NonNull;

import java.util.Locale;

public class Size implements Comparable<Size> {

    public static final Size UNKNOWN = new Size(0, 0);

    private final int height;
    private final int width;

    public Size(@NonNull Camera.Size size) {
        this(size.width, size.height);
    }

    private Size(int width, int height) {
        this.width = width;
        this.height = height;
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

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isSameAspectRatio(Size other) {
        return Float.compare(getAspectRatio(), other.getAspectRatio()) == 0;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Size [width=%d;height=%d]", width, height);
    }

    private float getAspectRatio() {
        return ((float) width) / height;
    }
}
