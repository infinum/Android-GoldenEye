package co.infinum.goldeneye.configurations;

import android.support.annotation.NonNull;

import co.infinum.goldeneye.models.PreviewScale;
import co.infinum.goldeneye.models.PreviewType;

public interface PreviewConfig {

    void setPreviewScale(@NonNull PreviewScale previewScale);
    void setPreviewType(@NonNull PreviewType previewType);
    @NonNull
    PreviewType getPreviewType();
    @NonNull
    PreviewScale getPreviewScale();

    class Builder {

        private PreviewScale previewScale = PreviewScale.FIT;
        private PreviewType previewType = PreviewType.IMAGE;

        public PreviewConfig build() {
            return new PreviewConfigImpl(
                this.previewType,
                this.previewScale
            );
        }

        public Builder setPreviewScale(PreviewScale previewScale) {
            this.previewScale = previewScale;
            return this;
        }

        public Builder setPreviewType(PreviewType previewType) {
            this.previewType = previewType;
            return this;
        }
    }
}
