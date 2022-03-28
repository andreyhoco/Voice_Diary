package com.designdrivendevelopment.voicediary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecordsViewModel : ViewModel() {
    private val _isRecording = MutableLiveData<Boolean>(false)
    val isRecording: LiveData<Boolean>
        get() = _isRecording

    fun setRecordingStatus(isRecording: Boolean) {
        _isRecording.value = isRecording
    }
}