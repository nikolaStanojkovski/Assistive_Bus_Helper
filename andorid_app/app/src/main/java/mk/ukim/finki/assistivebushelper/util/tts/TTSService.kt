package mk.ukim.finki.assistivebushelper.util.tts

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import assistivebushelper.R
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mk.ukim.finki.assistivebushelper.util.NotificationUtils
import java.io.File


class TTSService : Service() {

    private lateinit var ttsReceiver: BroadcastReceiver
    private var notificationReference: NotificationCompat.Builder? = null

    override fun onBind(intent: Intent?): IBinder {
        receiveInferenceUpdate()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(ttsReceiver, IntentFilter("tts_inference_update"))
        startModel()

        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        receiveInferenceUpdate()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(ttsReceiver, IntentFilter("tts_inference_update"))
        startModel()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startModel() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val context = this

        CoroutineScope(Dispatchers.IO).launch {
            val py: Python = Python.getInstance()

            val synthesizeObject = py.getModule("synthesize_android")
            val model = synthesizeObject.callAttr("get_static_model")
            val vocoder = synthesizeObject.callAttr("get_static_vocoder")

            TTSUtils.synthesize_object = synthesizeObject
            TTSUtils.model = model
            TTSUtils.vocoder = vocoder

            withContext(Dispatchers.Main) {
                notificationReference = NotificationUtils.showNotification(context)
                sendProgressUpdate()
            }
        }
    }

    private fun inferenceModel(textValue: String) {
        if (notificationReference != null) {
            NotificationUtils.updateNotificationProgress(notificationReference!!, false)
        }

        TTSUtils.synthesize_object!!.callAttr(
            "main",
            textValue,
            TTSUtils.model,
            TTSUtils.vocoder
        )

        Toast.makeText(
            applicationContext,
            resources.getText(R.string.audio_generated_message),
            Toast.LENGTH_LONG
        ).show()

        if (notificationReference != null) {
            NotificationUtils.updateNotificationProgress(notificationReference!!, true)
        }
        playFile(textValue)
    }

    private fun playFile(textValue: String) {
        val path = getExternalFilesDir(null)
        val audioFile = File(path, "../${textValue}.wav")
        val spectrogramFile = File(path, "../${textValue}.png")

        if (audioFile.exists()) {
            MediaPlayer().apply {
                setDataSource(audioFile.path)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    audioFile.delete()
                    if (spectrogramFile.exists()) {
                        spectrogramFile.delete()
                    }
                    sendInferenceUpdate()
                }
            }
        }
    }

    private fun receiveInferenceUpdate() {
        ttsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.extras != null) {
                    val bundle: Bundle = intent.extras!!

                    val textToSpeak: String? = bundle.getString("textToSpeak")
                    if (!textToSpeak.isNullOrEmpty()) {
                        inferenceModel(textToSpeak)
                    }
                }
            }

        }
    }

    private fun sendProgressUpdate() {
        val activityIntent = Intent("tts_service_update")
        activityIntent.putExtra("closeProgressDialog", true)

        LocalBroadcastManager.getInstance(this).sendBroadcast(activityIntent)
    }

    private fun sendInferenceUpdate() {
        val activityIntent = Intent("tts_service_update")
        activityIntent.putExtra("inferenceFinished", true)

        LocalBroadcastManager.getInstance(this).sendBroadcast(activityIntent)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (notificationReference != null) {
            NotificationUtils.cancelNotification()
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(ttsReceiver)
    }
}