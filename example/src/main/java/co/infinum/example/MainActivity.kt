package co.infinum.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.VideoCapture
import androidx.camera.core.impl.VideoCaptureConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.main_activity2.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min

private const val REQUEST_CODE_PERMISSIONS = 14

@SuppressLint("SetTextI18n", "RestrictedApi")
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private var preview: Preview? = null
    private var videoCapture: VideoCapture? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var videoFile: File
    private var isRecording = false
    private var lensFacing = CameraSelector.LENS_FACING_BACK
//    private var settingsAdapter = SettingsAdapter(listOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity2)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        videoFile = File.createTempFile("vid", "")

        initListeners()
        setUpPinchToZoom()
    }

    private fun initListeners() {
//        settingsView.setOnClickListener {
//            prepareItems()
//            settingsRecyclerView.apply {
//                visibility = View.VISIBLE
//                layoutManager = LinearLayoutManager(this@MainActivity2)
//                adapter = settingsAdapter
//            }
//        }

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
            lensFacing =
                if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            CameraX.getCameraWithLensFacing(lensFacing)
            recreateCamera()
        }
    }

    private fun recreateCamera() {
        CameraX.unbindAll()
        startCamera()
    }

    private fun takePicture(onPictureTaken: (Bitmap) -> Unit) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create timestamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Setup image capture listener which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    val image = MediaStore.Images.Media.getBitmap(contentResolver, savedUri)
                    onPictureTaken(image)
                    Log.d(TAG, msg)
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0x1)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
            imageCapture = ImageCapture.Builder()
                .build()
            videoCapture = VideoCaptureConfig.Builder().build()

            // Select camera lens
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture
                )
                preview?.setSurfaceProvider(viewFinder.createSurfaceProvider(camera?.cameraInfo))
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

        val videoCapture = videoCapture ?: return
        videoCapture.startRecording(videoFile, ContextCompat.getMainExecutor(this), object : VideoCapture.OnVideoSavedCallback {
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
        } else {
            super.onBackPressed()
        }
    }

    private fun prepareItems() {
//        camera?.cameraControl.
//            prepareItems(this@MainActivity, settingsAdapter)
//        }
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

    fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun setUpPinchToZoom() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                zoomView.text = "Zoom: %.02f".format(currentZoomRatio)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this, listener)

        viewFinder.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }
}
