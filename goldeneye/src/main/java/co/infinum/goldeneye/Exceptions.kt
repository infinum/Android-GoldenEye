package co.infinum.goldeneye

object MissingCameraPermissionException: Exception("Cannot open camera without Camera permission.")
object PictureConversionException: Exception("Failed to process picture.")