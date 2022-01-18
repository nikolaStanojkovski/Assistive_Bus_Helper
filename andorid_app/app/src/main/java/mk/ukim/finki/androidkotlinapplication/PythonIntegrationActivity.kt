package mk.ukim.finki.androidkotlinapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PythonIntegrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_python_integration)

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