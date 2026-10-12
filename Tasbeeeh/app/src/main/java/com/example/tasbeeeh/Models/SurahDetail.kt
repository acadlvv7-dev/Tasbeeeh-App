package com.example.tasbeeeh.Models

import Ayah

data class SurahDetail(
    val number: Int,
    val name: String,
    val englishName: String,
    val ayahs: List<Ayah>
)
