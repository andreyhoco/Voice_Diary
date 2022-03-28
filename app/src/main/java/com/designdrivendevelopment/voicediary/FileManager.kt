package com.designdrivendevelopment.voicediary

interface FileManager {
    fun getRecordName(): String

    fun getPathToRecords(): String

    suspend fun getRecords(): List<Record>

    suspend fun renameRecord(oldName: String, newName: String): Boolean

    suspend fun deleteRecords(records: List<Record>): Boolean
}

