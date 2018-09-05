package co.infinum.example

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_settings.view.*

class SettingsAdapter(
    private var settingsItems: List<SettingsItem>
) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false))

    override fun getItemCount() = settingsItems.size

    fun updateDataSet(settingsItems: List<SettingsItem>) {
        this.settingsItems = settingsItems
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = settingsItems[position]
        holder.itemView.apply {
            nameView.text = item.name
            valueView.text = item.value
            setOnClickListener { item.onClick() }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}