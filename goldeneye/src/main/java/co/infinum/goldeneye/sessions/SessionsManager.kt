package co.infinum.goldeneye.sessions

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
    val textureView: TextureView,
    private val pictureSession: PictureSession,
    private val videoSession: VideoSession
) {

    private var activeSession: BaseSession = pictureSession

    fun updateSession(update: CaptureRequest.Builder.() -> Unit) {
        pictureSession.updateRequest(update)
        videoSession.updateRequest(update)

        try {
            activeSession.startSession()
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    fun restartSession() {
        try {
            if (activeSession is PictureSession) {
                activeSession.createSession(textureView)
            }
        } catch (t: Throwable) {
            LogDelegate.log(t)
        }
    }

    fun lockFocus(region: Array<MeteringRectangle>) {
        activeSession.cancelFocus()

        updateSession {
            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
            set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            set(CaptureRequest.CONTROL_AF_REGIONS, region)
        }
    }

    fun unlockFocus(focus: FocusMode) {
        updateSession {
            set(CaptureRequest.CONTROL_AF_MODE, focus.toCamera2())
            set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
            set(CaptureRequest.CONTROL_AF_REGIONS, null)
        }
    }

    fun startPreview() {
        activeSession = pictureSession
        pictureSession.createSession(textureView)
    }

    fun takePicture(callback: PictureCallback) {
        pictureSession.takePicture(callback)
    }

    fun startRecording(file: File, callback: VideoCallback) {
        pictureSession.release()
        activeSession = videoSession
        videoSession.startRecording(textureView, file, callback)
    }

    fun stopRecording() {
        videoSession.stopRecording()
        videoSession.release()
        activeSession = pictureSession
        pictureSession.createSession(textureView)
    }

    fun release() {
        activeSession.release()
    }
}