package co.infinum.goldeneye.configurations;

import co.infinum.goldeneye.models.PreviewScale;
import co.infinum.goldeneye.models.PreviewType;

public interface PreviewConfig {

    void setPreviewScale(PreviewScale previewScale);
    void setPreviewType(PreviewType previewType);
    PreviewType getPreviewType();
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

        public void setPreviewScale(PreviewScale previewScale) {
            this.previewScale = previewScale;
        }

        public void setPreviewType(PreviewType previewType) {
            this.previewType = previewType;
        }
    }
}
