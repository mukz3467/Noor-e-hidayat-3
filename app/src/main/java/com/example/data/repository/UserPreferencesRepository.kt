package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.AppTheme
import com.example.data.model.BookmarkItem
import com.example.data.model.BookmarkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("noor_e_hidayat_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(
        AppLanguage.values().firstOrNull { it.code == prefs.getString("app_language", "en") } ?: AppLanguage.ENGLISH
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _theme = MutableStateFlow(
        AppTheme.values().firstOrNull { it.id == prefs.getString("app_theme", "emerald_gold") } ?: AppTheme.EMERALD_GOLD
    )
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    private val _arabicFontSize = MutableStateFlow(prefs.getFloat("arabic_font_size", 24f))
    val arabicFontSize: StateFlow<Float> = _arabicFontSize.asStateFlow()

    private val _selectedReciterId = MutableStateFlow(prefs.getString("selected_reciter_id", "mishary") ?: "mishary")
    val selectedReciterId: StateFlow<String> = _selectedReciterId.asStateFlow()

    private val _tasbeehCount = MutableStateFlow(prefs.getInt("tasbeeh_count", 0))
    val tasbeehCount: StateFlow<Int> = _tasbeehCount.asStateFlow()

    private val _tasbeehTarget = MutableStateFlow(prefs.getInt("tasbeeh_target", 33))
    val tasbeehTarget: StateFlow<Int> = _tasbeehTarget.asStateFlow()

    private val _bookmarks = MutableStateFlow(
        listOf(
            BookmarkItem("bm_1", BookmarkType.QURAN, "Surah Al-Fatihah, Ayah 1", "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", surahNumber = 1, ayahNumber = 1),
            BookmarkItem("bm_2", BookmarkType.HADITH, "Sahih al-Bukhari, Hadith 1", "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ", hadithId = "bukhari_1"),
            BookmarkItem("bm_3", BookmarkType.DUA, "Sayyid al-Istighfar", "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ")
        )
    )
    val bookmarks: StateFlow<List<BookmarkItem>> = _bookmarks.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setLanguage(newLanguage: AppLanguage) {
        _language.value = newLanguage
        prefs.edit().putString("app_language", newLanguage.code).apply()
    }

    fun setTheme(newTheme: AppTheme) {
        _theme.value = newTheme
        prefs.edit().putString("app_theme", newTheme.id).apply()
    }

    fun setArabicFontSize(size: Float) {
        _arabicFontSize.value = size
        prefs.edit().putFloat("arabic_font_size", size).apply()
    }

    fun setReciter(reciterId: String) {
        _selectedReciterId.value = reciterId
        prefs.edit().putString("selected_reciter_id", reciterId).apply()
    }

    fun incrementTasbeeh() {
        val next = _tasbeehCount.value + 1
        _tasbeehCount.value = next
        prefs.edit().putInt("tasbeeh_count", next).apply()
    }

    fun resetTasbeeh() {
        _tasbeehCount.value = 0
        prefs.edit().putInt("tasbeeh_count", 0).apply()
    }

    fun setTasbeehTarget(target: Int) {
        _tasbeehTarget.value = target
        prefs.edit().putInt("tasbeeh_target", target).apply()
    }

    fun toggleBookmark(item: BookmarkItem) {
        val current = _bookmarks.value.toMutableList()
        val exists = current.any { it.id == item.id || (it.surahNumber == item.surahNumber && it.ayahNumber == item.ayahNumber && item.surahNumber != null) }
        if (exists) {
            current.removeAll { it.id == item.id || (it.surahNumber == item.surahNumber && it.ayahNumber == item.ayahNumber && item.surahNumber != null) }
        } else {
            current.add(0, item)
        }
        _bookmarks.value = current
    }

    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean {
        return _bookmarks.value.any { it.surahNumber == surahNumber && it.ayahNumber == ayahNumber }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    private val _favoriteDuaIds = MutableStateFlow<Set<String>>(
        prefs.getStringSet("favorite_dua_ids", setOf("dua_sayyidul_istighfar", "dua_rabbana_atina_dunya")) ?: setOf("dua_sayyidul_istighfar", "dua_rabbana_atina_dunya")
    )
    val favoriteDuaIds: StateFlow<Set<String>> = _favoriteDuaIds.asStateFlow()

    private val _recentViewedDuaIds = MutableStateFlow<List<String>>(
        prefs.getString("recent_viewed_dua_ids", "dua_sayyidul_istighfar,dua_rabbana_atina_dunya")
            ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    )
    val recentViewedDuaIds: StateFlow<List<String>> = _recentViewedDuaIds.asStateFlow()

    private val _recentPlayedDuaIds = MutableStateFlow<List<String>>(
        prefs.getString("recent_played_dua_ids", "")
            ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    )
    val recentPlayedDuaIds: StateFlow<List<String>> = _recentPlayedDuaIds.asStateFlow()

    fun toggleFavoriteDua(id: String): Boolean {
        val current = _favoriteDuaIds.value.toMutableSet()
        val isAdded: Boolean
        if (current.contains(id)) {
            current.remove(id)
            isAdded = false
        } else {
            current.add(id)
            isAdded = true
        }
        _favoriteDuaIds.value = current
        prefs.edit().putStringSet("favorite_dua_ids", current).apply()
        return isAdded
    }

    fun isFavoriteDua(id: String): Boolean {
        return _favoriteDuaIds.value.contains(id)
    }

    fun recordViewedDua(id: String) {
        val list = _recentViewedDuaIds.value.toMutableList()
        list.remove(id)
        list.add(0, id)
        val trimmed = list.take(20)
        _recentViewedDuaIds.value = trimmed
        prefs.edit().putString("recent_viewed_dua_ids", trimmed.joinToString(",")).apply()
    }

    fun recordPlayedDua(id: String) {
        val list = _recentPlayedDuaIds.value.toMutableList()
        list.remove(id)
        list.add(0, id)
        val trimmed = list.take(20)
        _recentPlayedDuaIds.value = trimmed
        prefs.edit().putString("recent_played_dua_ids", trimmed.joinToString(",")).apply()
    }
}
