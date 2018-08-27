package co.infinum.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;

import co.infinum.goldeneye.GoldenEye;
import co.infinum.goldeneye.GoldenEyeImpl;
import co.infinum.goldeneye.configurations.CameraConfig;
import co.infinum.goldeneye.models.Facing;

public class MainActivity extends AppCompatActivity {

    GoldenEye goldenEye;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goldenEye = new GoldenEyeImpl(this);
        goldenEye.init(Facing.BACK, new GoldenEye.InitializationCallback() {
            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onSuccess(CameraConfig configuration) {
                Log.e("Example", "Initialized");
                goldenEye.startPreview((TextureView) findViewById(R.id.textureView));
            }
        });
    }
}
