package com.ailenezareti.panelapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ailenezareti.panelapp.R
import com.ailenezareti.panelapp.model.Child

class ChildChipAdapter(
    private val onSelect: (Child) -> Unit
) : RecyclerView.Adapter<ChildChipAdapter.VH>() {

    private var items: List<Child> = emptyList()
    private var activeId: Int = -1

    fun submit(children: List<Child>, activeChildId: Int) {
        items = children
        activeId = activeChildId
        notifyDataSetChanged()
    }

    fun setActive(id: Int) {
        activeId = id
        notifyDataSetChanged()
    }

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val root: android.view.View = view.findViewById(R.id.chipRoot)
        val avatar: android.widget.TextView = view.findViewById(R.id.chipAvatar)
        val name: android.widget.TextView = view.findViewById(R.id.chipName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_child_chip, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val child = items[position]
        holder.name.text = child.name
        holder.avatar.text = child.name.take(1).uppercase()
        try {
            (holder.avatar.background as? android.graphics.drawable.GradientDrawable)?.setColor(
                Color.parseColor(child.avatar_color ?: "#2E6F6B")
            )
        } catch (e: Exception) { /* rəng düzgün deyilsə default qalır */ }

        holder.root.setBackgroundResource(
            if (child.id == activeId) R.drawable.bg_chip_active else R.drawable.bg_chip
        )
        holder.root.setOnClickListener { onSelect(child) }
    }

    override fun getItemCount() = items.size
}
