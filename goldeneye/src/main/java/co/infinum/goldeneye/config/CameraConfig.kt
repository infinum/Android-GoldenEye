package co.infinum.goldeneye.config

import co.infinum.goldeneye.camera1.config.CameraInfo

interface CameraConfig :
    CameraInfo,
    VideoConfig,
    FeatureConfig,
    SizeConfig,
    ZoomConfig