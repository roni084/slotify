package com.example.slotify

import android.app.Application
import com.cloudinary.android.MediaManager

class MediaManagerInitialization : Application() {
    override fun onCreate() {
        super.onCreate()

        // Cloudinary configuration
        val config = mapOf(
            "cloud_name" to "db9iddpxf",
            "api_key" to "782139581913668",
            "api_secret" to "wWWNpVtBVJpBQUkfwhWEKrd0nbQ"
        )
        MediaManager.init(this, config)
    }
}