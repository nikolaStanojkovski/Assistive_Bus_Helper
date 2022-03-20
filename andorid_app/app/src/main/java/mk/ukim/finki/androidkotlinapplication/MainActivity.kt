package mk.ukim.finki.androidkotlinapplication

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mk.ukim.finki.androidkotlinapplication.ui.ProgressDialogCreator
import mk.ukim.finki.androidkotlinapplication.util.ModelUtils
import mk.ukim.finki.androidkotlinapplication.util.NotificationUtils
import java.io.File


class MainActivity : AppCompatActivity() {

    private var notificationReference: NotificationCompat.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startModel()

        findViewById<Button>(R.id.btnSpeak).setOnClickListener {
            inferenceModel(findViewById<TextView>(R.id.inputText).text.toString())
        }
    }

    private fun startModel() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val context = this
        val progressDialog = ProgressDialogCreator.create(this)
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            val py: Python = Python.getInstance()

            val synthesizeObject = py.getModule("synthesize_android")
            val model = synthesizeObject.callAttr("get_static_model")
            val vocoder = synthesizeObject.callAttr("get_static_vocoder")

            ModelUtils.synthesize_object = synthesizeObject
            ModelUtils.model = model
            ModelUtils.vocoder = vocoder

            withContext(Dispatchers.Main) {
                val statusMessageView = findViewById<TextView>(R.id.txtStatusMessage)
                statusMessageView.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.green
                    )
                )
                statusMessageView.text = resources.getText(R.string.model_loading_message)
                notificationReference = NotificationUtils.showNotification(context)
                progressDialog.dismiss()
            }
        }
    }

    private fun inferenceModel(textValue: String) {
        val outputValue = findViewById<TextView>(R.id.txtSpeakText)
        val verifiedSymbol = findViewById<ImageView>(R.id.imgViewVerified)
        val progressBar = findViewById<ProgressBar>(R.id.inferenceProgressBar)

        if (notificationReference != null) {
            NotificationUtils.updateNotificationProgress(notificationReference!!, false)
        }
        progressBar.visibility = View.VISIBLE
        outputValue.visibility = View.VISIBLE
        outputValue.text = resources.getText(R.string.inference_message)
        verifiedSymbol.visibility = View.INVISIBLE

        CoroutineScope(Dispatchers.Default).launch {
            ModelUtils.synthesize_object!!.callAttr(
                "main",
                textValue,
                ModelUtils.model,
                ModelUtils.vocoder
            )

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    resources.getText(R.string.audio_generated_message),
                    Toast.LENGTH_LONG
                ).show()

                progressBar.visibility = View.INVISIBLE
                outputValue.visibility = View.INVISIBLE
                verifiedSymbol.visibility = View.VISIBLE

                if (notificationReference != null) {
                    NotificationUtils.updateNotificationProgress(notificationReference!!, true)
                }
                playFile(textValue)
            }
        }
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
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (notificationReference != null) {
            NotificationUtils.cancelNotification()
        }
    }
}