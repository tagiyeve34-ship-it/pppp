package com.ailenezareti.panelapp.ui

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ailenezareti.panelapp.R
import com.ailenezareti.panelapp.model.CallEntry
import java.text.SimpleDateFormat
import java.util.*

class CallsAdapter(private val items: List<CallEntry>) : RecyclerView.Adapter<CallsAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.callIcon)
        val name: TextView = view.findViewById(R.id.callName)
        val number: TextView = view.findViewById(R.id.callNumber)
        val meta: TextView = view.findViewById(R.id.callMeta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_call, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val call = items[position]
        val ctx = holder.itemView.context
        holder.name.text = call.contact_name ?: "Naməlum nömrə"
        holder.number.text = call.phone_number

        val typeLabel = when (call.call_type) {
            "outgoing" -> "Gedən"
            "missed" -> "Buraxılmış"
            else -> "Gələn"
        }
        val duration = if (call.duration_sec > 0) {
            String.format("%d:%02d", call.duration_sec / 60, call.duration_sec % 60)
        } else "—"
        holder.meta.text = "$typeLabel · $duration\n${fmtTime(call.occurred_at)}"

        val isMissed = call.call_type == "missed"
        val color = if (isMissed) R.color.red else R.color.teal
        val bgColor = if (isMissed) R.color.redSoft else R.color.tealSoft
        holder.icon.setColorFilter(ContextCompat.getColor(ctx, color), PorterDuff.Mode.SRC_IN)
        (holder.icon.background.mutate() as? android.graphics.drawable.GradientDrawable)?.setColor(
            ContextCompat.getColor(ctx, bgColor)
        )
    }

    override fun getItemCount() = items.size

    private fun fmtTime(iso: String): String = try {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(iso)
        SimpleDateFormat("d MMM, HH:mm", Locale("az")).format(date!!)
    } catch (e: Exception) { iso }
}
