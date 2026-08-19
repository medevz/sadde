package com.example

import android.app.Application
import android.util.Log

class ShreyaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("ShreyaApplication", "Shreya AI Assistant Application initialized.")
    }
}
