package com.designdrivendevelopment.voicediary

import android.content.Context

import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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
            val calendar = Calendar.getInstance()
            val currDay = with(calendar) {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 1)
                time
            }

            val mediaMetadataRetriever = MediaMetadataRetriever()
            val records = recordsDir.listFiles { file ->
                file.canRead() && file.isFile && file.name.endsWith(FILE_EXTENSION)
            }?.map { file -> file.toRecord(mediaMetadataRetriever, currDay, calendar) } ?: emptyList()

            mediaMetadataRetriever.release()
            records
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

    private fun File.toRecord(
        mediaMetadataRetriever: MediaMetadataRetriever,
        currDay: Date,
        calendar: Calendar
    ): Record {
        mediaMetadataRetriever.setDataSource(absolutePath)

        val mediaMetadataDate = mediaMetadataRetriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE).orEmpty()
        val date = SimpleDateFormat("yyyyMMdd'T'hhmmss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }.parse(mediaMetadataDate)
        val creationDate = if (date != null) {
            calendar.time = date
            val hours = calendar.get(Calendar.HOUR_OF_DAY).convertToTwoDigits()
            val minutes = calendar.get(Calendar.MINUTE).convertToTwoDigits()
            if (calendar.timeInMillis > currDay.time) {
                "Сегодня в $hours:$minutes"
            } else {
                val year = calendar.get(Calendar.YEAR).convertToTwoDigits()
                val month = calendar.get(Calendar.MONTH).convertToTwoDigits()
                val day = calendar.get(Calendar.DAY_OF_MONTH).convertToTwoDigits()
                "$day.$month.$year в $hours:$minutes"
            }
        } else {
            ""
        }

        return Record(
            name = name.dropLast(FILE_EXTENSION.length),
            length = mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                ?: DEFAULT_LENGTH,
            creationDate = creationDate,
            uri = toUri()
        )
    }

    private fun Int.convertToTwoDigits(): String {
        return if (this in 0..9) "0$this" else this.toString()
    }

    companion object {
        private const val DEFAULT_FILE_PREFIX = "record"
        private const val DEFAULT_LENGTH = 0L
        private const val FILE_EXTENSION = ".mp3"
    }
}