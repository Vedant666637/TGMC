package com.tgm.tgmc

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TgmcApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase — reads google-services.json automatically
        FirebaseApp.initializeApp(this)
        Log.i("TgmcApplication", "🔥 Firebase initialized — project: ${FirebaseApp.getInstance().options.projectId}")
    }
}
