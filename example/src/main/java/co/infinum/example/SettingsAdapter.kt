package co.infinum.example

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_settings.view.*

class SettingsAdapter(
    private var settingsItems: List<SettingsItem>
) : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        if (viewType == 0) {
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false))
        } else {
            HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.header_settings, parent, false))
        }

    override fun getItemCount() = settingsItems.size

    fun updateDataSet(settingsItems: List<SettingsItem>) {
        this.settingsItems = settingsItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = settingsItems[position].type

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val item = settingsItems[position]
        if (holder is ItemViewHolder) {
            holder.itemView.apply {
                nameView.text = item.name
                valueView.text = item.value
                setOnClickListener { item.onClick?.invoke() }
            }
        } else if (holder is HeaderViewHolder) {
            (holder.itemView as TextView).apply {
                text = item.name
            }
        }
    }

    class ItemViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
    class HeaderViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}