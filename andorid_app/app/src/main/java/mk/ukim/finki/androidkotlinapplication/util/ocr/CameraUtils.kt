package mk.ukim.finki.androidkotlinapplication.util.ocr

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import mk.ukim.finki.androidkotlinapplication.R

class CameraUtils {
    companion object {
        const val CAMERA_PERMISSION_CODE = 101
        const val CAMERA_REQUEST_CODE = 102

        fun askCameraPermissions(context: Activity) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            } else {
                openCamera(context)
            }
        }

        fun openCamera(context: Activity) {
            val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camera.putExtra("android.intent.extra.quickCapture", true)

            try {
                context.startActivityForResult(camera, CAMERA_REQUEST_CODE)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(context, R.string.application_not_found, Toast.LENGTH_LONG).show()
            }
        }
    }
}