package mk.ukim.finki.androidkotlinapplication

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import mk.ukim.finki.androidkotlinapplication.ui.CameraActivity
import mk.ukim.finki.androidkotlinapplication.ui.ProgressDialog
import mk.ukim.finki.androidkotlinapplication.util.ocr.OCRService
import mk.ukim.finki.androidkotlinapplication.util.tts.TTSService


class MainActivity : AppCompatActivity() {

    private lateinit var progressDialog: AlertDialog
    private lateinit var serviceReceiver: BroadcastReceiver
    private lateinit var buttonSpeak: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonSpeak = findViewById(R.id.btnSpeak)
        progressDialog = ProgressDialog(this).build()
        progressDialog.show()

        receiveInferenceUpdate()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(serviceReceiver, IntentFilter("tts_service_update"))
        startTTSService()
        startOCRService()

        buttonSpeak.setOnClickListener {
            buttonSpeak.isEnabled = false
            buttonSpeak.isClickable = false
            sendTTSUpdate(findViewById<TextView>(R.id.inputText).text.toString())
        }

        findViewById<ImageView>(R.id.btnCamera).setOnClickListener {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show()
            } else {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun receiveInferenceUpdate() {
        serviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.extras != null) {
                    val bundle: Bundle = intent.extras!!

                    val closeProgressDialog: Boolean =
                        bundle.getBoolean("closeProgressDialog", false)
                    val inferenceFinished: Boolean = bundle.getBoolean("inferenceFinished", false)
                    if (closeProgressDialog) {
                        progressDialog.dismiss()
                    }
                    if (inferenceFinished) {
                        buttonSpeak.isEnabled = true
                        buttonSpeak.isClickable = true
                    }
                }
            }
        }
    }

    private fun startTTSService() {
        val serviceIntent = Intent(this, TTSService::class.java)
        startService(serviceIntent)
    }

    private fun startOCRService() {
        val serviceIntent = Intent(this, OCRService::class.java)
        startService(serviceIntent)
    }

    private fun sendTTSUpdate(textToSpeak: String?) {
        val serviceIntent = Intent("tts_inference_update")
        serviceIntent.putExtra("textToSpeak", textToSpeak)

        LocalBroadcastManager.getInstance(this).sendBroadcast(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver)
    }
}