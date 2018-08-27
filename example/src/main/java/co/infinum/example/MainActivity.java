package co.infinum.example;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;

import co.infinum.goldeneye.GoldenEye;
import co.infinum.goldeneye.Logger;
import co.infinum.goldeneye.configurations.CameraConfig;
import co.infinum.goldeneye.configurations.PreviewConfig;
import co.infinum.goldeneye.models.Facing;
import co.infinum.goldeneye.models.PreviewScale;

public class MainActivity extends AppCompatActivity {

    GoldenEye goldenEye;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goldenEye = new GoldenEye.Builder(this)
            .setLogger(new Logger() {
                @Override
                public void log(@NonNull String message) {
                    Log.e("Example", message);
                }

                @Override
                public void log(@NonNull Throwable throwable) {
                    throwable.printStackTrace();
                }
            })
            .build();
        goldenEye.initialize(Facing.BACK, new GoldenEye.InitializationCallback() {
            @Override
            public void onError(@NonNull Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onSuccess(@NonNull CameraConfig config) {
                Log.e("Example", "Initialized");
                goldenEye.startPreview(
                    (TextureView) findViewById(R.id.textureView),
                    new PreviewConfig.Builder()
                        .setPreviewScale(PreviewScale.FIT)
                        .build()
                );
            }
        });
    }
}
