package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.Facing

/**
 * General Camera info used by Camera APIs to open Camera.
 */
interface CameraInfo {
    /**
     * Camera ID.
     */
    val id: String

    /**
     * Camera orientation. Camera has its own orientation that is not in sync
     * with Device orientation.
     */
    val orientation: Int

    /**
     * Camera facing can be either FRONT, BACK or EXTERNAL.
     * EXTERNAL cameras are mostly handled same as BACK cameras.
     */
    val facing: Facing
}