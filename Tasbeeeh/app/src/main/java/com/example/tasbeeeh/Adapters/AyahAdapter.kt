package com.example.tasbeeeh.Adapters

import Ayah
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tasbeeeh.R

class AyahAdapter(private var ayahList: List<Ayah>) :
    RecyclerView.Adapter<AyahAdapter.AyahViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AyahViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ayah, parent, false)
        return AyahViewHolder(view)
    }

    override fun onBindViewHolder(holder: AyahViewHolder, position: Int) {
        val ayah = ayahList[position]
        holder.tvAyahNumber.text = ayah.numberInSurah.toString()
        holder.tvAyahText.text = ayah.text
    }

    override fun getItemCount(): Int {
        return ayahList.size
    }

    fun updateData(newList: List<Ayah>) {
        ayahList = newList
        notifyDataSetChanged()
    }

    class AyahViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAyahNumber: TextView = itemView.findViewById(R.id.tvAyahNumber)
        val tvAyahText: TextView = itemView.findViewById(R.id.tvAyahText)
    }
}
