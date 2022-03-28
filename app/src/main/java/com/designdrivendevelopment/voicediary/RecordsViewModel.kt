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
    private var currentPlayingRecord: Record? = null
    private var _playingRecord: MutableLiveData<Record?> = MutableLiveData(null)
    private val _isRecording = MutableLiveData<Boolean>(false)
    private val _isPlaying = MutableLiveData<Boolean>(false)
    private val _records = MutableLiveData<List<Record>>(emptyList())
    private val _newRecordPath = MutableLiveData<String>()
    val isRecording: LiveData<Boolean>
        get() = _isRecording
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying
    val records: LiveData<List<Record>>
        get() = _records
    val newRecordPath: LiveData<String>
        get() = _newRecordPath
    val playingRecord: LiveData<Record?>
        get() = _playingRecord

    fun updateRecordsList() {
        viewModelScope.launch(coroutineDispatcher) {
            val records = if (_isRecording.value == true) {
                fileManager.getRecords().filterNot { record -> record.name == currentRecordName }
            } else fileManager.getRecords()
            if (currentPlayingRecord != null) {
                val mappedRecords = records.map { record ->
                    if (record.uri == currentPlayingRecord?.uri) {
                        record.copy(isPlaying = currentPlayingRecord?.isPlaying == true)
                    } else record
                }
                _records.postValue(mappedRecords)
            } else {
                _records.postValue(records)
            }
        }
    }

    fun deleteRecord(recordPos: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            val recordToDelete = _records.value?.get(recordPos) ?: return@launch
            launch {
                fileManager.deleteRecords(listOf(recordToDelete))
            }.join()
            updateRecordsList()
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

    fun onCompletion() {
        setPlayingRecord(null)
    }

    fun setPlayingRecord(record: Record?) {
        when {
            record == currentPlayingRecord -> return
            record == null -> {
                _playingRecord.value = record
                currentPlayingRecord = record
                _records.value = _records.value?.map { record ->
                    if (record.isPlaying) record.copy(isPlaying = false) else record
                }
            }
            currentPlayingRecord?.equalsExcludePlaying(record) == true -> {
                _playingRecord.value = record
                currentPlayingRecord = record
                _records.value = records.value?.map { oldRecord ->
                    if (currentPlayingRecord?.equalsExcludePlaying(oldRecord) == true) {
                        oldRecord.copy(isPlaying = currentPlayingRecord?.isPlaying == true)
                    } else oldRecord
                }
            }
            else -> {
                _playingRecord.value = record
                _records.value = records.value?.map { oldRecord ->
                    when {
                        (oldRecord.equalsExcludePlaying(record)) -> {
                            oldRecord.copy(isPlaying = record.isPlaying)
                        }
                        (currentPlayingRecord?.equalsExcludePlaying(oldRecord) == true) -> {
                            oldRecord.copy(isPlaying = false)
                        }
                        else -> oldRecord
                    }
                }
                currentPlayingRecord = record
            }
        }
    }

    companion object {
        private const val FILE_EXTENSION = ".mp3"
    }
}