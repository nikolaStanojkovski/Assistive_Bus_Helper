package mk.ukim.finki.assistivebushelper

import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import assistivebushelper.R
import mk.ukim.finki.assistivebushelper.ui.CameraActivity
import mk.ukim.finki.assistivebushelper.ui.ProgressDialog
import mk.ukim.finki.assistivebushelper.util.InternetUtils
import mk.ukim.finki.assistivebushelper.util.ocr.OCRService
import mk.ukim.finki.assistivebushelper.util.tts.TTSService
import mk.ukim.finki.assistivebushelper.util.tts.TTSUtils
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressDialog: AlertDialog
    private lateinit var ttsServiceReceiver: BroadcastReceiver
    private lateinit var buttonSpeak: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonSpeak = findViewById(R.id.btnSpeak)
        sharedPreferences = getSharedPreferences("mk.ukim.finki.assistivebushelper", MODE_PRIVATE)
        receivePictureNumber(intent)

        if(TTSUtils.model == null && TTSUtils.synthesize_object == null && TTSUtils.vocoder == null) {
            if(checkFirstRun()) {
                if(InternetUtils.hasActiveInternetConnection(this)) {
                    sharedPreferences.edit().putBoolean("firstrun", false).apply()
                    initModels()
                } else {
                    progressDialog = ProgressDialog(this).build()
                    progressDialog.show()
                    Handler().postDelayed({
                        finish()
                        exitProcess(0)
                    }, 3000)
                }
            } else {
                initModels()
            }
        }

        buttonSpeak.setOnClickListener {
            buttonSpeak.isEnabled = false
            buttonSpeak.isClickable = false
            sendTTSUpdate(findViewById<TextView>(R.id.inputText).text.toString())
        }

        findViewById<ImageView>(R.id.btnCamera).setOnClickListener {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Toast.makeText(this, "No camera has been found on this device", Toast.LENGTH_LONG)
                    .show()
            } else {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun initModels() {
        progressDialog = ProgressDialog(this).build()
        progressDialog.show()
        receiveTTSInferenceUpdate()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(ttsServiceReceiver, IntentFilter("tts_service_update"))
        startTTSService()
        startOCRService()
    }

    private fun checkFirstRun(): Boolean {
        return (sharedPreferences.getBoolean("firstrun", true))
    }

    private fun receivePictureNumber(intent: Intent?) {
        if(intent != null) {
            if (intent.extras != null) {
                val bundle: Bundle = intent.extras!!

                val busNumber: String?  =
                    bundle.getString("busNumber")

                if (!busNumber.isNullOrEmpty()) {
                    sendTTSUpdate(busNumber)
                }
            }
        }
    }

    private fun receiveTTSInferenceUpdate() {
        ttsServiceReceiver = object : BroadcastReceiver() {
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

    private fun sendTTSUpdate(textToSpeak: String) {
        val serviceIntent = Intent("tts_inference_update")
        serviceIntent.putExtra("textToSpeak", textToSpeak)

        LocalBroadcastManager.getInstance(this).sendBroadcast(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(ttsServiceReceiver)
    }
}