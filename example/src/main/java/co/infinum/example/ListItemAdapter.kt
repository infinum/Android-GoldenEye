package co.infinum.example

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ListItemAdapter<T>(
    private val items: List<ListItem<T>>,
    private val onClick: (T) -> Unit
) : RecyclerView.Adapter<ListItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)=
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView.findViewById<TextView>(R.id.textView).text = item.text
        holder.itemView.setOnClickListener { onClick(item.realItem) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

}
