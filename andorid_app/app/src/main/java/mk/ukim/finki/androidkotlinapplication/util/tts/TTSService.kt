package mk.ukim.finki.androidkotlinapplication.util.tts

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import mk.ukim.finki.androidkotlinapplication.R
import mk.ukim.finki.androidkotlinapplication.util.NotificationUtils
import java.io.File

class TTSService : Service() {

    private var notificationReference: NotificationCompat.Builder? = null

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.extras != null) {
            val textToSpeak = (intent.extras as Bundle).get("textToSpeak") as String?
            if (!textToSpeak.isNullOrBlank()) {
                inferenceModel(textToSpeak)
            }
        }

        return super.onStartCommand(intent, flags, startId)
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
                        stopSelf()
                    }
                }
            }
        }
    }
}