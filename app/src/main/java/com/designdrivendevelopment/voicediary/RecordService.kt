package com.designdrivendevelopment.voicediary

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class RecordService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var recorder: MediaRecorder? = null
    private val binder = LocalBinder()
    private var noClients: Boolean = false
    private var prevJob: Job? = null
    var isRecording = false

    inner class LocalBinder : Binder() {
        fun getService(): RecordService {
            return this@RecordService
        }
    }

    override fun onCreate() {
        recorder = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            MediaRecorder()
        } else {
            MediaRecorder(this)
        }
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
        stopRecord()
        recorder?.release()
    }

    fun startRecord(pathToFile: String) {
        if (!isRecording) {
            isRecording = true
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(pathToFile)
                prepare()
                start()
            }
        }
    }

    fun stopRecord() {
        if (isRecording) {
            isRecording = false
            recorder?.apply {
                stop()
                reset()
            }
        }
    }

    companion object {
        private const val DELAY_TO_DESTROY_WITHOUT_CLIENTS = 5000L

        fun start(context: Context) {
            val intent = Intent(context, RecordService::class.java)
            context.startService(intent)
        }
    }
}