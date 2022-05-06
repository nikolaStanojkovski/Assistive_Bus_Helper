package mk.ukim.finki.androidkotlinapplication.util.ocr

import android.app.Service
import android.content.Intent
import android.os.IBinder

class OCRService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        receiveInferenceUpdate()
//        LocalBroadcastManager.getInstance(this)
//            .registerReceiver(ttsReceiver, IntentFilter("tts_inference_update"))
//        startModel()

        return super.onStartCommand(intent, flags, startId)
    }
}