package com.designdrivendevelopment.voicediary

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Calendar

class FileManagerImpl(
    private val context: Context,
    private val coroutineDispatcher: CoroutineDispatcher
) : FileManager {
    private val recordsDir = context.filesDir

    override fun getRecordName(): String {
        return DEFAULT_FILE_PREFIX + Calendar.getInstance().timeInMillis.toString() + FILE_EXTENSION
    }

    override fun getPathToRecords(): String {
        return recordsDir.path
    }

    override suspend fun renameRecord(
        oldName: String,
        newName: String
    ): Boolean = withContext(coroutineDispatcher) {
        return@withContext try {
            recordsDir.listFiles()
                ?.firstOrNull { it.name == oldName }
                ?.renameTo(File(recordsDir, newName)) ?: false
        } catch (e: IOException) {
            false
        }
    }

    override suspend fun getRecords(): List<Record> = withContext(coroutineDispatcher) {
        return@withContext try {
            recordsDir.listFiles { file ->
                file.canRead() && file.isFile && file.name.endsWith(FILE_EXTENSION)
            }?.map { Record(it.name) } ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }

    override suspend fun deleteRecords(
        records: List<Record>
    ): Boolean = withContext(coroutineDispatcher) {
        return@withContext try {
            records.forEach { record -> context.deleteFile(record.name) }
            true
        } catch (e: IOException) {
            false
        }
    }

    companion object {
        private const val DEFAULT_FILE_PREFIX = "record"
        private const val FILE_EXTENSION = ".mp3"
    }
}