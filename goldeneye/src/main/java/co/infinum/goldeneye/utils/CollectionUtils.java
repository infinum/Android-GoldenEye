package co.infinum.goldeneye.utils;

import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import co.infinum.goldeneye.models.FlashMode;
import co.infinum.goldeneye.models.FocusMode;
import co.infinum.goldeneye.models.Size;

public class CollectionUtils {

    @Nullable
    public static Size findFirstSize(@Nullable List<Camera.Size> sizeList, @NonNull Size referenceSize) {
        for (Size size : toSortedInternalSizeList(sizeList)) {
            if (referenceSize.isSameAspectRatio(size)) {
                return size;
            }
        }
        return null;
    }

    @NonNull
    public static List<FlashMode> toInternalFlashModeList(@Nullable List<String> flashModeList) {
        return toInternalDistinctList(flashModeList, new Function<String, FlashMode>() {
            @Override
            public FlashMode invoke(String arg) {
                return FlashMode.fromString(arg);
            }
        });
    }

    @NonNull
    public static List<FocusMode> toInternalFocusModeList(@Nullable List<String> focusModeList) {
        return toInternalDistinctList(focusModeList, new Function<String, FocusMode>() {
            @Override
            public FocusMode invoke(String arg) {
                return FocusMode.fromString(arg);
            }
        });
    }

    @NonNull
    public static List<Size> toSortedInternalSizeList(@Nullable List<Camera.Size> sizeList) {
        List<Size> internalSizeList = toInternalList(sizeList, new Function<Camera.Size, Size>() {
            @Override
            public Size invoke(Camera.Size arg) {
                return new Size(arg);
            }
        });

        Collections.sort(internalSizeList);
        return internalSizeList;
    }

    @NonNull
    private static <In, Out> List<Out> toInternalDistinctList(List<In> inList, Function<In, Out> convert) {
        if (inList == null) {
            return new ArrayList<>();
        }

        Set<Out> outSet = new ArraySet<>(inList.size());
        for (In in : inList) {
            outSet.add(convert.invoke(in));
        }
        return new ArrayList<>(outSet);
    }

    @NonNull
    private static <In, Out> List<Out> toInternalList(@Nullable List<In> inList, Function<In, Out> convert) {
        if (inList == null) {
            return new ArrayList<>();
        }

        List<Out> outList = new ArrayList<>(inList.size());
        for (In in : inList) {
            outList.add(convert.invoke(in));
        }

        return outList;
    }
}
