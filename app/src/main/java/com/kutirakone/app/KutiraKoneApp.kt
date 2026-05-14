package com.kutirakone.app

import android.app.Application
import com.google.firebase.FirebaseApp

class KutiraKoneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
