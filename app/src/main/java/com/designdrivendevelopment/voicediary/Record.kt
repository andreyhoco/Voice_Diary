package com.designdrivendevelopment.voicediary

import android.net.Uri

data class Record(
    val name: String,
    val length: Long,
    val creationDate: String,
    val uri: Uri
)
