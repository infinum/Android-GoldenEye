package co.infinum.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import co.infinum.goldeneye.GoldenEye
import co.infinum.goldeneye.InitCallback
import co.infinum.goldeneye.Logger
import co.infinum.goldeneye.config.CameraConfig
import co.infinum.goldeneye.config.CameraInfo
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.Executors
import kotlin.math.min

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var goldenEye: GoldenEye
    private lateinit var videoFile: File
    private var isRecording = false
    private var settingsAdapter = SettingsAdapter(listOf())

    private val initCallback = object : InitCallback() {
        override fun onReady(config: CameraConfig) {
            zoomView.text = "Zoom: ${config.zoom.toPercentage()}"
        }

        override fun onError(t: Throwable) {
            t.printStackTrace()
        }
    }

    private val logger = object : Logger {
        override fun log(message: String) {
            Log.e("GoldenEye", message)
        }

        override fun log(t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initGoldenEye()
        videoFile = File.createTempFile("vid", ".mp4",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES))
        initListeners()
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
            goldenEye.takePicture(
                onPictureTaken = { bitmap ->
                    if (bitmap.width <= 4096 && bitmap.height <= 4096) {
                        saveImage(bitmap, "MyExperience" + java.util.Calendar.getInstance(), this)
                        displayPicture(bitmap)
                    } else {
                        reducePictureSize(bitmap)
                    }
                },
                onError = { it.printStackTrace() }
            )
        }

        recordVideoView.setOnClickListener { _ ->
            if (isRecording) {
                isRecording = false
                recordVideoView.setImageResource(R.drawable.ic_record_video)
                goldenEye.stopRecording()
            } else {
                startRecording()
            }
        }

        switchCameraView.setOnClickListener { _ ->
            val currentIndex = goldenEye.availableCameras.indexOfFirst { goldenEye.config?.id == it.id }
            val nextIndex = (currentIndex + 1) % goldenEye.availableCameras.size
            openCamera(goldenEye.availableCameras[nextIndex])
        }
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

    private fun initGoldenEye() {
        goldenEye = GoldenEye.Builder(this)
            .setLogger(logger)
            .withAdvancedFeatures()
            .setOnZoomChangedCallback { zoomView.text = "Zoom: ${it.toPercentage()}" }
            .build()
    }

    override fun onStart() {
        super.onStart()
        if (goldenEye.availableCameras.isNotEmpty()) {
            openCamera(goldenEye.availableCameras[0])
        }
    }

    override fun onStop() {
        super.onStop()
        goldenEye.release()
    }

    private fun openCamera(cameraInfo: CameraInfo) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            goldenEye.open(textureView, cameraInfo, initCallback)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0x1)
        }
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
        goldenEye.startRecording(
            file = videoFile,
            onVideoRecorded = {
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
            },
            onError = { it.printStackTrace() }
        )
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0x1) {
            if (grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
                goldenEye.open(textureView, goldenEye.availableCameras[0], initCallback)
            } else {
                AlertDialog.Builder(this)
                    .setTitle("GoldenEye")
                    .setMessage("Permission needed to access app features")
                    .setPositiveButton("Deny") { _, _ ->
                        throw NoPermissionException
                    }
                    .setNegativeButton("Allow") { _, _ ->
                        openCamera(goldenEye.availableCameras[0])
                    }
                    .setCancelable(false)
                    .show()
            }
        } else if (requestCode == 0x2) {
            record()
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
        goldenEye.config?.apply {
            prepareItems(this@MainActivity, settingsAdapter)
        }
    }

    private fun startVideo() {
        MediaPlayer().apply {
            setSurface(Surface(previewVideoView.surfaceTexture))
            setDataSource(videoFile.absolutePath)
            Toast.makeText(baseContext, videoFile.absolutePath, Toast.LENGTH_LONG).show()
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
}

// Method to save an image to gallery and return uri
private fun saveImage(bitmap:Bitmap, title:String, context:Context):Uri{

    // Save image to gallery
    val savedImageURL = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            title,
            "Image of $title"
    )

    // Parse the gallery image url to uri
    return Uri.parse(savedImageURL)
}



object NoPermissionException : Throwable()
