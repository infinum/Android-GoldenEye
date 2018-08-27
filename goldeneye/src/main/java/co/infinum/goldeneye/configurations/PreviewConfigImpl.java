package co.infinum.goldeneye.configurations;

import co.infinum.goldeneye.models.PreviewScale;
import co.infinum.goldeneye.models.PreviewType;

public class PreviewConfigImpl implements PreviewConfig {

    private PreviewScale previewScale;
    private PreviewType previewType;

    PreviewConfigImpl(PreviewType previewType, PreviewScale previewScale) {
        this.previewType = previewType;
        this.previewScale = previewScale;
    }

    @Override
    public PreviewScale getPreviewScale() {
        return previewScale;
    }

    @Override
    public PreviewType getPreviewType() {
        return previewType;
    }

    @Override
    public void setPreviewScale(PreviewScale previewScale) {
        this.previewScale = previewScale;
    }

    @Override
    public void setPreviewType(PreviewType previewType) {
        this.previewType = previewType;
    }
}
