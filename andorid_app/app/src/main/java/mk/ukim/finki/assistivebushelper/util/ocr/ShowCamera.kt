package mk.ukim.finki.assistivebushelper.util.ocr

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

@Suppress("DEPRECATION")
@SuppressLint("ViewConstructor")
class ShowCamera(context: Context, camera: Camera) : SurfaceView(context, null, 0),
    SurfaceHolder.Callback {

    private var contextCamera: Camera = camera
    private var timer: Timer? = null

    init {
        holder.addCallback(this)
    }

    private var timerTask: TimerTask = object : TimerTask() {
        override fun run() {
            contextCamera.startPreview()
            contextCamera.takePicture(null, null, { data, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    sendPictureUpdate(data)
                    contextCamera.startPreview()
                }
            })
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val params = contextCamera.parameters

        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            params.set("orientation", "portrait")
            contextCamera.setDisplayOrientation(90)
            params.setRotation(90)
        } else {
            params.set("orientation", "landscape")
            contextCamera.setDisplayOrientation(0)
            params.setRotation(0)
        }

        contextCamera.enableShutterSound(false)
        contextCamera.parameters = params
        try {
            contextCamera.setPreviewDisplay(holder)
            contextCamera.startPreview()

            takePictures()
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (holder.surface == null) {
            return
        }

        try {
            contextCamera.stopPreview()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        try {
            contextCamera.setPreviewDisplay(holder)
            contextCamera.startPreview()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        cancelTimer()
        contextCamera.stopPreview()
        contextCamera.release()
    }

    private fun takePictures() {
        timer = Timer()
        Timer().schedule(timerTask, 0, 5000)
    }

    private fun sendPictureUpdate(imageParts: ByteArray?) {
        val activityIntent = Intent("ocr_inference_update")
        activityIntent.putExtra("pictureToProcess", imageParts)

        LocalBroadcastManager.getInstance(context).sendBroadcast(activityIntent)
    }

    fun cancelTimer() {
        if (timerTask.cancel() && timer != null) {
            timer!!.purge()
            timer!!.cancel()
            timer = null
        }
    }
}