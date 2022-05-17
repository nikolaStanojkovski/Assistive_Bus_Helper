package mk.ukim.finki.assistivebushelper.util.ocr

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import assistivebushelper.R
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OCRService : Service() {
    private lateinit var ocrReceiver: BroadcastReceiver

    override fun onBind(intent: Intent?): IBinder {
        receiveInferenceUpdate()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(ocrReceiver, IntentFilter("ocr_inference_update"))
        startModel()

        return Binder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        receiveInferenceUpdate()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(ocrReceiver, IntentFilter("ocr_inference_update"))
        startModel()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startModel() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        CoroutineScope(Dispatchers.Default).launch {
            val py: Python = Python.getInstance()

            val busNumberClassificationObject = py.getModule("compute_bus_number")
            val model = busNumberClassificationObject.callAttr("get_static_model")

            OCRUtils.bus_number_classification_object = busNumberClassificationObject
            OCRUtils.model = model
        }
    }

    private fun inferenceModel(pictureToProcess: ByteArray) {
        val busNumberObject = OCRUtils.bus_number_classification_object!!.callAttr(
            "predict_img_class",
            pictureToProcess,
            OCRUtils.model
        )

        if(busNumberObject != null) {
            val busNumber = busNumberObject.toString()
            if(busNumber.isNotEmpty()) {
                Toast.makeText(
                    applicationContext,
                    resources.getText(R.string.bus_number_generated_message),
                    Toast.LENGTH_LONG
                ).show()

                sendInferenceUpdate(busNumber)
            }
        }
    }

    private fun sendInferenceUpdate(busNumber: String) {
        val activityIntent = Intent("ocr_service_update")
        activityIntent.putExtra("busNumber", busNumber)

        LocalBroadcastManager.getInstance(this).sendBroadcast(activityIntent)
    }

    private fun receiveInferenceUpdate() {
        ocrReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.extras != null) {
                    val bundle: Bundle = intent.extras!!

                    val pictureToProcess: ByteArray? = bundle.getByteArray("pictureToProcess")
                    if (pictureToProcess != null) {
                        inferenceModel(pictureToProcess)
                    }
                }
            }

        }
    }
}