package com.designdrivendevelopment.voicediary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordsViewModel(
    private val fileManager: FileManager,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {
    private var currentRecordName = ""
    private val _isRecording = MutableLiveData<Boolean>(false)
    private val _records = MutableLiveData<List<Record>>(emptyList())
    private val _newRecordPath = MutableLiveData<String>()
    val isRecording: LiveData<Boolean>
        get() = _isRecording
    val records: LiveData<List<Record>>
        get() = _records
    val newRecordPath: LiveData<String>
        get() = _newRecordPath

    fun updateRecordsList() {
        viewModelScope.launch(coroutineDispatcher) {
            val records = if (_isRecording.value == true) {
                fileManager.getRecords().filterNot { record -> record.name == currentRecordName }
            } else fileManager.getRecords()
            _records.postValue(records)
        }
    }

    fun updateNewRecordPath() {
        currentRecordName = fileManager.getRecordName()
        _newRecordPath.value = fileManager.getPathToRecords() + "/" + currentRecordName
    }

    fun renameRecord(oldName: String, newName: String) {
        viewModelScope.launch(coroutineDispatcher) {
            fileManager.renameRecord(oldName, newName + FILE_EXTENSION)
            updateRecordsList()
        }
    }

    fun saveCurrRecordAs(name: String) {
        renameRecord(currentRecordName, name)
    }

    fun setRecordingStatus(isRecording: Boolean) {
        _isRecording.value = isRecording
    }

    companion object {
        private const val FILE_EXTENSION = ".mp3"
    }
}