package co.infinum.example

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import co.infinum.goldeneye.*
import co.infinum.goldeneye.extensions.reverseCameraRotation
import co.infinum.goldeneye.InitCallback
import co.infinum.goldeneye.PictureCallback
import co.infinum.goldeneye.models.PreviewScale
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    lateinit var goldenEye: GoldenEye
    private lateinit var focusModeView: TextView
    private lateinit var flashModeView: TextView
    private lateinit var previewScaleView: TextView
    private lateinit var videoSizeView: TextView
    private lateinit var pictureSizeView: TextView
    private lateinit var previewSizeView: TextView
    private lateinit var nextCameraView: TextView
    private lateinit var settingsContainer: View
    private lateinit var settingsToggleButton: View
    private lateinit var takePictureView: TextView
    private lateinit var previewPictureView: ImageView
    private lateinit var tapToFocusView: TextView

    private val initCallback = object : InitCallback {
        override fun onSuccess() {
            goldenEye.startPreview(findViewById(R.id.textureView))
            updateViews()
        }

        override fun onError(t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        goldenEye = GoldenEye.Builder(this).build()
        goldenEye.init(goldenEye.availableCameras[0], initCallback)
    }

    private fun initViews() {

        focusModeView = findViewById(R.id.focusModeView)
        flashModeView = findViewById(R.id.flashModeView)
        previewScaleView = findViewById(R.id.previewScaleView)
        videoSizeView = findViewById(R.id.videoSizeView)
        pictureSizeView = findViewById(R.id.pictureSizeView)
        previewSizeView = findViewById(R.id.previewSizeView)
        nextCameraView = findViewById(R.id.nextCameraView)
        settingsContainer = findViewById(R.id.settingsContainer)
        settingsToggleButton = findViewById(R.id.settingsToggleButton)
        takePictureView = findViewById(R.id.takePictureView)
        previewPictureView = findViewById(R.id.previewPictureView)
        tapToFocusView = findViewById(R.id.tapToFocusView)
        settingsToggleButton.setOnClickListener {
            if (settingsContainer.visibility == View.GONE) {
                settingsContainer.visibility = View.VISIBLE
            } else {
                settingsContainer.visibility = View.GONE
            }
        }
        settingsContainer.visibility = View.GONE

        nextCameraView.setOnClickListener { _ ->
            val nextIndex = (goldenEye.availableCameras.indexOfFirst { it.id == goldenEye.currentConfig.id } + 1) % goldenEye.availableCameras.size
            goldenEye.init(goldenEye.availableCameras[nextIndex], initCallback)
        }

        focusModeView.setOnClickListener { _ ->
            displayDialog(
                title = "Focus",
                listItems = goldenEye.currentConfig.supportedFocusModes.map { focusMode ->
                    ListItem(focusMode) { it.convertToString() }
                },
                onClick = { goldenEye.currentConfig.focusMode = it }
            )
        }

        flashModeView.setOnClickListener { _ ->
            displayDialog(
                title = "Flash",
                listItems = goldenEye.currentConfig.supportedFlashModes.map { flashMode ->
                    ListItem(flashMode) { it.convertToString() }
                },
                onClick = { goldenEye.currentConfig.flashMode = it }
            )
        }

        previewScaleView.setOnClickListener { _ ->
            displayDialog(
                title = "Scale",
                listItems = PreviewScale.values().map { scale ->
                    ListItem(scale) { it.convertToString() }
                },
                onClick = { goldenEye.currentConfig.previewScale = it }
            )
        }

        previewSizeView.setOnClickListener { _ ->
            displayDialog(
                title = "Preview size",
                listItems = goldenEye.currentConfig.supportedPreviewSizes.map { size ->
                    ListItem(size) { it.convertToString() }
                },
                onClick = { goldenEye.currentConfig.previewSize = it }
            )
        }

        videoSizeView.setOnClickListener { _ ->
            displayDialog(
                title = "Video size",
                listItems = goldenEye.currentConfig.supportedVideoSizes.map { size ->
                    ListItem(size) { it.convertToString() }
                },
                onClick = { goldenEye.currentConfig.videoSize = it }
            )
        }

        pictureSizeView.setOnClickListener { _ ->
            displayDialog(
                title = "Picture size",
                listItems = goldenEye.currentConfig.supportedPictureSizes.map { size ->
                    ListItem(size) { it.convertToString() }
                },
                onClick = { goldenEye.currentConfig.pictureSize = it }
            )
        }

        takePictureView.setOnClickListener { _ ->
            goldenEye.takePicture(object : PictureCallback() {
                override fun onPictureTaken(picture: Bitmap) {
                    previewPictureView.visibility = View.VISIBLE
                    executeOnBackground(
                        task = { picture.reverseCameraRotation(this@MainActivity, goldenEye.currentConfig) },
                        onSuccess = {
                            previewPictureView.setImageBitmap(it)
                            mainHandler.postDelayed({ previewPictureView.visibility = View.GONE }, 3000)
                        },
                        onError = { it.printStackTrace() }
                    )
                }

                override fun onError(t: Throwable) {
                    t.printStackTrace()
                }
            })
        }

        tapToFocusView.setOnClickListener {
            goldenEye.currentConfig.isTapToFocusEnabled = goldenEye.currentConfig.isTapToFocusEnabled.not()
            updateViews()
        }
    }

    private fun <T> displayDialog(title: String, listItems: List<ListItem<T>>, onClick: (T) -> Unit) {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.list)
            .setCancelable(true)
            .setTitle(title)
            .show()

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = ListItemAdapter(listItems) {
            onClick(it)
            updateViews()
            dialog.dismiss()
        }
    }

    private fun updateViews() {
        with(goldenEye.currentConfig) {
            focusModeView.text = "Focus:\n${focusMode.convertToString()}"
            flashModeView.text = "Flash:\n${flashMode.convertToString()}"
            previewScaleView.text = "Scale:\n${previewScale.convertToString()}"
            videoSizeView.text = "Video:\n${videoSize.convertToString()}"
            pictureSizeView.text = "Picture:\n${pictureSize.convertToString()}"
            previewSizeView.text = "Preview:\n${previewSize.convertToString()}"
            nextCameraView.text = "Cycle camera"
            takePictureView.text = "Take picture"
            tapToFocusView.text = "Tap to Focus:\n$isTapToFocusEnabled"
        }
    }

    private fun <T> executeOnBackground(task: () -> T, onSuccess: (T) -> Unit, onError: ((Throwable) -> Unit)? = null) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val result = task()
                mainHandler.post { onSuccess(result) }
            } catch (t: Throwable) {
                mainHandler.post { onError?.invoke(t) }
            }
        }
    }
}