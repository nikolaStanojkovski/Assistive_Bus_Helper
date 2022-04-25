package mk.ukim.finki.androidkotlinapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mk.ukim.finki.androidkotlinapplication.ui.ProgressDialog
import mk.ukim.finki.androidkotlinapplication.util.NotificationUtils
import mk.ukim.finki.androidkotlinapplication.util.tts.TTSService
import mk.ukim.finki.androidkotlinapplication.util.tts.TTSUtils


class MainActivity : AppCompatActivity() {

    private var notificationReference: NotificationCompat.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startTTSModel()
        findViewById<Button>(R.id.btnSpeak).setOnClickListener {
            startTTSService(findViewById<TextView>(R.id.inputText).text.toString())
        }
    }

    private fun startTTSModel() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val context = this
        val progressDialog = ProgressDialog(this).build()
        progressDialog.show()
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
                progressDialog.dismiss()
            }
        }
    }

    private fun startTTSService(textToSpeak: String?) {
        val serviceIntent = Intent(this, TTSService::class.java)
        serviceIntent.putExtra("textToSpeak", textToSpeak)
        startService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (notificationReference != null) {
            NotificationUtils.cancelNotification()
        }
    }
}