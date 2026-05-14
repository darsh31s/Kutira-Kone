package com.kutirakone.app

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kutirakone.app.repository.AuthRepository

class KutiraFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val authRepo = AuthRepository()
        val user = authRepo.getCurrentUser()
        if (user != null) {
            // Usually need a coroutine scope here, but kept simple
            // In a real app we'd use WorkManager or a lifecycle aware scope
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Here we'd build the notification using NotificationUtils
        // and issue it to the NotificationManager
    }
}
