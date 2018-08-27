package co.infinum.goldeneye.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

import co.infinum.goldeneye.Logger;

public class LoggingUtils {

    private static Logger logger;

    private LoggingUtils() {
        throw new RuntimeException("Utility class should never be instantiated.");
    }

    public static void log(@NonNull String message, Object... args) {
        if (logger != null) {
            if (args.length > 0) {
                logger.log(String.format(Locale.US, message, args));
            } else {
                logger.log(message);
            }
        }
    }

    public static void log(@NonNull Throwable t) {
        if (logger != null) {
            logger.log(t);
        }
    }

    public static void setLogger(@Nullable Logger logger) {
        LoggingUtils.logger = logger;
    }
}
