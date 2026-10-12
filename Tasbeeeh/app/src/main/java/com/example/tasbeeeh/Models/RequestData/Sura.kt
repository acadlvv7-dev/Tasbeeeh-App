package com.example.tasbeeeh.Models.RequestData

data class Sura(
    val number: Int,
    val name: String,
    val englishName: String,
    val revelationType: String,
    val ayahsCount: Int = 0
)