/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.harmber2.suadat.MainActivity
import com.harmber2.suadat.R
import timber.log.Timber

/**
 * Firebase Cloud Messaging service for Harmber push notifications.
 *
 * ## FCM Topics
 * The app auto-subscribes to the following topics on first launch:
 * - `harmber_all`         — General announcements sent to all users
 * - `harmber_updates`     — New version / feature announcements
 * - `harmber_promotions`  — Promotional content (playlists, events, etc.)
 *
 * You can send push notifications from the Firebase Console → Cloud Messaging,
 * targeting any of the topics above (or a specific device token for testing).
 */
class HarmberMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.tag(TAG).d("FCM token refreshed: %s", token)
        // If you have a backend that needs the token, send it here.
        // For topic-based messaging this is not strictly required.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.tag(TAG).d(
            "FCM message received from: %s, data: %s",
            message.from,
            message.data,
        )

        val notification = message.notification
        val data = message.data

        val title = notification?.title ?: data["title"] ?: getString(R.string.app_name)
        val body = notification?.body ?: data["body"] ?: return

        showNotification(title, body, data)
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel (required for Android O+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Harmber push notifications for updates and announcements"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap opens main activity
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // Forward any deep-link or action from the data payload
            data["action"]?.let { putExtra("fcm_action", it) }
            data["deeplink"]?.let { putExtra("fcm_deeplink", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification,
        )
    }

    companion object {
        private const val TAG = "HarmberFCM"
        const val CHANNEL_ID = "harmber_push_notifications"
        const val CHANNEL_NAME = "Harmber Notifications"
        private const val NOTIFICATION_REQUEST_CODE = 1001

        /** Topics the app subscribes to — use these when sending from Firebase Console */
        const val TOPIC_ALL = "harmber_all"
        const val TOPIC_UPDATES = "harmber_updates"
        const val TOPIC_PROMOTIONS = "harmber_promotions"
    }
}
