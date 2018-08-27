package co.infinum.goldeneye.exceptions;

public class CameraNotOpenedException extends Exception {

    public CameraNotOpenedException() {
        super("Camera failed to open.");
    }
}
