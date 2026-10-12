package com.example.tasbeeeh.Utils

import android.content.Context
import com.example.tasbeeeh.Models.*
import com.google.gson.Gson

object AssetJsonReader {
    private var cachedTextList: SurahTextList? = null
    private var cachedAudioList: SurahAudioList? = null

    fun getSurahTextEntry(context: Context, surahNumber: Int): SurahTextEntry? {
        if (cachedTextList == null) {
            val json = context.assets.open("quran_text_api.json").bufferedReader().use { it.readText() }
            cachedTextList = Gson().fromJson(json, SurahTextList::class.java)
        }
        return cachedTextList?.surahs?.find { it.surah_number == surahNumber }
    }

    fun getSurahAudioEntry(context: Context, surahNumber: Int): SurahAudioEntry? {
        if (cachedAudioList == null) {
            val json = context.assets.open("quran_audio_api.json").bufferedReader().use { it.readText() }
            cachedAudioList = Gson().fromJson(json, SurahAudioList::class.java)
        }
        return cachedAudioList?.surahs?.find { it.surah_number == surahNumber }
    }
}