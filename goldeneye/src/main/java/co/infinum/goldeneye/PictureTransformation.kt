package co.infinum.goldeneye

import android.graphics.Bitmap
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.extensions.applyMatrix
import co.infinum.goldeneye.extensions.mirror
import co.infinum.goldeneye.extensions.rotate
import co.infinum.goldeneye.models.Facing

interface PictureTransformation {
    fun transform(picture: Bitmap, config: CameraConfig, orientationDifference: Float): Bitmap

    object Default : PictureTransformation {
        override fun transform(picture: Bitmap, config: CameraConfig, orientationDifference: Float) =
            picture.applyMatrix {
                val cx = picture.width / 2f
                val cy = picture.height / 2f
                val degrees = if (config.facing == Facing.BACK) orientationDifference else -orientationDifference
                rotate(degrees, cx, cy)
                if (config.facing == Facing.FRONT) {
                    mirror()
                }
            }
    }
}