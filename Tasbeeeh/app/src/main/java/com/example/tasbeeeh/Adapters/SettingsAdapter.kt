package com.example.tasbeeeh.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tasbeeeh.R
import com.example.tasbeeeh.Models.SettingsModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsAdapter(private val settingList: List<SettingsModel>, private val onItemClick: (SettingsModel) -> Unit) :
    RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: MaterialCardView = itemView.findViewById(R.id.card_view_settings)
        val imageSett: ImageView = itemView.findViewById(R.id.image_settings)
        val textSett: TextView = itemView.findViewById(R.id.text_settings)
        val switch: MaterialSwitch = itemView.findViewById(R.id.switch_settings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_settings, parent, false)
        return SettingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        val settings = settingList[position]
        holder.textSett.text = settings.textName
        holder.imageSett.setImageResource(settings.imageSettings)
        
        val prefs = holder.itemView.context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)

        if (settings.hasSwitch) {
            holder.switch.visibility = View.VISIBLE
            holder.switch.isChecked = prefs.getBoolean(settings.textName, true)
            holder.switch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean(settings.textName, isChecked).apply()
            }
        } else {
            holder.switch.visibility = View.GONE
        }

        holder.cardView.setOnClickListener {
            onItemClick(settings)
        }
    }

    override fun getItemCount(): Int {
        return settingList.size
    }
}