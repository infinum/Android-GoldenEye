package co.infinum.goldeneye.models

import android.media.CamcorderProfile
import android.os.Build
import android.support.annotation.RequiresApi

enum class VideoQuality(
    val key: Int
) {
    /**
     * @see CamcorderProfile.QUALITY_LOW
     */
    LOW(CamcorderProfile.QUALITY_LOW),
    /**
     * @see CamcorderProfile.QUALITY_HIGH
     */
    HIGH(CamcorderProfile.QUALITY_HIGH),
    /**
     * @see CamcorderProfile.QUALITY_480P
     */
    RESOLUTION_480P(CamcorderProfile.QUALITY_480P),
    /**
     * @see CamcorderProfile.QUALITY_QVGA
     */
    RESOLUTION_QVGA(CamcorderProfile.QUALITY_QVGA),
    /**
     * @see CamcorderProfile.QUALITY_720P
     */
    RESOLUTION_720P(CamcorderProfile.QUALITY_720P),
    /**
     * @see CamcorderProfile.QUALITY_1080P
     */
    RESOLUTION_1080P(CamcorderProfile.QUALITY_1080P),
    /**
     * @see CamcorderProfile.QUALITY_2160P
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    RESOLUTION_2160P(CamcorderProfile.QUALITY_2160P),
    /**
     * @see CamcorderProfile.QUALITY_HIGH_SPEED_LOW
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_LOW(CamcorderProfile.QUALITY_HIGH_SPEED_LOW),
    /**
     * @see CamcorderProfile.QUALITY_HIGH_SPEED_HIGH
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_HIGH(CamcorderProfile.QUALITY_HIGH_SPEED_HIGH),
    /**
     * @see CamcorderProfile.QUALITY_HIGH_SPEED_480P
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_480P(CamcorderProfile.QUALITY_HIGH_SPEED_480P),
    /**
     * @see CamcorderProfile.QUALITY_HIGH_SPEED_720P
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_720P(CamcorderProfile.QUALITY_HIGH_SPEED_720P),
    /**
     * @see CamcorderProfile.QUALITY_HIGH_SPEED_1080P
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_1080P(CamcorderProfile.QUALITY_HIGH_SPEED_1080P),
    /**
     * @see CamcorderProfile.QUALITY_HIGH_SPEED_2160P
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_2160P(CamcorderProfile.QUALITY_HIGH_SPEED_2160P),
    UNKNOWN(-1);

    internal fun isCamera2Required() = listOf(
        RESOLUTION_2160P,
        HIGH_SPEED_LOW,
        HIGH_SPEED_HIGH,
        HIGH_SPEED_480P,
        HIGH_SPEED_720P,
        HIGH_SPEED_1080P,
        HIGH_SPEED_2160P
    ).contains(this)
}