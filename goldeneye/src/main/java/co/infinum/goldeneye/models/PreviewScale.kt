package co.infinum.goldeneye.models

/**
 * Defines how preview scales on the screen.
 *
 * MANUAL_* scale requires preview size to be set, otherwise the preview
 * will probably be distorted.
 *
 * AUTO_* scale will automatically pick best available preview size that
 * has same aspect ratio as currently chosen picture size or video size.
 *
 * While standard preview is active, it will scale preview by picture size and
 * while video recording is active, it will scale preview by video size.
 */
enum class PreviewScale {
    /**
     * Preview does not scale. Developer must set preview size manually.
     */
    MANUAL,
    /**
     * Preview scales to fit both axes. Developer must set preview size manually.
     */
    MANUAL_FIT,
    /**
     * Preview scales to fill whole view. Developer must set preview size manually.
     */
    MANUAL_FILL,
    /**
     * Preview scales to fit both axes. Preview size is automatically chosen.
     */
    AUTO_FIT,
    /**
     * Preview scales to fill whole view. Preview size is automatically chosen.
     */
    AUTO_FILL
}