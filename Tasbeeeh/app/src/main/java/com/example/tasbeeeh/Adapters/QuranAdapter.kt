package com.example.tasbeeeh.Adapters

import com.example.tasbeeeh.Models.RequestData.Sura
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tasbeeeh.R

class QuranAdapter(private var quranList: List<Sura>, private val onItemClick:(Sura) -> Unit):
    RecyclerView.Adapter<QuranAdapter.QuranViewHolder>(){

    private var fullList: List<Sura> = quranList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuranViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quran, parent, false)
        return QuranViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuranViewHolder,position:Int) {
        val quran: Sura =quranList[position]
        holder.quranTitleArabic.text=quran.name
        holder.quranTitleEnglish.text=quran.englishName
        holder.countNumber.text=quran.number.toString()
        holder.tvAyatCount.text = "${quran.ayahsCount} oyat"
        holder.itemView.setOnClickListener {
            onItemClick(quran)
        }
    }

    override fun getItemCount(): Int {
        return quranList.size
    }

    fun updateData(newList: List<Sura>) {
        quranList = newList
        fullList = newList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        quranList = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter {
                it.englishName.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    class QuranViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val quranTitleEnglish: TextView =itemView.findViewById(R.id.tvName)
        val quranTitleArabic: TextView =itemView.findViewById(R.id.tvArabic)
        val countNumber: TextView =itemView.findViewById(R.id.tvNumber)
        val tvAyatCount: TextView = itemView.findViewById(R.id.tvAyatCount)
    }
}