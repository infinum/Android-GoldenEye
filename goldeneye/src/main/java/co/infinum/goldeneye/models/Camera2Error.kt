package co.infinum.goldeneye.models

import android.hardware.camera2.CameraDevice

/**
 * Camera2 error wrapper.
 */
internal enum class Camera2Error(
    val message: String
) {
    IN_USE("Camera already used by higher-priority camera API client"),
    MAX_CAMERAS_IN_USE("Camera could not open because there are too many other open cameras"),
    DISABLED("Camera could not be opened due to a device policy"),
    DEVICE("Fatal error. Camera needs to be re-opened to be used again"),
    HARDWARE("Hardware error"),
    UNKNOWN("Unknown Camera error happened");

    companion object {
        fun fromInt(errorCode: Int) = when (errorCode) {
            CameraDevice.StateCallback.ERROR_CAMERA_IN_USE -> IN_USE
            CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE -> MAX_CAMERAS_IN_USE
            CameraDevice.StateCallback.ERROR_CAMERA_DISABLED -> DISABLED
            CameraDevice.StateCallback.ERROR_CAMERA_DEVICE -> DEVICE
            CameraDevice.StateCallback.ERROR_CAMERA_SERVICE -> HARDWARE
            else -> UNKNOWN
        }
    }
}