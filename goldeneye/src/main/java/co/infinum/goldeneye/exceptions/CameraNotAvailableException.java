package co.infinum.goldeneye.exceptions;

public class CameraNotAvailableException extends Exception {

    public CameraNotAvailableException() {
        super("Device has no available cameras to use.");
    }
}
