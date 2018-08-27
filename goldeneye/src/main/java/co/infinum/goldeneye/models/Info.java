package co.infinum.goldeneye.models;

public enum Info {
    PREVIEW_ALREADY_STARTED("Preview already started. Ignoring startPreview() call."),
    PREVIEW_NOT_STARTED("Preview not started. Ignoring stopPreview() call."),
    FACING_NOT_AVAILABLE("Facing [%s] not available."),
    CAMERA_NOT_INITIALIZED("Camera not initialized."),
    PREVIEW_FAILED_TO_START("Preview failed to start.");

    private String message;

    Info(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
