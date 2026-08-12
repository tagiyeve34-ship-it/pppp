package com.ailenezareti.panelapp.ui

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ailenezareti.panelapp.R

data class ActivityItem(
    val iconRes: Int,
    val title: String,
    val time: String,
    val colorRes: Int = R.color.teal,
    val bgColorRes: Int = R.color.tealSoft
)

class ActivityAdapter(private val items: List<ActivityItem>) : RecyclerView.Adapter<ActivityAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.itemIcon)
        val title: TextView = view.findViewById(R.id.itemTitle)
        val time: TextView = view.findViewById(R.id.itemTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.time.text = item.time
        holder.icon.setImageResource(item.iconRes)
        holder.icon.setColorFilter(ContextCompat.getColor(holder.itemView.context, item.colorRes), PorterDuff.Mode.SRC_IN)
        val bg = holder.icon.background.mutate()
        (bg as? android.graphics.drawable.GradientDrawable)?.setColor(
            ContextCompat.getColor(holder.itemView.context, item.bgColorRes)
        )
    }

    override fun getItemCount() = items.size
}
