package co.infinum.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import co.infinum.goldeneye.Facing
import co.infinum.goldeneye.GoldenEye
import co.infinum.goldeneye.GoldenEyeImpl
import co.infinum.goldeneye.InitCallback

class MainActivity : AppCompatActivity() {

    lateinit var goldenEye: GoldenEye
    private val initCallback = object: InitCallback {
        override fun onSuccess() {
            goldenEye.start(findViewById(R.id.textureView))
        }

        override fun onError(t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        goldenEye = GoldenEyeImpl(this)
        goldenEye.init(goldenEye.availableCameras[0], initCallback)
        initViews()
    }

    private fun initViews() {
        val focusModeView = findViewById<TextView>(R.id.focusModeView)
        val flashModeView = findViewById<TextView>(R.id.flashModeView)
        val previewTypeView = findViewById<TextView>(R.id.previewTypeView)
        val previewScaleView = findViewById<TextView>(R.id.previewScaleView)
        val videoSizeView = findViewById<TextView>(R.id.videoSizeView)
        val pictureSizeView = findViewById<TextView>(R.id.pictureSizeView)
        val frontFacingView = findViewById<TextView>(R.id.frontFacingView)
        val backFacingView = findViewById<TextView>(R.id.backFacingView)
        val nextFacingView = findViewById<TextView>(R.id.nextFacingView)
        val settingsContainer = findViewById<View>(R.id.settingsContainer)
        val settingsToggleButton = findViewById<View>(R.id.settingsToggleButton)

        with(goldenEye.currentConfig) {
            focusModeView.text = "Focus:\n${focusMode.convertToString()}"
            flashModeView.text = "Flash:\n${flashMode.convertToString()}"
            previewTypeView.text = "Type:\n${previewType.convertToString()}"
            previewScaleView.text = "Scale:\n${previewScale.convertToString()}"
            videoSizeView.text = "Video:\n${videoSize.convertToString()}"
            pictureSizeView.text = "Picture:\n${pictureSize.convertToString()}"
            frontFacingView.text = "Front\ncamera"
            backFacingView.text = "Back\ncamera"
            nextFacingView.text = "Change\ncamera"
            settingsContainer.visibility = View.GONE
        }

        settingsToggleButton.setOnClickListener {
            if (settingsContainer.visibility == View.GONE) {
                settingsContainer.visibility = View.VISIBLE
            } else {
                settingsContainer.visibility = View.GONE
            }
        }

        frontFacingView.setOnClickListener { _ ->
            val frontCamera = goldenEye.availableCameras.find { it.facing == Facing.FRONT }
            frontCamera?.let { goldenEye.init(it, initCallback) }
        }
        
        backFacingView.setOnClickListener { _ ->
            val backCamera = goldenEye.availableCameras.find { it.facing == Facing.BACK }
            backCamera?.let { goldenEye.init(it, initCallback) }
        }
        nextFacingView.setOnClickListener {
            val nextIndex = (goldenEye.availableCameras.indexOf(goldenEye.currentCamera) + 1) % goldenEye.availableCameras.size
            goldenEye.init(goldenEye.availableCameras[nextIndex], initCallback)
        }
    }
}