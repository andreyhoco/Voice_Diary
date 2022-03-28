package com.designdrivendevelopment.voicediary

import android.app.Application
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler

class VoiceDiaryApplication : Application() {
    val appComponent by lazy { AppComponent(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        VK.addTokenExpiredHandler(tokenTracker)
    }

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            MainActivity.start(this@VoiceDiaryApplication)
        }
    }
}