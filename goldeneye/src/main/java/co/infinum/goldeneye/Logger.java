package co.infinum.goldeneye;

import android.util.Log;

import java.util.Locale;

import co.infinum.goldeneye.models.Info;

public class Logger {

    private static final String TAG = "GoldenEye";

    void info(Info info, String... args) {
        Log.i(TAG, String.format(Locale.US, info.getMessage(), args));
    }
}
