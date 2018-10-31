package co.infinum.goldeneye

object MissingCameraPermissionException :
    Exception("Cannot open camera without Camera permission.")

object PictureConversionException :
    Exception("Failed to process picture.")

object TaskOnMainThreadException :
    Exception("Heavy tasks must not be handled on MainThread!")

object MediaRecorderDeadException:
    Exception("Media recorder died for unknown reason.")

object CameraFailedToOpenException :
    Exception("For some unknown reason, camera failed to open.")

object IllegalEnumException :
    Exception("Trying to convert illegal enum to Camera value. This is library error that should be reported.")

object ThreadNotStartedException :
    Exception("Trying to fetch [backgroundHandler] but background Thread is not started.")

object CameraConfigurationFailedException :
    Exception("For some unknown reason, camera configuration failed.")

object ExternalVideoRecordingNotSupportedException :
    Exception("GoldenEye does not support video recording for external cameras.")

object IllegalCharacteristicsException :
    Exception("Camera Characteristics are [NULL]")

object CameraConfigNotAvailableException :
    Exception("Camera configuration is not available. Be sure to wait for InitCallback.onReady callback.")

class CameraNotActiveException :
    Exception("Camera is currently not active. State = [${BaseGoldenEye.state}]")