package mk.ukim.finki.assistivebushelper.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import assistivebushelper.R
import mk.ukim.finki.assistivebushelper.MainActivity
import mk.ukim.finki.assistivebushelper.util.ocr.CameraUtils
import mk.ukim.finki.assistivebushelper.util.ocr.ShowCamera

@Suppress("DEPRECATION")
class CameraActivity : AppCompatActivity() {

    private lateinit var ocrServiceReceiver: BroadcastReceiver
    private lateinit var camera: Camera
    private lateinit var showCamera: ShowCamera
    private lateinit var frameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        receiveOCRInferenceUpdate()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(ocrServiceReceiver, IntentFilter("ocr_service_update"))

        frameLayout = findViewById(R.id.cameraFrameLayout)
    }

    override fun onResume() {
        super.onResume()

        if (CameraUtils.checkCameraPermissions(this)) {
            displayCamera()
        } else {
            CameraUtils.askCameraPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CameraUtils.CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayCamera()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission is required to use camera. $grantResults",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayCamera() {
        try {
            camera = Camera.open()
            showCamera = ShowCamera(this, camera)
            frameLayout.addView(showCamera)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun receiveOCRInferenceUpdate() {
        val cameraActivityContext = this
        ocrServiceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.extras != null) {
                    val bundle: Bundle = intent.extras!!

                    val busNumber: String? =
                        bundle.getString("busNumber")
                    if (!busNumber.isNullOrEmpty() && busNumber.all { char -> char.isDigit() }) {
                        showCamera.cancelTimer()
                        val mainActivityIntent =
                            Intent(cameraActivityContext, MainActivity::class.java)
                        mainActivityIntent.putExtra("busNumber", busNumber)
                        startActivity(mainActivityIntent)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(ocrServiceReceiver)
    }
}