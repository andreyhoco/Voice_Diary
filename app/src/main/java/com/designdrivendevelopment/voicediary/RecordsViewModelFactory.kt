package com.designdrivendevelopment.voicediary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecordsViewModelFactory(
    private val fileManager: FileManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordsViewModel::class.java)) {
            return RecordsViewModel(fileManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}