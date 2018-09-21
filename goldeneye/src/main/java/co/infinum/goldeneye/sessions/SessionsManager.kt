package co.infinum.goldeneye.sessions

import android.graphics.Bitmap
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.PictureCallback
import co.infinum.goldeneye.VideoCallback
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class SessionsManager(
    private val textureView: TextureView,
    private val pictureSession: PictureSession
) {

//    private var activeSession: BaseSession = pictureSession

    fun updateRequests(update: CaptureRequest.Builder.() -> Unit) {
        pictureSession.updateRequest(update)

        try {
            //TODO expose way to tell whether its restart or recreate
            pictureSession.startSession()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    fun lockFocus(region: Array<MeteringRectangle>) {
        pictureSession.updateRequest {
            set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
        }
        pictureSession.singleCapture()

        updateRequests {
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
            set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            set(CaptureRequest.CONTROL_AF_REGIONS, region)
        }
    }

    fun unlockFocus(focus: FocusMode) {
        updateRequests {
            set(CaptureRequest.CONTROL_AF_MODE, focus.toCamera2())
            set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
            set(CaptureRequest.CONTROL_AF_REGIONS, null)
        }
    }

    fun startPreview() {
        pictureSession.createSession(textureView)
    }

    fun takePicture(callback: PictureCallback) {
        pictureSession.takePicture(callback)
    }

    fun startRecording(file: File, callback: VideoCallback) {
        //TODO
    }

    fun stopRecording() {
        //TODO
    }

    fun release() {

    }
}