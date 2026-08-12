package com.ailenezareti.panelapp.ui

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ailenezareti.panelapp.R
import com.ailenezareti.panelapp.model.AlertEntry
import java.text.SimpleDateFormat
import java.util.*

class AlertsAdapter(
    private val items: MutableList<AlertEntry>,
    private val onMarkRead: (AlertEntry) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val root: android.view.View = view.findViewById(R.id.alertRoot)
        val icon: ImageView = view.findViewById(R.id.alertIcon)
        val message: TextView = view.findViewById(R.id.alertMessage)
        val time: TextView = view.findViewById(R.id.alertTime)
        val dot: android.view.View = view.findViewById(R.id.unreadDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val alert = items[position]
        val ctx = holder.itemView.context
        holder.message.text = alert.message
        holder.time.text = fmtTime(alert.created_at)

        val (color, bgColor) = when (alert.alert_type) {
            "geofence", "low_battery" -> R.color.amber to R.color.amberSoft
            "sos", "offline" -> R.color.red to R.color.redSoft
            else -> R.color.teal to R.color.tealSoft
        }
        holder.icon.setColorFilter(ContextCompat.getColor(ctx, color), PorterDuff.Mode.SRC_IN)
        (holder.icon.background.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(
            ContextCompat.getColor(ctx, bgColor)
        )

        val isUnread = alert.is_read == 0
        holder.dot.visibility = if (isUnread) android.view.View.VISIBLE else android.view.View.GONE
        holder.root.alpha = if (isUnread) 1f else 0.6f

        holder.root.setOnClickListener {
            if (isUnread) {
                onMarkRead(alert)
                items[position] = alert.copy(is_read = 1)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount() = items.size

    private fun fmtTime(iso: String): String = try {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(iso)
        SimpleDateFormat("d MMM, HH:mm", Locale("az")).format(date!!)
    } catch (e: Exception) { iso }
}
