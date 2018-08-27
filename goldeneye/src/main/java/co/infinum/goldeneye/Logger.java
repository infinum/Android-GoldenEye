package co.infinum.goldeneye;

import android.support.annotation.NonNull;

public interface Logger {

    void log(@NonNull String message);
    void log(@NonNull Throwable throwable);
}
