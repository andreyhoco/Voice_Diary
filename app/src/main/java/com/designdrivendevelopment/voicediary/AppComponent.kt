package com.designdrivendevelopment.voicediary

import android.content.Context
import kotlinx.coroutines.Dispatchers

class AppComponent(context: Context) {
    val fileManager by lazy { FileManagerImpl(context, Dispatchers.IO) }
}