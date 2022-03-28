package com.designdrivendevelopment.voicediary

import android.net.Uri

data class Record(
    val name: String,
    val length: Long,
    val creationDate: String,
    val uri: Uri,
    val isPlaying: Boolean = false
) {
    fun equalsExcludePlaying(other: Record): Boolean {
        return name == other.name
                && length == other.length
                && creationDate == other.creationDate
                && uri == other.uri
    }
}
