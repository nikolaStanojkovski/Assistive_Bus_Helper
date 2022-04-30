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
import mk.ukim.finki.androidkotlinapplication.ui.ProgressDialog
import mk.ukim.finki.androidkotlinapplication.util.ocr.CameraUtils
import mk.ukim.finki.androidkotlinapplication.util.tts.TTSService


class MainActivity : AppCompatActivity() {

    private lateinit var progressDialog: AlertDialog
    private lateinit var serviceReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressDialog = ProgressDialog(this).build()
        progressDialog.show()

        receiveInferenceUpdate()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(serviceReceiver, IntentFilter("tts_service_update"))
        startTTSService()

        findViewById<Button>(R.id.btnSpeak).setOnClickListener {
            sendTTSUpdate(findViewById<TextView>(R.id.inputText).text.toString())
        }

        findViewById<ImageView>(R.id.btnCamera).setOnClickListener {
            CameraUtils.askCameraPermissions(this)
        }
    }


    private fun startTTSService() {
        val serviceIntent = Intent(this, TTSService::class.java)
        startService(serviceIntent)
    }

    private fun sendTTSUpdate(textToSpeak: String?) {
        val serviceIntent = Intent("tts_inference_update")
        serviceIntent.putExtra("textToSpeak", textToSpeak)

        LocalBroadcastManager.getInstance(this).sendBroadcast(serviceIntent)
    }

    private fun receiveInferenceUpdate() {
        serviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.extras != null) {
                    val bundle: Bundle = intent.extras!!

                    val closeProgressDialog: Boolean = bundle.getBoolean("closeProgressDialog")
                    if (closeProgressDialog) {
                        progressDialog.dismiss()
                    }
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CameraUtils.CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CameraUtils.openCamera(this)
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission is required to use camera. $grantResults",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CameraUtils.CAMERA_REQUEST_CODE && data != null) {
            if (data.extras != null) {
                // TODO: Pass the image where it is needed.
                val image = data.extras!!.get("data")
            }
        }
    }
}