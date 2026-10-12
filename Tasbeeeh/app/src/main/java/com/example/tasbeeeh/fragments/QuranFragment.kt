package com.example.tasbeeeh.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tasbeeeh.Adapters.QuranAdapter
import com.example.tasbeeeh.R
import com.example.tasbeeeh.Utils.SurahHelper
import com.example.tasbeeeh.Models.RequestData.Sura

class QuranFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var quranAdapter: QuranAdapter
    private lateinit var etSearch: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quran, container, false)

        recyclerView = view.findViewById(R.id.recycle_view_quran)
        progressBar = view.findViewById(R.id.progress_bar_quran)
        etSearch = view.findViewById(R.id.et_search_quran)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        quranAdapter = QuranAdapter(emptyList()) { sura ->
            val bundle = Bundle().apply {
                putInt("suraNumber", sura.number)
                putString("suraName", sura.name)
                putString("suraEnglishName", sura.englishName)
            }
            findNavController().navigate(
                R.id.action_quranFragment_to_quranTextFragment,
                bundle
            )
        }

        recyclerView.adapter = quranAdapter

        setupSearch()
        loadQuranData()

        return view
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                quranAdapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadQuranData() {
        progressBar.visibility = View.VISIBLE
        
        val list = SurahHelper.getSurahList()
        quranAdapter.updateData(list)
        
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}