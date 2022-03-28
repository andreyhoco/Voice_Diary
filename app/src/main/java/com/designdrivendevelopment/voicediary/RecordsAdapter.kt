package com.designdrivendevelopment.voicediary

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RecordsAdapter(
    private val context: Context,
    private val onPlayClicked: (Record) -> Unit,
    private val onPauseClicked: (Record) -> Unit
) : ListAdapter<Record, RecordsAdapter.ViewHolder>(RecordsDiffUtil()) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val recordName = view.findViewById<TextView>(R.id.record_name)
        private val recordDate = view.findViewById<TextView>(R.id.record_date)
        val playButton: Button = view.findViewById<Button>(R.id.play_audio_button)
        val pauseButton: Button = view.findViewById<Button>(R.id.pause_audio_button)

        fun bind(record: Record) {
            recordName.text = record.name
            recordDate.text = record.creationDate
            playButton.isVisible = !record.isPlaying
            playButton.isEnabled = !record.isPlaying
            pauseButton.isVisible = record.isPlaying
            pauseButton.isEnabled = record.isPlaying
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_record, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = getItem(position)
        holder.bind(record)
        holder.playButton.setOnClickListener {
            onPlayClicked(record.copy(isPlaying = true))
        }
        holder.pauseButton.setOnClickListener {
            onPauseClicked(record.copy(isPlaying = false))
        }
    }
}