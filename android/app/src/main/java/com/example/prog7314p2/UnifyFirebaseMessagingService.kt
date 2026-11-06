package com.example.prog7314p2

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.prog7314p2.Models.DeviceTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnifyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "UnifyFCM"
        private const val CHANNEL_ID = "unify_notifications"

        fun registerCurrentTokenWithBackend() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    sendTokenToBackend(token)
                }
            }
        }

        fun sendTokenToBackend(token: String) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.instance.registerDeviceToken(DeviceTokenRequest(token))
                    Log.d(TAG, "Successfully registered device token with backend: $token")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to register device token with backend", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "Firebase messaging token refreshed: $token")
        sendTokenToBackend(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "FCM Message Received: notification=${message.notification?.body}, data=${message.data}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Unify Retail"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new notification"

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {

        // Android 13+ requires notification permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Notification permission not granted")
                return
            }
        }

        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("TARGET_NAV", "ORDER_HISTORY")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.unify)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager

        manager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Unify Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Order status updates, product news, and promotions"
                enableVibration(true)
                setShowBadge(true)
            }

            val manager = getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}
