package co.infinum.goldeneye

import android.graphics.Bitmap
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.applyMatrix
import co.infinum.goldeneye.extensions.mirror
import co.infinum.goldeneye.extensions.rotate
import co.infinum.goldeneye.models.Facing
import co.infinum.goldeneye.utils.LogDelegate
import co.infinum.goldeneye.utils.LogDelegate.log
import kotlin.math.log

/**
 * Implement picture transformation after picture is taken.
 *
 * [transform] is called after [PictureCallback.onShutter] and before [PictureCallback.onPictureTaken].
 * Bitmap returned from [transform] is delegated to [PictureCallback.onPictureTaken].
 *
 * Keep in mind that when working with Bitmaps, [OutOfMemoryError] is something
 * that can easily happen!
 */
interface PictureTransformation {

    /**
     * @param picture is original bitmap returned by the camera. It is rotated for [CameraConfig.orientation]
     * degrees and mirrored if front camera is used.
     * @param config current camera config that can be used to calculate and measure necessary transformations.
     * @param orientationDifference represents orientation difference between device and camera.
     *
     * NOTE: If [OutOfMemoryError] happens, original bitmap is returned!
     *
     * @return bitmap that will be delegated to [PictureCallback.onPictureTaken]
     */
    fun transform(picture: Bitmap, config: CameraConfig, orientationDifference: Float): Bitmap

    /**
     * Default [PictureTransformation] implementation. It rotates the image to keep in sync with
     * device orientation and mirrors the image if front camera is used.
     *
     * In case of error, original bitmap is returned.
     */
    object Default : PictureTransformation {
        override fun transform(picture: Bitmap, config: CameraConfig, orientationDifference: Float) =
            try {
                picture.applyMatrix {
                    val cx = picture.width / 2f
                    val cy = picture.height / 2f
                    val degrees = if (config.facing == Facing.FRONT) -orientationDifference else orientationDifference
                    rotate(degrees, cx, cy)
                    if (config.facing == Facing.FRONT) {
                        mirror()
                    }
                }
            } catch (t: Throwable) {
                log("Failed to transform picture. Returning raw picture.", t)
                picture
            }
    }
}