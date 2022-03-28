package com.designdrivendevelopment.voicediary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RecordsAdapter(
    private val context: Context
) : ListAdapter<Record, RecordsAdapter.ViewHolder>(RecordsDiffUtil()) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val recordName = view.findViewById<TextView>(R.id.record_name)
        private val recordDate = view.findViewById<TextView>(R.id.record_date)

        fun bind(record: Record) {
            recordName.text = record.name
            recordDate.text = record.creationDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_record, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}