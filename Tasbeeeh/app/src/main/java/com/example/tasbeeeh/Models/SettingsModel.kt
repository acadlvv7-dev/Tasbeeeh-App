package com.example.tasbeeeh.Models

data class SettingsModel (
    val imageSettings: Int,
    val textName: String,
    val subTitle: String = "",
    val hasSwitch: Boolean = false
)