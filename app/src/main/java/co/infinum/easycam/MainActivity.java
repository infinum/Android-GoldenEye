package co.infinum.easycam;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import co.infinum.easycamera.AutoFitTextureView;
import co.infinum.easycamera.BitmapUtils;
import co.infinum.easycamera.CameraApi;
import co.infinum.easycamera.CameraApiCallbacks;
import co.infinum.easycamera.CameraApiManager;
import co.infinum.easycamera.CameraError;
import co.infinum.easycamera.Config;
import co.infinum.easycamera.SimpleSurfaceTextureListener;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements CameraApiCallbacks, EasyPermissions.PermissionCallbacks {

    private static final int RC_REQUEST_CAMERA_AND_WRITE_STORAGE = 100;

    @Bind(R.id.texture_view_camera)
    protected AutoFitTextureView textureViewCamera;

    @Bind(R.id.iv_image_taken_preview)
    protected ImageView ivImageTakenPreview;

    @Bind(R.id.v_bottom_bar)
    protected View vBottomBar;

    @Bind(R.id.iv_take_picture)
    protected ImageView ivTakePicture;

    @Bind(R.id.ll_camera_control_pre_take_image)
    protected LinearLayout llCameraControlPreTakeImage;

    @Bind(R.id.ll_camera_control_post_take_image)
    protected LinearLayout llCameraControlPostTakeImage;

    @Bind(R.id.fl_camera_control_panel)
    protected FrameLayout flCameraControlPanel;

    /**
     * CameraApi interface towards platform specific camera implementation.
     */
    private CameraApi cameraApi;

    /**
     * Holds currently active display metrics.
     */
    private DisplayMetrics displayMetrics = new DisplayMetrics();

    /**
     * Image received from successful call of <code>onImageTaken</code> callback method.
     */
    private File currentImageFile;

    /**
     * SurfaceTextureListener which will adapt UI when there are dimensional differences to TextureView.
     */
    private TextureView.SurfaceTextureListener surfaceTextureListener;

    private int minControlPanelHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // create a configuration for CameraApi
        Config config = new Config.Builder(this)
                .cameraFacing(CameraApi.CAMERA_FACING_BACK)
                .aspectRatio(1.333)
                .aspectRatioOffset(0.01) // gives 0.01 negative and positive offset to aspectRatio
                .build();
        // initialize camera interface and camera api
        this.cameraApi = CameraApiManager.newInstance(config).init(this);
        this.minControlPanelHeight = getResources().getDimensionPixelSize(R.dimen.camera_control_panel_height);

        this.surfaceTextureListener = new SimpleSurfaceTextureListener(this.cameraApi) {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                // setup the camera and open the preview
                initializeCameraView();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                super.onSurfaceTextureSizeChanged(surface, width, height);

                // adjust layout to newly prepared width and height of the texture
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                final int screenHeight = displayMetrics.heightPixels;
                final int surfaceHeight = textureViewCamera.getHeight();

                final int diff = screenHeight - surfaceHeight;
                final int newHeight = diff > minControlPanelHeight ? diff : minControlPanelHeight;

                // set bottom layout height (layout holding camera controls)
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) flCameraControlPanel.getLayoutParams();
                params.height = newHeight;
                flCameraControlPanel.setLayoutParams(params);
                flCameraControlPanel.requestLayout();

                // properly set bottom margin for bottom transparent bar
                params = (FrameLayout.LayoutParams) vBottomBar.getLayoutParams();
                params.bottomMargin = newHeight;
                vBottomBar.setLayoutParams(params);

                // mimic layout params for preview image view from texture view
                params = (FrameLayout.LayoutParams) ivImageTakenPreview.getLayoutParams();
                params.height = textureViewCamera.getHeight();
                params.width = textureViewCamera.getWidth();
                ivImageTakenPreview.setLayoutParams(params);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        // check if texture view is available
        // it will be available if it was already initialized, thus it does not need to be initialized again
        if (textureViewCamera.isAvailable()) {
            initializeCameraView();
        } else {
            // initialization is done asynchronously, once the listener is set
            textureViewCamera.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
    }

    @OnClick(R.id.iv_retake_picture)
    @Override
    public void onBackPressed() {
        if (this.currentImageFile != null) {
            this.currentImageFile.delete();
        }

        // if camera is closed, then it should be reopened
        if (!cameraApi.isCameraActive()) {
            initializeCameraView();
            displayCameraControls();
            // back button handled by turning camera back on
            return;
        }

        super.onBackPressed();
    }

    @OnClick(R.id.iv_take_picture)
    protected void onTakePictureClick() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, permissions)) {
            // noinspection ResourceType
            cameraApi.takePicture();
        } else {
            Timber.w("Sneaky user removed %s permissions after initial request has been granted.",
                    Arrays.toString(permissions));
        }
    }

    @OnClick(R.id.iv_switch_camera)
    protected void onSwitchCameraClick() {
        cameraApi.switchCameraFacing();
    }

    @OnClick(R.id.iv_close)
    protected void onCloseClick() {
        // close activity
        finish();
    }

    @OnClick(R.id.iv_accept_picture)
    protected void onAcceptPictureClick() {
        Toast.makeText(MainActivity.this, "Yay, image is great!", Toast.LENGTH_SHORT).show();
    }

    @OnTouch(R.id.texture_view_camera)
    protected boolean takeFocus(MotionEvent event) {
        // check for action UP to avoid calling acquireFocus multiple time during touch
        if (cameraApi.isCameraActive() && MotionEvent.ACTION_UP == event.getAction()) {
            cameraApi.acquireFocus((int) event.getX(), (int) event.getY());
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_REQUEST_CAMERA_AND_WRITE_STORAGE)
    public void initializeCameraView() {
        ivImageTakenPreview.setImageBitmap(null);
        ivImageTakenPreview.setVisibility(View.GONE);

        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {
//            // set surface texture to camera api so that it can draw a stream from camera hardware to it
//            cameraApi.setSurfaceTexture(textureViewCamera.getSurfaceTexture());
//            // noinspection ResourceType
//            cameraApi.openCamera(textureViewCamera.getWidth(), textureViewCamera.getHeight());

            // or for convenience
            // noinspection ResourceType
            cameraApi.openCamera(textureViewCamera);
        } else {
            // both permissions have not been granted, ask user for these permissions
            EasyPermissions.requestPermissions(this, getString(R.string.request_access_to_camera_and_storage),
                    RC_REQUEST_CAMERA_AND_WRITE_STORAGE, permissions);
        }
    }

    @Override
    public void onResolvedPreviewSize(int width, int height) {
        // camera preview size has been resolved, best practice is to
        // set the correct aspect ratio for preview
        // do it manually for your implementation of surface view
        textureViewCamera.setAspectRatio(width, height);
    }

    @Override
    public void onTransformChanged(Matrix matrix) {
        // Preview transform which needs to be applied to the underlying surface view
        textureViewCamera.setTransform(matrix);
    }

    @Override
    public void onCameraError(CameraError cameraError) {
        // either finish or show an error

        new AlertDialog.Builder(this)
                .setTitle("Camera Error")
                .setMessage(cameraError.getError())
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onImageTaken(@NonNull File imageFile) {
        // store image for further manipulation
        this.currentImageFile = imageFile;
        // close camera, as preview will be shown
        closeCamera();
        // change UI, add image controls
        displayPostImageTakenCameraControls();

        ivImageTakenPreview.setVisibility(View.VISIBLE);

        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException e) {
            Timber.w(e, "Could not get Exif information from image %s", imageFile.getAbsolutePath());
        }

        ivImageTakenPreview.setImageBitmap(BitmapUtils.rotateBitmap(BitmapUtils.decodeSampledBitmapFromFile(imageFile,
                ivImageTakenPreview.getWidth(),
                ivImageTakenPreview.getHeight()), orientation));
    }

    private void closeCamera() {
        // check if camera is active and close it if it is
        if (cameraApi.isCameraActive()) {
            cameraApi.closeCamera();
        }
    }

    private void displayPostImageTakenCameraControls() {
        llCameraControlPostTakeImage.setVisibility(View.VISIBLE);
        llCameraControlPreTakeImage.setVisibility(View.GONE);
    }

    private void displayCameraControls() {
        llCameraControlPostTakeImage.setVisibility(View.GONE);
        llCameraControlPreTakeImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        // no op
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // we don't have all permissions, no use in being here, finish
        finish();
    }
}
