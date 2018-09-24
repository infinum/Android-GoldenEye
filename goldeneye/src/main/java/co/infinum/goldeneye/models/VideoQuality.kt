package co.infinum.goldeneye.models

import android.media.CamcorderProfile
import android.os.Build
import android.support.annotation.RequiresApi

enum class VideoQuality(
    val key: Int
) {
    LOW(CamcorderProfile.QUALITY_LOW),
    HIGH(CamcorderProfile.QUALITY_HIGH),
    RESOLUTION_720P(CamcorderProfile.QUALITY_720P),
    RESOLUTION_1080P(CamcorderProfile.QUALITY_1080P),
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    RESOLUTION_2160P(CamcorderProfile.QUALITY_2160P),
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_LOW(CamcorderProfile.QUALITY_HIGH_SPEED_LOW),
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_HIGH(CamcorderProfile.QUALITY_HIGH_SPEED_HIGH),
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_480P(CamcorderProfile.QUALITY_HIGH_SPEED_480P),
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_720P(CamcorderProfile.QUALITY_HIGH_SPEED_720P),
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_1080P(CamcorderProfile.QUALITY_HIGH_SPEED_1080P),
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    HIGH_SPEED_2160P(CamcorderProfile.QUALITY_HIGH_SPEED_2160P),
    UNKNOWN(-1);

    fun isCamera2Required() = listOf(
        RESOLUTION_2160P,
        HIGH_SPEED_LOW,
        HIGH_SPEED_HIGH,
        HIGH_SPEED_480P,
        HIGH_SPEED_720P,
        HIGH_SPEED_1080P,
        HIGH_SPEED_2160P
    ).contains(this)
}