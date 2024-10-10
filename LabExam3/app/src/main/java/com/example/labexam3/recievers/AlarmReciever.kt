package com.example.labexam3.recievers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.labexam3.MainActivity
import com.example.labexam3.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AlarmReceiver", "Alarm received") // Log when the alarm is triggered

        // Get the task name or birthday name from the intent
        val taskName = intent?.getStringExtra("birthday_name") ?: "Birthday"
        Log.d("AlarmReceiver", "Task Name: $taskName") // Log the task name

        // Call the method to create and show the notification
        createNotification(context, taskName)
    }

    // Method to create and show the notification
    private fun createNotification(context: Context, taskName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt() // Unique notification ID using timestamp

        // Create the notification channel if the Android version is 8.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "birthday_notifications"
            val channelName = "Birthday Notifications"
            val channelDescription = "Channel for birthday notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create the intent to open MainActivity when the notification is clicked
        val notificationIntent = Intent(context, MainActivity::class.java)

        // Set PendingIntent with appropriate flags based on Android version
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            pendingIntentFlags
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, "birthday_notifications")
            .setContentTitle("Birthday Reminder")
            .setContentText("It's $taskName's birthday today!")
            .setSmallIcon(R.drawable.logo) // Ensure the drawable exists
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss notification after tapping
            .build()

        // Show the notification
        notificationManager.notify(notificationId, notification)
    }
}
