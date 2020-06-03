package co.infinum.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.VideoCapture
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.min

@SuppressLint("SetTextI18n", "RestrictedApi")
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private var configs: CameraXConfig = CameraXConfig()
    private var videoCapture: VideoCapture? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var videoFile: File
    private var isRecording = false
    private var cameraLensSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var settingsAdapter = SettingsAdapter(listOf())
    private var needToRecreateCamera = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoFile = File.createTempFile("vid", "")
        initListeners()
        setUpZoomAndFocus()
    }

    private fun initListeners() {
        settingsView.setOnClickListener {
            prepareItems()
            settingsRecyclerView.apply {
                visibility = View.VISIBLE
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = settingsAdapter
            }
        }

        takePictureView.setOnClickListener { _ ->
            takePicture(
                onPictureTaken = { bitmap ->
                    if (bitmap.width <= 4096 && bitmap.height <= 4096) {
                        displayPicture(bitmap)
                    } else {
                        reducePictureSize(bitmap)
                    }
                }
            )
        }

        recordVideoView.setOnClickListener { _ ->
            if (isRecording) {
                isRecording = false
                recordVideoView.setImageResource(R.drawable.ic_record_video)
                videoCapture?.stopRecording()
            } else {
                startRecording()
            }
        }

        switchCameraView.setOnClickListener { _ ->
            cameraLensSelector =
                if (cameraLensSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
            recreateCamera()
        }
    }

    private fun recreateCamera() {
        CameraX.unbindAll()
        startCamera()
    }

    private fun takePicture(onPictureTaken: (Bitmap) -> Unit) {

        // Setup image capture listener which is triggered after photo has
        // been taken
        imageCapture?.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            @SuppressLint("UnsafeExperimentalUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                image.image?.let {
                    onPictureTaken(it.toBitmap())
                }
                super.onCaptureSuccess(image)
            }
        })
    }

    private fun reducePictureSize(bitmap: Bitmap) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val scaleX = 4096f / bitmap.width
                val scaleY = 4096f / bitmap.height
                val scale = min(scaleX, scaleY)
                val newBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
                mainHandler.post {
                    displayPicture(newBitmap)
                }
            } catch (t: Throwable) {
                toast("Picture is too big. Reduce picture size in settings below 4096x4096.")
            }
        }
    }

    private fun displayPicture(bitmap: Bitmap) {
        previewPictureView.apply {
            setImageBitmap(bitmap)
            visibility = View.VISIBLE
        }
        mainHandler.postDelayed(
            { previewPictureView.visibility = View.GONE },
            2000
        )
    }

    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onStop() {
        super.onStop()
        CameraX.unbindAll()
    }

    private fun openCamera() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val previewBuilder = Preview.Builder()
            val imageCaptureBuilder = ImageCapture.Builder()
                .setFlashMode(configs.flashMode)
            val videoCaptureBuilder = VideoCaptureConfig.Builder()
            if (configs.pictureSize != null) {
                configs.pictureSize?.let {
                    previewBuilder.setTargetResolution(it)
                    imageCaptureBuilder.setTargetResolution(it)
                    videoCaptureBuilder.setTargetResolution(it)
                }
            } else {
                previewBuilder.setTargetAspectRatio(configs.aspectRatio)
                imageCaptureBuilder.setTargetAspectRatio(configs.aspectRatio)
                videoCaptureBuilder.setTargetAspectRatio(configs.aspectRatio)
            }
            val preview = previewBuilder.build()
            imageCapture = imageCaptureBuilder.build()
            videoCapture = videoCaptureBuilder.build()

            // Select camera lens
            val cameraSelector = cameraLensSelector

            try {
                // Unbind use cases before rebindings
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture
                )

                preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera?.cameraInfo))
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            record()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0x2)
        }
    }

    private fun record() {
        isRecording = true
        recordVideoView.setImageResource(R.drawable.ic_stop)

        videoCapture?.startRecording(videoFile, ContextCompat.getMainExecutor(this), object : VideoCapture.OnVideoSavedCallback {
            override fun onVideoSaved(file: File) {
                previewVideoContainer.visibility = View.VISIBLE
                if (previewVideoView.isAvailable) {
                    startVideo()
                } else {
                    previewVideoView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?) = true

                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                            startVideo()
                        }
                    }
                }
            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                Log.i(javaClass.simpleName, "Video Error: $message")
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (settingsRecyclerView.visibility == View.VISIBLE) {
            settingsRecyclerView.visibility = View.GONE
            updateSettings()
        } else {
            super.onBackPressed()
        }
    }

    private fun updateSettings() {
        recreateCamera()
        setUpZoomAndFocus()
    }

    private fun prepareItems() {
        configs.prepareItems(this, settingsAdapter)
    }

    private fun startVideo() {
        MediaPlayer().apply {
            setSurface(Surface(previewVideoView.surfaceTexture))
            setDataSource(videoFile.absolutePath)
            setOnCompletionListener {
                mainHandler.postDelayed({
                    previewVideoContainer.visibility = View.GONE
                    release()
                }, 1500)
            }
            setOnVideoSizeChangedListener { _, width, height ->
                previewVideoView.apply {
                    layoutParams = layoutParams.apply {
                        val scaleX = previewVideoContainer.width / width.toFloat()
                        val scaleY = previewVideoContainer.height / height.toFloat()
                        val scale = min(scaleX, scaleY)

                        this.width = (width * scale).toInt()
                        this.height = (height * scale).toInt()
                    }
                }
            }
            prepare()
            start()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpPinchToZoom(event: MotionEvent, scaleGestureDetector: ScaleGestureDetector?): Boolean {
        scaleGestureDetector?.onTouchEvent(event)
        return true
    }

    private fun setAutomaticFocus() {
        viewFinder.afterMeasured {
            val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                viewFinder.width.toFloat(), viewFinder.height.toFloat()
            )
            val centerWidth = viewFinder.width.toFloat() / 2
            val centerHeight = viewFinder.height.toFloat() / 2
            //create a point on the center of the view
            val autoFocusPoint = factory.createPoint(centerWidth, centerHeight)
            try {
                camera?.cameraControl?.startFocusAndMetering(
                    FocusMeteringAction.Builder(
                        autoFocusPoint,
                        FocusMeteringAction.FLAG_AF
                    ).apply {
                        //auto-focus every 1 seconds
                        setAutoCancelDuration(1, TimeUnit.SECONDS)
                    }.build()
                )
            } catch (e: CameraInfoUnavailableException) {
                Log.d("ERROR", "cannot access camera", e)
            }
        }
    }

    private fun setUpTapToFocus(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                true
            }
            MotionEvent.ACTION_UP -> {
                val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    viewFinder.width.toFloat(), viewFinder.height.toFloat()
                )
                val autoFocusPoint = factory.createPoint(event.x, event.y)
                try {
                    camera?.cameraControl?.startFocusAndMetering(
                        FocusMeteringAction.Builder(
                            autoFocusPoint,
                            FocusMeteringAction.FLAG_AF
                        ).apply {
                            //focus only when the user tap the preview
                            disableAutoCancel()
                        }.build()
                    )
                } catch (e: CameraInfoUnavailableException) {
                    Log.d("ERROR", "cannot access camera", e)
                }
                true
            }
            else -> false // Unhandled event.
        }
    }

    fun setUpZoomAndFocus() {
        var scaleGestureDetector: ScaleGestureDetector? = null
        if (configs.pinchToZoomEnabled) {
            val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                    val delta = detector.scaleFactor
                    camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                    zoomView.text = "Zoom: %.02f".format(currentZoomRatio)
                    return true
                }
            }
            scaleGestureDetector = ScaleGestureDetector(this, listener)
        }
        viewFinder.afterMeasured {
            viewFinder.setOnTouchListener { _, event ->
                return@setOnTouchListener when {
                    configs.pinchToZoomEnabled && configs.focusMode == FOCUS_MODE_TAP -> setUpPinchToZoom(
                        event,
                        scaleGestureDetector
                    ) && setUpTapToFocus(event)
                    configs.pinchToZoomEnabled -> setUpPinchToZoom(event, scaleGestureDetector)
                    configs.focusMode == FOCUS_MODE_TAP -> setUpTapToFocus(event)
                    else -> false
                }
            }
        }
        if (configs.focusMode == FOCUS_MODE_AUTO) {
            setAutomaticFocus()
        } else if (configs.focusMode == FOCUS_MODE_OFF) {
            camera?.cameraControl?.cancelFocusAndMetering()
        }
    }
}
