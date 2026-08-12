package com.ailenezareti.panelapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ailenezareti.panelapp.R

class RangeAdapter(
    private val items: List<Pair<String, String>>, // key to label
    private var activeKey: String,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<RangeAdapter.VH>() {

    class VH(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_range_chip, parent, false) as TextView
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (key, label) = items[position]
        holder.text.text = label
        val active = key == activeKey
        holder.text.setBackgroundResource(if (active) R.drawable.bg_chip_active else R.drawable.bg_chip)
        holder.text.setTextColor(
            holder.itemView.context.getColor(if (active) R.color.ink else R.color.slate)
        )
        holder.text.setOnClickListener {
            activeKey = key
            notifyDataSetChanged()
            onSelect(key)
        }
    }

    override fun getItemCount() = items.size
}
