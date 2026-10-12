package com.example.tasbeeeh.Models

data class SurahTextList(
    val surahs: List<SurahTextEntry>
)

data class SurahTextEntry(
    val surah_number: Int,
    val text_api_url: String
)

data class SurahAudioList(
    val surahs: List<SurahAudioEntry>
)

data class SurahAudioEntry(
    val surah_number: Int,
    val audio_url: String
)