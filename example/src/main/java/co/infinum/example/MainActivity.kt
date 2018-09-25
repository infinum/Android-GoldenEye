package co.infinum.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import co.infinum.goldeneye.GoldenEye
import co.infinum.goldeneye.InitCallback
import co.infinum.goldeneye.Logger
import co.infinum.goldeneye.config.CameraInfo
import co.infinum.goldeneye.models.PreviewScale
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.math.max

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    lateinit var goldenEye: GoldenEye
    private lateinit var videoFile: File
    private var isRecording = false
    private var settingsAdapter = SettingsAdapter(listOf())

    private val initCallback = object : InitCallback {
        override fun onConfigReady() {
            zoomView.text = "Zoom: ${goldenEye.config.zoom.toPercentage()}"
        }

        override fun onError(t: Throwable) {
            toast("Init error - check log")
            t.printStackTrace()
        }
    }

    private val logger = object : Logger {
        override fun log(message: String) {
            Log.e("GoldenEye", message)
        }

        override fun log(t: Throwable) {
            toast("GoldenEye error - check log")
            t.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        goldenEye = GoldenEye.Builder(this)
            .setLogger(logger)
            .setOnZoomChangedCallback { zoomView.text = "Zoom: ${it.toPercentage()}" }
            .build()
        videoFile = File.createTempFile("vid", "")

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
                        previewPictureView.apply {
                            setImageBitmap(bitmap)
                            visibility = View.VISIBLE
                        }
                        mainHandler.postDelayed(
                            { previewPictureView.visibility = View.GONE },
                            3_000
                        )
                    } else {
                        toast("Bitmap too large to show in ImageView. Max is 4096x4096.")
                    }
                },
                onError = {
                    toast("TakePicture error - check log")
                    it.printStackTrace()
                }
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
            val currentIndex = goldenEye.availableCameras.indexOfFirst { goldenEye.config.id == it.id }
            val nextIndex = (currentIndex + 1) % goldenEye.availableCameras.size
            openCamera(goldenEye.availableCameras[nextIndex])
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
            onError = {
                toast("Recording error - check log")
                it.printStackTrace()
            }
        )
    }

    private fun openCamera(cameraInfo: CameraInfo) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            goldenEye.open(textureView, cameraInfo, initCallback)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0x1)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0x1 && grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED) {
            goldenEye.open(textureView, goldenEye.availableCameras[0], initCallback)
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

    override fun onResume() {
        super.onResume()
        if (goldenEye.availableCameras.isNotEmpty()) {
            openCamera(goldenEye.availableCameras[0])
        }
    }

    override fun onPause() {
        super.onPause()
        goldenEye.release()
    }

    private fun prepareItems() {
        with(goldenEye.config) {
            val settingsItems = listOf(
                SettingsItem("Preview size:", previewSize.convertToString()) {
                    if (previewScale == PreviewScale.MANUAL
                        || previewScale == PreviewScale.MANUAL_FIT
                        || previewScale == PreviewScale.MANUAL_FILL
                    ) {
                        displayDialog(
                            title = "Preview size",
                            listItems = supportedPreviewSizes.map { ListItem(it, it.convertToString()) },
                            onClick = { previewSize = it }
                        )
                    } else {
                        toast("Preview scale is automatic so it picks preview size on its own.")
                    }
                },
                SettingsItem("Picture size:", pictureSize.convertToString()) {
                    displayDialog(
                        title = "Picture size",
                        listItems = supportedPictureSizes.map { ListItem(it, it.convertToString()) },
                        onClick = { pictureSize = it }
                    )
                },
                SettingsItem("Preview scale", previewScale.convertToString()) {
                    displayDialog(
                        title = "Preview scale",
                        listItems = PreviewScale.values().map { ListItem(it, it.convertToString()) },
                        onClick = { previewScale = it }
                    )
                },
                SettingsItem("Video quality:", videoQuality.convertToString()) {
                    displayDialog(
                        title = "Video quality",
                        listItems = supportedVideoQualities.map { ListItem(it, it.convertToString()) },
                        onClick = { videoQuality = it }
                    )
                },
                SettingsItem("Video stabilization:", videoStabilizationEnabled.convertToString()) {
                    if (isVideoStabilizationSupported) {
                        displayDialog(
                            title = "Video stabilization",
                            listItems = boolList(),
                            onClick = { videoStabilizationEnabled = it }
                        )
                    } else {
                        toast("Video stabilization not supported")
                    }
                },
                SettingsItem("Flash mode:", flashMode.convertToString()) {
                    displayDialog(
                        title = "Flash mode",
                        listItems = supportedFlashModes.map { ListItem(it, it.convertToString()) },
                        onClick = { flashMode = it }
                    )
                },
                SettingsItem("Focus mode:", focusMode.convertToString()) {
                    displayDialog(
                        title = "Focus mode",
                        listItems = supportedFocusModes.map { ListItem(it, it.convertToString()) },
                        onClick = { focusMode = it }
                    )
                },
                SettingsItem("White Balance:", whiteBalanceMode.convertToString()) {
                    displayDialog(
                        title = "White Balance",
                        listItems = supportedWhiteBalanceModes.map { ListItem(it, it.convertToString()) },
                        onClick = { whiteBalanceMode = it }
                    )
                },
                SettingsItem("Color Effect:", colorEffectMode.convertToString()) {
                    displayDialog(
                        title = "Color Effect",
                        listItems = supportedColorEffectModes.map { ListItem(it, it.convertToString()) },
                        onClick = { colorEffectMode = it }
                    )
                },
                SettingsItem("Antibanding:", antibandingMode.convertToString()) {
                    displayDialog(
                        title = "Antibanding",
                        listItems = supportedAntibandingModes.map { ListItem(it, it.convertToString()) },
                        onClick = { antibandingMode = it }
                    )
                },
                SettingsItem("Tap to focus:", tapToFocusEnabled.convertToString()) {
                    if (isTapToFocusSupported) {
                        displayDialog(
                            title = "Tap to focus",
                            listItems = boolList(),
                            onClick = { tapToFocusEnabled = it }
                        )
                    } else {
                        toast("Tap to focus not supported.")
                    }
                },
                SettingsItem("Tap to focus - reset focus delay:", resetFocusDelay.toString()) {
                    if (isTapToFocusSupported) {
                        displayDialog(
                            title = "Reset delay",
                            listItems = listOf(
                                ListItem(2_500L, "2500"),
                                ListItem(5_000L, "5000"),
                                ListItem(7_500L, "7500")
                            ),
                            onClick = { resetFocusDelay = it }
                        )
                    } else {
                        toast("Tap to focus not supported.")
                    }
                },
                SettingsItem("Pinch to zoom:", pinchToZoomEnabled.convertToString()) {
                    if (isZoomSupported) {
                        displayDialog(
                            title = "Pinch to zoom",
                            listItems = boolList(),
                            onClick = { pinchToZoomEnabled = it }
                        )
                    } else {
                        toast("Pinch to zoom not supported.")
                    }
                },
                SettingsItem("Pinch to zoom friction:", "%.02f".format(pinchToZoomFriction)) {
                    if (isZoomSupported) {
                        displayDialog(
                            title = "Friction",
                            listItems = listOf(
                                ListItem(0.5f, "0.50"),
                                ListItem(1f, "1.00"),
                                ListItem(2f, "2.00")
                            ),
                            onClick = { pinchToZoomFriction = it }
                        )
                    } else {
                        toast("Pinch to zoom not supported.")
                    }
                }
            )
            settingsAdapter.updateDataSet(settingsItems)
        }
    }

    private fun startVideo() {
        MediaPlayer().apply {
            setSurface(Surface(previewVideoView.surfaceTexture))
            setDataSource(videoFile.absolutePath)
            setOnCompletionListener {
                mainHandler.postDelayed({
                    previewVideoContainer.visibility = View.GONE
                    release()
                }, 3000)
            }
            setOnVideoSizeChangedListener { _, width, height ->
                previewVideoView.apply {
                    layoutParams = layoutParams.apply {
                        val scaleX = previewVideoContainer.width / width.toFloat()
                        val scaleY = previewVideoContainer.height / height.toFloat()
                        val scale = max(scaleX, scaleY)

                        this.width = (width * scale).toInt()
                        this.height = (height * scale).toInt()
                    }
                }
            }
            prepare()
            start()
        }
    }

    private fun <T> displayDialog(title: String, listItems: List<ListItem<T>>, onClick: (T) -> Unit) {
        if (listItems.isEmpty()) {
            toast("$title not supported")
            return
        }

        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.list)
            .setCancelable(true)
            .setTitle(title)
            .show()

        dialog.findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ListItemAdapter(listItems) {
                onClick(it)
                dialog.dismiss()
                prepareItems()
            }
        }
    }
}