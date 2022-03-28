package com.designdrivendevelopment.voicediary

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class MediaPlayerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var player: MediaPlayer? = null
    private val binder = LocalBinder()
    private var noClients: Boolean = false
    private var prevJob: Job? = null
    private var previousUri: Uri? = null
    private var isPaused = false
    var completionListener: (() -> Unit)? = null
    var isPlaying = false

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlayerService {
            return this@MediaPlayerService
        }
    }

    override fun onCreate() {
        player = MediaPlayer()
    }

    override fun onBind(p0: Intent?): IBinder {
        noClients = false
        return binder
    }

    override fun onRebind(intent: Intent?) {
        noClients = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        noClients = true
        prevJob?.cancel()
        val job = serviceScope.launch {
            delay(DELAY_TO_DESTROY_WITHOUT_CLIENTS)
            if (noClients) stopSelf()
        }
        prevJob = job
        return true
    }

    override fun onDestroy() {
        completionListener = null
        stop()
        player?.release()
    }

    fun play(audioSource: Uri) {
        if (!isPlaying) {
            isPlaying = true
            if (isPaused) {
                isPaused = false
                player?.start()
            } else {
                player?.prepareAsyncAndStart(audioSource)
            }
            previousUri = audioSource
        }
    }

    fun pause() {
        if (isPlaying && !isPaused) {
            isPlaying = false
            isPaused = true
            player?.pause()
        }
    }

    fun stop() {
        if (isPlaying) {
            isPlaying = false
            player?.apply {
                stop()
                reset()
            }
        }
    }

    private fun MediaPlayer.prepareAsyncAndStart(audioSource: Uri) {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setOnCompletionListener {
            completionListener?.invoke()
        }
        setDataSource(applicationContext, audioSource)
        setOnPreparedListener {
            it.start()
        }
        prepareAsync()
    }

    companion object {
        private const val DELAY_TO_DESTROY_WITHOUT_CLIENTS = 5000L

        fun start(context: Context) {
            val intent = Intent(context, MediaPlayerService::class.java)
            context.startService(intent)
        }
    }
}