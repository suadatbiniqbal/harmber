package com.harmber.suadat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "harmber_notifications"
        private const val CHANNEL_NAME = "Harmber Notifications"
    }

    /**
     * Called when a new FCM token is generated
     * This happens on:
     * - First app install
     * - App reinstall
     * - User clears app data
     * - Token rotation by Firebase
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")

        // TODO: Send token to your backend server
        sendTokenToServer(token)
    }

    /**
     * Called when a message is received from Firebase
     * This works when app is in:
     * - Foreground
     * - Background (only data payload)
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "From: ${message.from}")

        // Check if message contains notification payload
        message.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")

            showNotification(
                title = it.title ?: "New Message",
                body = it.body ?: "",
                data = message.data
            )
        }

        // Check if message contains data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Data Payload: ${message.data}")
            handleDataPayload(message.data)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        createNotificationChannel(notificationManager)

        // Create intent for when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data as extras
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get default notification sound
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Change to your icon
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // Show notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for Harmber app notifications"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle custom data payload
        // Example: Navigate to specific screen, update UI, etc.
        val action = data["action"]
        val userId = data["userId"]

        Log.d(TAG, "Action: $action, UserId: $userId")

        // Process based on action type
        when (action) {
            "open_profile" -> {
                // Handle profile opening
            }
            "new_message" -> {
                // Handle new message
            }
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Implement your API call to send token to backend
        // Example:
        // RetrofitClient.api.updateFcmToken(token)

        // Or save to SharedPreferences for later use
        getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }
}
