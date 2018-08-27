package co.infinum.goldeneye.configurations;

import android.support.annotation.NonNull;

import co.infinum.goldeneye.models.PreviewScale;
import co.infinum.goldeneye.models.PreviewType;

public class PreviewConfigImpl implements PreviewConfig {

    private PreviewScale previewScale;
    private PreviewType previewType;

    PreviewConfigImpl(PreviewType previewType, PreviewScale previewScale) {
        this.previewType = previewType;
        this.previewScale = previewScale;
    }

    @NonNull
    @Override
    public PreviewScale getPreviewScale() {
        return previewScale;
    }

    @NonNull
    @Override
    public PreviewType getPreviewType() {
        return previewType;
    }

    @Override
    public void setPreviewScale(@NonNull PreviewScale previewScale) {
        this.previewScale = previewScale;
    }

    @Override
    public void setPreviewType(@NonNull PreviewType previewType) {
        this.previewType = previewType;
    }
}
