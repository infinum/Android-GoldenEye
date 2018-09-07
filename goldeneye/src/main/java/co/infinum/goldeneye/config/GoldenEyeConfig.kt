package co.infinum.goldeneye.config

import co.infinum.goldeneye.models.PreviewScale

interface GoldenEyeConfig {
    val tapToFocusEnabled: Boolean
    val resetFocusDelay: Long
    val pinchToZoomEnabled: Boolean
    val pinchToZoomFriction: Float
    val previewScale: PreviewScale
    val preferredVideoAspectRatio: Float
    val preferredPreviewAspectRatio: Float
    val preferredPictureAspectRatio: Float

    class Builder {
        private var tapToFocusEnabled = true
        private var resetFocusDelay = 7_500L
        private var pinchToZoomEnabled = true
        private var pinchToZoomFriction = 1f
        private var previewScale = PreviewScale.SCALE_TO_FIT
        private var preferredVideoAspectRatio = -1f
        private var preferredPreviewAspectRatio = -1f
        private var preferredPictureAspectRatio = -1f

        fun setTapToFocusEnabled(enabled: Boolean) = apply { this.tapToFocusEnabled = enabled }
        fun setResetFocusDelay(timeMs: Long) = apply { this.resetFocusDelay = timeMs }
        fun setPinchToZoomEnabled(enabled: Boolean) = apply { this.pinchToZoomEnabled = enabled }
        fun setPinchToZoomFriction(friction: Float) = apply { this.pinchToZoomFriction = friction }
        fun setPreviewScale(previewScale: PreviewScale) = apply { this.previewScale = previewScale }
        fun setPreferredVideoAspectRatio(aspectRatio: Float) = apply { this.preferredVideoAspectRatio = aspectRatio }
        fun setPreferredPreviewAspectRatio(aspectRatio: Float) = apply { this.preferredPreviewAspectRatio = aspectRatio }
        fun setPreferredPictureAspectRatio(aspectRatio: Float) = apply { this.preferredPictureAspectRatio = aspectRatio }
        fun setPreferredAspectRatio(aspectRatio: Float) = apply {
            this.preferredVideoAspectRatio = aspectRatio
            this.preferredPreviewAspectRatio = aspectRatio
            this.preferredPictureAspectRatio = aspectRatio
        }

        fun build() = object : GoldenEyeConfig {
            override val tapToFocusEnabled = this@Builder.tapToFocusEnabled
            override val resetFocusDelay = this@Builder.resetFocusDelay
            override val pinchToZoomEnabled = this@Builder.pinchToZoomEnabled
            override val pinchToZoomFriction = this@Builder.pinchToZoomFriction
            override val previewScale = this@Builder.previewScale
            override val preferredVideoAspectRatio = this@Builder.preferredVideoAspectRatio
            override val preferredPreviewAspectRatio = this@Builder.preferredPreviewAspectRatio
            override val preferredPictureAspectRatio = this@Builder.preferredPictureAspectRatio
        }
    }
}