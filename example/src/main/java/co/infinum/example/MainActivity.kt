package co.infinum.example

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import co.infinum.goldeneye.GoldenEye
import co.infinum.goldeneye.InitCallback
import co.infinum.goldeneye.Logger
import co.infinum.goldeneye.OnZoomChangeCallback
import co.infinum.goldeneye.models.PreviewScale
import co.infinum.goldeneye.models.Zoom
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    lateinit var goldenEye: GoldenEye
    lateinit var videoFile: File
    private var isRecording = false
    private var settingsAdapter = SettingsAdapter(listOf())

    private val initCallback = object : InitCallback {
        override fun onConfigReady() {
            zoomView.text = "Zoom: ${goldenEye.config.zoom.ratio.toPercentage()}"
        }

        override fun onError(t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goldenEye = GoldenEye.Builder(this)
            .setLogger(object : Logger {
                override fun log(message: String) {
                    Log.e("GoldenEye", message)
                }

                override fun log(t: Throwable) {
                    t.printStackTrace()
                }
            })
            .setOnZoomChangeCallback(object : OnZoomChangeCallback {
                override fun onZoomChanged(zoom: Zoom) {
                    zoomView.text = "Zoom: ${zoom.ratio.toPercentage()}"
                }
            })
            .build()
        goldenEye.open(textureView, goldenEye.availableCameras[0], initCallback)
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
                    previewPictureView.apply {
                        setImageBitmap(bitmap)
                        visibility = View.VISIBLE
                    }
                    mainHandler.postDelayed(
                        { previewPictureView.visibility = View.GONE },
                        3_000
                    )
                },
                onError = { it.printStackTrace() }
            )
        }

        recordVideoView.setOnClickListener { _ ->
            if (isRecording) {
                isRecording = false
                recordVideoView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_record_video))
                goldenEye.stopRecording()
            } else {
                isRecording = true
                recordVideoView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_stop))
                goldenEye.startRecording(
                    file = videoFile,
                    onVideoRecorded = {
                        previewVideoView.visibility = View.VISIBLE
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
        }

        switchCameraView.setOnClickListener { _ ->
            val currentIndex = goldenEye.availableCameras.indexOfFirst { goldenEye.config.id == it.id }
            val nextIndex = (currentIndex + 1) % goldenEye.availableCameras.size
            goldenEye.open(textureView, goldenEye.availableCameras[nextIndex], initCallback)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.e("GoldenEye", "Result received")
    }

    override fun onBackPressed() {
        if (settingsRecyclerView.visibility == View.VISIBLE) {
            settingsRecyclerView.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    private fun prepareItems() {
        with(goldenEye.config) {
            val settingsItems = listOf(
                SettingsItem("Preview size:", previewSize.convertToString()) {
                    displayDialog(
                        title = "Preview size",
                        listItems = supportedPreviewSizes.map { ListItem(it, it.convertToString()) },
                        onClick = { previewSize = it }
                    )
                },
                SettingsItem("Picture size:", pictureSize.convertToString()) {
                    displayDialog(
                        title = "Picture size",
                        listItems = supportedPictureSizes.map { ListItem(it, it.convertToString()) },
                        onClick = { pictureSize = it }
                    )
                },
                SettingsItem("Video size:", videoSize.convertToString()) {
                    displayDialog(
                        title = "Video size",
                        listItems = supportedVideoSizes.map { ListItem(it, it.convertToString()) },
                        onClick = { videoSize = it }
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
                    displayDialog(
                        title = "Video stabilization",
                        listItems = boolList(),
                        onClick = { videoStabilizationEnabled = it }
                    )
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
                SettingsItem("Zoom:", zoom.ratio.toPercentage()) {
                    displayDialog(
                        title = "Zoom",
                        listItems = supportedZooms.map { ListItem(it, it.ratio.toPercentage()) },
                        onClick = { zoom = it }
                    )
                },
                SettingsItem("White Balance:", whiteBalance.convertToString()) {
                    displayDialog(
                        title = "White Balance",
                        listItems = supportedWhiteBalance.map { ListItem(it, it.convertToString()) },
                        onClick = { whiteBalance = it }
                    )
                },
                SettingsItem("Scene Mode:", sceneMode.convertToString()) {
                    displayDialog(
                        title = "Scene Mode",
                        listItems = supportedSceneModes.map { ListItem(it, it.convertToString()) },
                        onClick = { sceneMode = it }
                    )
                },
                SettingsItem("Color Effect:", colorEffect.convertToString()) {
                    displayDialog(
                        title = "Color Effect",
                        listItems = supportedColorEffects.map { ListItem(it, it.convertToString()) },
                        onClick = { colorEffect = it }
                    )
                },
                SettingsItem("Exposure compensation:", exposureCompensation.toString()) {
                    displayDialog(
                        title = "Exposure compensation",
                        listItems = supportedExposureCompensation.map { ListItem(it, it.toString()) },
                        onClick = { exposureCompensation = it }
                    )
                },
                SettingsItem("Antibanding:", antibanding.convertToString()) {
                    displayDialog(
                        title = "Antibanding",
                        listItems = supportedAntibanding.map { ListItem(it, it.convertToString()) },
                        onClick = { antibanding = it }
                    )
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
                    previewVideoView.visibility = View.GONE
                    release()
                }, 3000)
            }
            prepare()
            start()
        }
    }

    private fun <T> displayDialog(title: String, listItems: List<ListItem<T>>, onClick: (T) -> Unit) {
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