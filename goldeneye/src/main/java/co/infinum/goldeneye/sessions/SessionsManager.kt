package co.infinum.goldeneye.sessions

import android.graphics.Rect
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import android.os.Build
import androidx.annotation.RequiresApi
import android.view.TextureView
import co.infinum.goldeneye.InitCallback
import co.infinum.goldeneye.PictureCallback
import co.infinum.goldeneye.VideoCallback
import co.infinum.goldeneye.models.FocusMode
import co.infinum.goldeneye.utils.LogDelegate
import java.io.File

/**
 * Picture and Video session wrapper. Delegates calls to picture
 * or video session depending on current camera state.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class SessionsManager(
    val textureView: TextureView,
    private val pictureSession: PictureSession,
    private val videoSession: VideoSession
) {

    private var activeSession: BaseSession = pictureSession

    /**
     * Update both session parameters and apply them only to currently
     * active session.
     */
    fun updateSession(update: CaptureRequest.Builder.() -> Unit) {
        pictureSession.updateRequest(update)
        videoSession.updateRequest(update)

        try {
            activeSession.startSession()
        } catch (t: Throwable) {
            LogDelegate.log("Failed to apply new parameters to camera.", t)
        }
    }

    /**
     * Restart session only if session is [PictureSession].
     *
     * This can happen if preview or picture size is updated.
     * If Video session is active, we want to completely ignore
     * that update and apply it only after recording is finished.
     */
    fun restartSession() {
        try {
            if (activeSession is PictureSession) {
                activeSession.createSession(textureView)
            }
        } catch (t: Throwable) {
            LogDelegate.log("Failed to restart session.", t)
        }
    }

    fun resetFlashMode() {
        activeSession.resetFlash()
    }

    /**
     * Used to lock focus for tap to focus functionality.
     */
    fun lockFocus(region: Rect) {
        activeSession.lockFocus(region)
    }

    /**
     * Used to unlock focus after tap to focus is finished.
     */
    fun unlockFocus(focus: FocusMode) {
        activeSession.unlockFocus(focus)
    }

    fun startPreview(callback: InitCallback) {
        activeSession = pictureSession
        pictureSession.createInitialPreviewSession(textureView, callback)
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
        pictureSession.release()
        videoSession.release()
    }
}