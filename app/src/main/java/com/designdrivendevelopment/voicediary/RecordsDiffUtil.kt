package com.designdrivendevelopment.voicediary

import androidx.recyclerview.widget.DiffUtil

class RecordsDiffUtil : DiffUtil.ItemCallback<Record>() {
    override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem == newItem
    }
}