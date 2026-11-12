package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.TasbeehDhikr
import com.example.data.model.TasbeehHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TasbeehRepository(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(context.getSharedPreferences("tasbeeh_prefs", Context.MODE_PRIVATE))

    companion object {
        val PRESET_DHIKRS: List<TasbeehDhikr> = listOf(
            TasbeehDhikr(
                id = "subhanallah",
                arabic = "سُبْحَانَ اللَّهِ",
                translit = "SubhanAllah",
                meaningUrdu = "اللہ کی ذات پاک اور ہر عیب سے مبرا ہے",
                meaningEn = "Glory be to Allah, free from all imperfections",
                meaningHindi = "अल्लाह पवित्र है और हर कमी से पाक है",
                defaultTarget = 33,
                virtueOrHadith = "صحیح مسلم: ہر نماز کے بعد 33 بار پڑھنے والے کے گناہ بخش دیے جاتے ہیں"
            ),
            TasbeehDhikr(
                id = "alhamdulillah",
                arabic = "الْحَمْدُ لِلَّهِ",
                translit = "Alhamdulillah",
                meaningUrdu = "تمام تعریفیں اور شکر صرف اللہ ہی کے لیے ہے",
                meaningEn = "All praise and gratitude belong to Allah alone",
                meaningHindi = "सभी प्रशंसाएँ और आभार केवल अल्लाह के लिए हैं",
                defaultTarget = 33,
                virtueOrHadith = "صحیح مسلم: الحمد للہ نیکیوں کے میزان کو بھر دیتا ہے"
            ),
            TasbeehDhikr(
                id = "allahuakbar",
                arabic = "اللَّهُ أَكْبَرُ",
                translit = "Allahu Akbar",
                meaningUrdu = "اللہ سب سے بڑا ہے اور کائنات پر غالب ہے",
                meaningEn = "Allah is the Greatest, sovereign over all creation",
                meaningHindi = "अल्लाह सबसे बड़ा है",
                defaultTarget = 34,
                virtueOrHadith = "تسبیحِ فاطمہؓ: رات سونے سے قبل 34 مرتبہ پڑھنا قلبی سکون کا باعث ہے"
            ),
            TasbeehDhikr(
                id = "astaghfirullah",
                arabic = "أَسْتَغْفِرُ اللَّهَ",
                translit = "Astaghfirullah",
                meaningUrdu = "میں اللہ سے اپنے تمام گناہوں کی بخشش مانگتا ہوں",
                meaningEn = "I seek forgiveness from Allah, the Most Merciful",
                meaningHindi = "मैं अल्लाह से अपने गुनाहों की माफ़ी मांगता हूँ",
                defaultTarget = 100,
                virtueOrHadith = "صحیح بخاری: رسول اللہ ﷺ روزانہ ستر سے زائد مرتبہ استغفار فرماتے تھے"
            ),
            TasbeehDhikr(
                id = "lailahaillallah",
                arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ",
                translit = "La ilaha illallah",
                meaningUrdu = "اللہ کے سوا کوئی معبود اور حاکمِ برحق نہیں",
                meaningEn = "There is no deity worthy of worship except Allah",
                meaningHindi = "अल्लाह के सिवा कोई पूज्य नहीं",
                defaultTarget = 100,
                virtueOrHadith = "جامع ترمذی: افضل ترین ذکر 'لا إله إلا الله' ہے"
            ),
            TasbeehDhikr(
                id = "durood_ibrahim",
                arabic = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
                translit = "Durood Ibrahim",
                meaningUrdu = "اے اللہ! درود و برکتیں نازل فرما محمد ﷺ پر اور آپ کی آل پر جیسا کہ تو نے ابراہیمؑ پر نازل فرمائیں، بے شک تو قابل تعریف ہے",
                meaningEn = "O Allah, send blessings upon Muhammad and the family of Muhammad, as You sent blessings upon Ibrahim and the family of Ibrahim. Indeed, You are Praiseworthy, Most Glorious.",
                meaningHindi = "ऐ अल्लाह! दुरूद व बरकत नाज़िल फ़रमा मुहम्मद ﷺ पर और उनकी आल पर जैसा कि तूने इब्राहीम (अलै.) पर नाज़िल फ़रमाई",
                defaultTarget = 100,
                virtueOrHadith = "صحیح مسلم: جو مجھ پر ایک بار درود بھیجے اللہ اس پر دس رحمتیں نازل فرماتا ہے"
            ),
            TasbeehDhikr(
                id = "subhanallah_bihamdihi",
                arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
                translit = "SubhanAllahi wa bihamdihi, SubhanAllahil Azeem",
                meaningUrdu = "اللہ اپنی حمد کے ساتھ پاک ہے، اللہ بڑی عظمت والا پاک ہے",
                meaningEn = "Glory be to Allah and His praise, Glory be to Allah the Almighty",
                meaningHindi = "अल्लाह अपनी प्रशंसा के साथ पवित्र है, महान अल्लाह पवित्र है",
                defaultTarget = 100,
                virtueOrHadith = "صحیح بخاری: دو کلمات زبان پر ہلکے اور میزان میں بہت بھاری ہیں"
            ),
            TasbeehDhikr(
                id = "la_hawla",
                arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
                translit = "La hawla wa la quwwata illa billah",
                meaningUrdu = "گناہوں سے بچنے اور نیکی کرنے کی طاقت صرف اللہ ہی کی طرف سے ہے",
                meaningEn = "There is no power and no strength except with Allah",
                meaningHindi = "बुराई से बचने और भलाई करने की शक्ति सिर्फ़ अल्लाह से है",
                defaultTarget = 100,
                virtueOrHadith = "صحیح بخاری: یہ کلمہ جنت کے خزانوں میں سے ایک نایاب خزانہ ہے"
            ),
            TasbeehDhikr(
                id = "hasbunallah",
                arabic = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                translit = "Hasbunallahu wa ni'mal wakeel",
                meaningUrdu = "ہمیں اللہ ہی کافی ہے اور وہ بہترین کارساز ہے",
                meaningEn = "Sufficient for us is Allah, and He is the best Disposer of affairs",
                meaningHindi = "हमारे लिए अल्लाह काफ़ी है और वह बेहतरीन कार्यसाधक है",
                defaultTarget = 100,
                virtueOrHadith = "صحیح بخاری: حضرت ابراہیمؑ نے آگ میں ڈالے جانے کے وقت یہی کلمہ پڑھا تھا"
            )
        )
    }

    private val _currentCount = MutableStateFlow(prefs.getInt("current_count", 0))
    val currentCount: StateFlow<Int> = _currentCount.asStateFlow()

    private val _currentTarget = MutableStateFlow(prefs.getInt("current_target", 33))
    val currentTarget: StateFlow<Int> = _currentTarget.asStateFlow()

    private val _currentRounds = MutableStateFlow(prefs.getInt("current_rounds", 0))
    val currentRounds: StateFlow<Int> = _currentRounds.asStateFlow()

    private val _selectedDhikrId = MutableStateFlow(prefs.getString("selected_dhikr_id", "subhanallah") ?: "subhanallah")
    val selectedDhikrId: StateFlow<String> = _selectedDhikrId.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(prefs.getBoolean("keep_screen_on", false))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _allTimeTotal = MutableStateFlow(prefs.getInt("all_time_total", 0))
    val allTimeTotal: StateFlow<Int> = _allTimeTotal.asStateFlow()

    private val _todayTotal = MutableStateFlow(0)
    val todayTotal: StateFlow<Int> = _todayTotal.asStateFlow()

    private val _todayCompletedTargets = MutableStateFlow(0)
    val todayCompletedTargets: StateFlow<Int> = _todayCompletedTargets.asStateFlow()

    private val _customDhikrs = MutableStateFlow<List<TasbeehDhikr>>(emptyList())
    val customDhikrs: StateFlow<List<TasbeehDhikr>> = _customDhikrs.asStateFlow()

    private val _history = MutableStateFlow<List<TasbeehHistoryItem>>(emptyList())
    val history: StateFlow<List<TasbeehHistoryItem>> = _history.asStateFlow()

    private val _dhikrBreakdown = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dhikrBreakdown: StateFlow<Map<String, Int>> = _dhikrBreakdown.asStateFlow()

    init {
        loadCustomDhikrs()
        loadHistory()
        checkAndRefreshDailyStats()
        loadBreakdown()
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun checkAndRefreshDailyStats() {
        val savedDate = prefs.getString("today_date_key", "") ?: ""
        val today = getTodayDateString()
        if (savedDate == today) {
            _todayTotal.value = prefs.getInt("today_total_count", 0)
            _todayCompletedTargets.value = prefs.getInt("today_completed_targets", 0)
        } else {
            // New day, reset daily stats
            _todayTotal.value = 0
            _todayCompletedTargets.value = 0
            prefs.edit()
                .putString("today_date_key", today)
                .putInt("today_total_count", 0)
                .putInt("today_completed_targets", 0)
                .apply()
        }
    }

    fun getAllDhikrs(): List<TasbeehDhikr> {
        return PRESET_DHIKRS + _customDhikrs.value
    }

    fun getSelectedDhikr(): TasbeehDhikr {
        val all = getAllDhikrs()
        return all.firstOrNull { it.id == _selectedDhikrId.value } ?: PRESET_DHIKRS[0]
    }

    fun selectDhikr(dhikr: TasbeehDhikr) {
        _selectedDhikrId.value = dhikr.id
        prefs.edit().putString("selected_dhikr_id", dhikr.id).apply()
        // If target is unchanged or user wants default:
        if (_currentTarget.value == 0 || _currentTarget.value == 33) {
            setTarget(dhikr.defaultTarget)
        }
    }

    fun increment(): Boolean {
        val nextCount = _currentCount.value + 1
        _currentCount.value = nextCount

        val nextToday = _todayTotal.value + 1
        _todayTotal.value = nextToday

        val nextAllTime = _allTimeTotal.value + 1
        _allTimeTotal.value = nextAllTime

        val target = _currentTarget.value
        var targetJustReached = false
        if (target > 0 && nextCount % target == 0) {
            targetJustReached = true
            val nextRounds = _currentRounds.value + 1
            _currentRounds.value = nextRounds
            val nextCompleted = _todayCompletedTargets.value + 1
            _todayCompletedTargets.value = nextCompleted
            prefs.edit()
                .putInt("current_rounds", nextRounds)
                .putInt("today_completed_targets", nextCompleted)
                .apply()
        }

        // Update zikr-wise count
        val activeDhikr = getSelectedDhikr()
        val currentBreakdown = _dhikrBreakdown.value.toMutableMap()
        val prev = currentBreakdown[activeDhikr.translit] ?: 0
        currentBreakdown[activeDhikr.translit] = prev + 1
        _dhikrBreakdown.value = currentBreakdown
        saveBreakdown(currentBreakdown)

        prefs.edit()
            .putInt("current_count", nextCount)
            .putInt("today_total_count", nextToday)
            .putInt("all_time_total", nextAllTime)
            .apply()

        return targetJustReached
    }

    fun undo() {
        if (_currentCount.value <= 0) return
        val nextCount = _currentCount.value - 1
        _currentCount.value = nextCount

        val nextToday = (_todayTotal.value - 1).coerceAtLeast(0)
        _todayTotal.value = nextToday

        val nextAllTime = (_allTimeTotal.value - 1).coerceAtLeast(0)
        _allTimeTotal.value = nextAllTime

        val activeDhikr = getSelectedDhikr()
        val currentBreakdown = _dhikrBreakdown.value.toMutableMap()
        val prev = currentBreakdown[activeDhikr.translit] ?: 0
        if (prev > 0) {
            currentBreakdown[activeDhikr.translit] = prev - 1
            _dhikrBreakdown.value = currentBreakdown
            saveBreakdown(currentBreakdown)
        }

        prefs.edit()
            .putInt("current_count", nextCount)
            .putInt("today_total_count", nextToday)
            .putInt("all_time_total", nextAllTime)
            .apply()
    }

    fun reset() {
        _currentCount.value = 0
        _currentRounds.value = 0
        prefs.edit()
            .putInt("current_count", 0)
            .putInt("current_rounds", 0)
            .apply()
    }

    fun setTarget(target: Int) {
        if (target <= 0) return
        _currentTarget.value = target
        prefs.edit().putInt("current_target", target).apply()
    }

    fun nextRound() {
        val nextRounds = _currentRounds.value + 1
        _currentRounds.value = nextRounds
        prefs.edit().putInt("current_rounds", nextRounds).apply()
    }

    fun toggleSound(): Boolean {
        val next = !_soundEnabled.value
        _soundEnabled.value = next
        prefs.edit().putBoolean("sound_enabled", next).apply()
        return next
    }

    fun toggleVibration(): Boolean {
        val next = !_vibrationEnabled.value
        _vibrationEnabled.value = next
        prefs.edit().putBoolean("vibration_enabled", next).apply()
        return next
    }

    fun toggleKeepScreenOn(): Boolean {
        val next = !_keepScreenOn.value
        _keepScreenOn.value = next
        prefs.edit().putBoolean("keep_screen_on", next).apply()
        return next
    }

    fun addCustomDhikr(arabic: String, translit: String, meaning: String, defaultTarget: Int = 33): TasbeehDhikr {
        val newDhikr = TasbeehDhikr(
            id = "custom_${UUID.randomUUID()}",
            arabic = arabic.trim(),
            translit = translit.trim(),
            meaningUrdu = meaning.trim(),
            meaningEn = meaning.trim(),
            meaningHindi = meaning.trim(),
            defaultTarget = defaultTarget,
            isCustom = true
        )
        val updated = _customDhikrs.value + newDhikr
        _customDhikrs.value = updated
        saveCustomDhikrs(updated)
        selectDhikr(newDhikr)
        return newDhikr
    }

    fun saveHistorySession(isCompleted: Boolean = false): TasbeehHistoryItem {
        val active = getSelectedDhikr()
        val item = TasbeehHistoryItem(
            id = UUID.randomUUID().toString(),
            dhikrId = active.id,
            dhikrArabic = active.arabic,
            dhikrTitle = active.translit,
            count = _currentCount.value,
            target = _currentTarget.value,
            timestamp = System.currentTimeMillis(),
            isTargetCompleted = isCompleted || (_currentTarget.value > 0 && _currentCount.value >= _currentTarget.value)
        )
        val updated = listOf(item) + _history.value
        _history.value = updated
        saveHistory(updated)
        return item
    }

    fun deleteHistoryItem(id: String) {
        val updated = _history.value.filterNot { it.id == id }
        _history.value = updated
        saveHistory(updated)
    }

    fun clearAllHistory() {
        _history.value = emptyList()
        saveHistory(emptyList())
    }

    private fun loadCustomDhikrs() {
        try {
            val json = prefs.getString("custom_dhikrs_json", "[]") ?: "[]"
            val array = JSONArray(json)
            val list = mutableListOf<TasbeehDhikr>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TasbeehDhikr(
                        id = obj.optString("id"),
                        arabic = obj.optString("arabic"),
                        translit = obj.optString("translit"),
                        meaningUrdu = obj.optString("meaningUrdu"),
                        meaningEn = obj.optString("meaningEn"),
                        meaningHindi = obj.optString("meaningHindi"),
                        defaultTarget = obj.optInt("defaultTarget", 33),
                        isCustom = true
                    )
                )
            }
            _customDhikrs.value = list
        } catch (_: Exception) {}
    }

    private fun saveCustomDhikrs(list: List<TasbeehDhikr>) {
        try {
            val array = JSONArray()
            for (d in list) {
                val obj = JSONObject().apply {
                    put("id", d.id)
                    put("arabic", d.arabic)
                    put("translit", d.translit)
                    put("meaningUrdu", d.meaningUrdu)
                    put("meaningEn", d.meaningEn)
                    put("meaningHindi", d.meaningHindi)
                    put("defaultTarget", d.defaultTarget)
                }
                array.put(obj)
            }
            prefs.edit().putString("custom_dhikrs_json", array.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadHistory() {
        try {
            val json = prefs.getString("history_json", "[]") ?: "[]"
            val array = JSONArray(json)
            val list = mutableListOf<TasbeehHistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TasbeehHistoryItem(
                        id = obj.optString("id"),
                        dhikrId = obj.optString("dhikrId"),
                        dhikrArabic = obj.optString("dhikrArabic"),
                        dhikrTitle = obj.optString("dhikrTitle"),
                        count = obj.optInt("count"),
                        target = obj.optInt("target"),
                        timestamp = obj.optLong("timestamp"),
                        isTargetCompleted = obj.optBoolean("isTargetCompleted")
                    )
                )
            }
            _history.value = list
        } catch (_: Exception) {}
    }

    private fun saveHistory(list: List<TasbeehHistoryItem>) {
        try {
            val array = JSONArray()
            // Limit to last 200 history records
            for (item in list.take(200)) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("dhikrId", item.dhikrId)
                    put("dhikrArabic", item.dhikrArabic)
                    put("dhikrTitle", item.dhikrTitle)
                    put("count", item.count)
                    put("target", item.target)
                    put("timestamp", item.timestamp)
                    put("isTargetCompleted", item.isTargetCompleted)
                }
                array.put(obj)
            }
            prefs.edit().putString("history_json", array.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadBreakdown() {
        try {
            val json = prefs.getString("breakdown_json", "{}") ?: "{}"
            val obj = JSONObject(json)
            val map = mutableMapOf<String, Int>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.optInt(k, 0)
            }
            _dhikrBreakdown.value = map
        } catch (_: Exception) {}
    }

    private fun saveBreakdown(map: Map<String, Int>) {
        try {
            val obj = JSONObject()
            for ((k, v) in map) {
                obj.put(k, v)
            }
            prefs.edit().putString("breakdown_json", obj.toString()).apply()
        } catch (_: Exception) {}
    }
}
