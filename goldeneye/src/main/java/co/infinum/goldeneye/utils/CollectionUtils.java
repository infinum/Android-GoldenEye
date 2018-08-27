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
        if (flashModeList == null) {
            return new ArrayList<>();
        }

        Set<FlashMode> internalFlashModeSet = new ArraySet<>(flashModeList.size());
        for (String flashMode : flashModeList) {
            internalFlashModeSet.add(FlashMode.fromString(flashMode));
        }

        return new ArrayList<>(internalFlashModeSet);
    }

    @NonNull
    public static List<Size> toSortedInternalSizeList(@Nullable List<Camera.Size> sizeList) {
        if (sizeList == null) {
            return new ArrayList<>();
        }

        List<Size> internalSizeList = new ArrayList<>(sizeList.size());
        for (Camera.Size size : sizeList) {
            internalSizeList.add(new Size(size));
        }

        Collections.sort(internalSizeList);
        return internalSizeList;
    }
}
