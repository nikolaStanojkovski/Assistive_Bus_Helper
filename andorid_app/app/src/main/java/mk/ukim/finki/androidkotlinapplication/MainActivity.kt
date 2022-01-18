package mk.ukim.finki.androidkotlinapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startModel()
        inferenceModel()
    }

    private fun startModel() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py: Python = Python.getInstance()

        val synthsize_object = py.getModule("synthesize_android")
        val model = synthsize_object.callAttr("get_static_model")
        val vocoder = synthsize_object.callAttr("get_static_vocoder")

        ModelDataHolder.synthesize_object = synthsize_object
        ModelDataHolder.model = model
        ModelDataHolder.vocoder = vocoder
    }

    private fun inferenceModel() {
        val speakButton = findViewById<Button>(R.id.btnSpeak)
        val textValue = findViewById<TextView>(R.id.inputText).text

        speakButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                ModelDataHolder.synthesize_object!!.callAttr(
                    "main",
                    textValue,
                    ModelDataHolder.model,
                    ModelDataHolder.vocoder
                )
                withContext(Dispatchers.Main) {
                    print("Success!!!")
                }
            }.start()
        }
    }
}