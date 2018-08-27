package co.infinum.goldeneye.exceptions;

public class NoCameraPermissionException extends Exception {

    public NoCameraPermissionException() {
        super("Camera permission is not granted!");
    }
}
