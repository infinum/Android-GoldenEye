package co.infinum.goldeneye

object MissingCameraPermissionException: Exception("Cannot open camera without Camera permission.")
object PictureConversionException: Exception("Failed to process picture.")
object TaskOnMainThreadException: Exception("Heavy tasks must not be handled on MainThread!")
object CameraInUseException: Exception("Camera is currently in active use and cannot be switched.")
object CameraFailedToOpenException: Exception("For some unknown reason, camera failed to open.")
object IllegalEnumException: Exception("Trying to convert illegal enum to Camera value. This is library error that should be reported.")
object ThreadNotStartedException: Exception("Trying to fetch [backgroundHandler] but background Thread is not started.")
object CameraConfigurationFailedException: Exception("For some unknown reason, camera configuration failed.")
object ExternalVideoRecordingNotSupportedException: Exception("GoldenEye does not support video recording for external cameras.")
class CameraNotReadyException: Exception("Camera is currently not ready. State = [${BaseGoldenEyeImpl.state}]")
