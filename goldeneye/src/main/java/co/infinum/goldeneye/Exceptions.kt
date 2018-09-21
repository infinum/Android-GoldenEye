package co.infinum.goldeneye

object MissingCameraPermissionException: Exception("Cannot open camera without Camera permission.")
object PictureConversionException: Exception("Failed to process picture.")
object TaskOnMainThreadException: Exception("Heavy tasks must not be handled on MainThread!")
object CameraInUseException: Exception("Camera is currently in active use and cannot be switched.")
object CameraLockedException: Exception("Camera is locked. It is recording or taking picture.")
object CameraFailedToOpenException: Exception("For some unknown reason, camera failed to open.")
object IllegalEnumException: Exception("Trying to convert illegal enum to Camera value. This is library error that should be reported.")