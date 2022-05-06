package mk.ukim.finki.androidkotlinapplication.ui

import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import mk.ukim.finki.androidkotlinapplication.R
import mk.ukim.finki.androidkotlinapplication.util.ocr.CameraUtils
import mk.ukim.finki.androidkotlinapplication.util.ocr.ShowCamera

@Suppress("DEPRECATION")
class CameraActivity : AppCompatActivity() {

    private lateinit var camera: Camera
    private lateinit var frameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

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
            val showCamera = ShowCamera(this, camera)
            frameLayout.addView(showCamera)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}