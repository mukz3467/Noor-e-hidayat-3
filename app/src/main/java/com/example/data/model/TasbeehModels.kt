package com.example.data.model

data class TasbeehDhikr(
    val id: String,
    val arabic: String,
    val translit: String,
    val meaningUrdu: String,
    val meaningEn: String,
    val meaningHindi: String,
    val defaultTarget: Int = 33,
    val isCustom: Boolean = false,
    val virtueOrHadith: String = ""
)

data class TasbeehHistoryItem(
    val id: String,
    val dhikrId: String,
    val dhikrArabic: String,
    val dhikrTitle: String,
    val count: Int,
    val target: Int,
    val timestamp: Long,
    val isTargetCompleted: Boolean
)
