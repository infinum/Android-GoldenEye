package co.infinum.goldeneye

object MissingCameraPermissionException: Exception("Cannot open camera without Camera permission.")
object PictureConversionException: Exception("Failed to process picture.")
object TaskOnMainThreadException: Exception("Heavy tasks must not be handled on MainThread!")
object CameraInUseException: Exception("Camera is currently in active use and cannot be switched.")