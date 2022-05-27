package mk.ukim.finki.assistivebushelper.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import assistivebushelper.R

class NotificationUtils {

    companion object {

        private lateinit var notificationManager: NotificationManager
        private const val CHANNEL_ID = 1000
        private const val CHANNEL_NAME = "mk.ukim.finki.assistivebushelper"

        private const val progressMax = 100
        private const val contentTitle = "Assistive Bus Helper"
        private const val contentTextPlaceholder = "There is currently no ongoing label inference"
        private const val contentTextProgress = "Label inference in progress..."
        private const val contentTextFinish = "Inference finished"

        fun showNotification(context: Context): NotificationCompat.Builder? {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager = getNotificationManager(context)

                val notification = getNotification(context)

                updateNotification(notification)
                return notification
            } else {
                Toast.makeText(
                    context,
                    "The application cannot show notifications on your device type",
                    Toast.LENGTH_LONG
                ).show()

                return null
            }
        }

        fun updateNotificationProgress(
            notification: NotificationCompat.Builder,
            isFinished: Boolean
        ) {
            if (isFinished) {
                notification.setContentText(contentTextFinish)
                    .setProgress(0, 0, false)
            } else {
                notification.setContentText(contentTextProgress)
                    .setProgress(progressMax, progressMax / 2, true)
            }

            updateNotification(notification)
        }

        fun cancelNotification() {
            notificationManager.cancel(CHANNEL_ID)
        }

        private fun updateNotification(
            notification: NotificationCompat.Builder
        ) {
            notificationManager.notify(CHANNEL_ID, notification.build())
        }

        private fun getNotification(
            context: Context
        ): NotificationCompat.Builder {

            return NotificationCompat.Builder(context, CHANNEL_NAME)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(contentTitle)
                .setContentText(contentTextPlaceholder)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setAutoCancel(false)
                .setProgress(0, 0, false)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun getNotificationManager(context: Context): NotificationManager {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_NAME,
                    contentTextPlaceholder,
                    NotificationManager.IMPORTANCE_HIGH
                )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLACK
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
            return notificationManager
        }
    }
}