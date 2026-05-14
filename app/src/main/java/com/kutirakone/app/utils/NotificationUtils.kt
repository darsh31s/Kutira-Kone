package com.kutirakone.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kutirakone.app.R

object NotificationUtils {
    const val CHANNEL_TRADE_REQUESTS = "TRADE_REQUESTS"
    const val CHANNEL_LISTING_EXPIRY = "LISTING_EXPIRY"
    const val CHANNEL_MESSAGES = "MESSAGES"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val tradeChannel = NotificationChannel(CHANNEL_TRADE_REQUESTS, "Trade Requests", NotificationManager.IMPORTANCE_HIGH)
            val expiryChannel = NotificationChannel(CHANNEL_LISTING_EXPIRY, "Listing Expiry", NotificationManager.IMPORTANCE_DEFAULT)
            val messagesChannel = NotificationChannel(CHANNEL_MESSAGES, "Messages", NotificationManager.IMPORTANCE_HIGH)

            manager.createNotificationChannels(listOf(tradeChannel, expiryChannel, messagesChannel))
        }
    }

    fun buildTradeNotification(context: Context, title: String, body: String, data: Map<String, String>): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, CHANNEL_TRADE_REQUESTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
    }
}
