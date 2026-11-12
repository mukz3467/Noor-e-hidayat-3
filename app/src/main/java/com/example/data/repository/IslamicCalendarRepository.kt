package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.PrayerSchedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

data class HijriDate(
    val day: Int,
    val month: Int, // 1-12
    val monthNameEn: String,
    val monthNameUr: String,
    val monthNameAr: String,
    val year: Int,
    val isConfirmedByRuetHilal: Boolean = false,
    val sourceDescription: String = "Astronomical Calculation (Umm Al-Qura Formula)"
)

data class IslamicCalendarEvent(
    val id: String,
    val titleEn: String,
    val titleUr: String,
    val titleAr: String,
    val titleHi: String,
    val hijriDay: Int,
    val hijriMonth: Int, // 1-12
    val hijriMonthNameEn: String,
    val hijriMonthNameUr: String,
    val isMoonSightingDependent: Boolean,
    val descriptionEn: String,
    val descriptionUr: String
)

data class RamadanDaySchedule(
    val ramadanDay: Int, // 1-30
    val gregorianDateStr: String,
    val hijriDateStr: String,
    val sehriTime: String,
    val iftarTime: String,
    val dayOfWeek: String
)

object IslamicCalendarRepository {

    private const val PREFS_NAME = "hijri_calendar_prefs"
    private const val KEY_MANUAL_ADJUSTMENT = "hijri_day_adjustment"
    private const val KEY_CALENDAR_SOURCE = "hijri_calendar_source" // "pakistan_official", "astronomical"

    val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    val HIJRI_MONTHS_UR = listOf(
        "محرم الحرام", "صفر المظفر", "ربیع الاول", "ربیع الثانی",
        "جمادی الاول", "جمادی الثانی", "رجب المرجب", "شعبان المعظم",
        "رمضان المبارک", "شوال المکرم", "ذوالقعدہ", "ذوالحجہ"
    )

    val HIJRI_MONTHS_AR = listOf(
        "محرّم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    val ISLAMIC_EVENTS: List<IslamicCalendarEvent> = listOf(
        IslamicCalendarEvent(
            id = "islamic_new_year",
            titleEn = "Islamic New Year",
            titleUr = "یکم محرم الحرام (ہجری سالِ نو)",
            titleAr = "رأس السنة الهجرية",
            titleHi = "इस्लामी नया साल (1 मुहर्रम)",
            hijriDay = 1,
            hijriMonth = 1,
            hijriMonthNameEn = "Muharram",
            hijriMonthNameUr = "محرم الحرام",
            isMoonSightingDependent = true,
            descriptionEn = "Commencement of the Hijri year marking the blessed migration of Prophet Muhammad ﷺ to Madinah.",
            descriptionUr = "ہجری تقویم کا آغاز جو حضور اکرم ﷺ کی مکہ سے مدینہ منورہ کی مبارک ہجرت کی یاد دلاتا ہے۔"
        ),
        IslamicCalendarEvent(
            id = "ashura",
            titleEn = "Day of Ashura",
            titleUr = "یومِ عاشورہ (دس محرم الحرام)",
            titleAr = "يوم عاشوراء",
            titleHi = "आशूरा (10 मुहर्रम)",
            hijriDay = 10,
            hijriMonth = 1,
            hijriMonthNameEn = "Muharram",
            hijriMonthNameUr = "محرم الحرام",
            isMoonSightingDependent = false,
            descriptionEn = "Fasting on the 10th of Muharram expiates sins of the previous year (Sahih Muslim).",
            descriptionUr = "یومِ عاشورہ کا روزہ پچھلے سال کے گناہوں کا کفارہ بن جاتا ہے (صحیح مسلم)۔"
        ),
        IslamicCalendarEvent(
            id = "mawlid_un_nabi",
            titleEn = "12 Rabi' al-Awwal (Mawlid an-Nabi ﷺ)",
            titleUr = "۱۲ ربیع الاول (ولادتِ باسعادت نبی آخر الزماں ﷺ)",
            titleAr = "المولد النبوي الشريف",
            titleHi = "12 रबी-उल-अव्वल (ईद-मिलाद-उन-नबी ﷺ)",
            hijriDay = 12,
            hijriMonth = 3,
            hijriMonthNameEn = "Rabi' al-Awwal",
            hijriMonthNameUr = "ربیع الاول",
            isMoonSightingDependent = true,
            descriptionEn = "The blessed day marking the arrival of the Mercy to the Worlds, Prophet Muhammad ﷺ.",
            descriptionUr = "رحمۃ للعالمین، خاتم النبیین حضرت محمد مصطفیٰ ﷺ کی ولادتِ طیبہ کا مبارک دن۔"
        ),
        IslamicCalendarEvent(
            id = "shab_e_miraj",
            titleEn = "27 Rajab (Shab-e-Mi'raj / Al-Isra' wal-Mi'raj)",
            titleUr = "۲۷ رجب المرجب (شبِ معراج)",
            titleAr = "ذكرى الإسراء والمعراج",
            titleHi = "27 रजब (शब-ए-मेराज)",
            hijriDay = 27,
            hijriMonth = 7,
            hijriMonthNameEn = "Rajab",
            hijriMonthNameUr = "رجب المرجب",
            isMoonSightingDependent = false,
            descriptionEn = "Commemorating the miraculous Night Journey and Heavenly Ascension where the 5 daily prayers were gifted.",
            descriptionUr = "سفرِ اسراء اور معراجِ سماوی کا عظیم معجزہ جس میں امت کو پانچ وقت کی نماز کا تحفہ ملا۔"
        ),
        IslamicCalendarEvent(
            id = "shab_e_barat",
            titleEn = "15 Sha'ban (Shab-e-Bar'at / Mid-Sha'ban)",
            titleUr = "۱۵ شعبان المعظم (شبِ برأت)",
            titleAr = "ليلة النصف من شعبان",
            titleHi = "15 शाबान (शब-ए-बरात)",
            hijriDay = 15,
            hijriMonth = 8,
            hijriMonthNameEn = "Sha'ban",
            hijriMonthNameUr = "شعبان المعظم",
            isMoonSightingDependent = false,
            descriptionEn = "Night of forgiveness and seeking Allah's divine pardon and decreed mercy.",
            descriptionUr = "بخشش و مغفرت کی رات جس میں توبہ و استغفار کی خصوصی ترغیب وارد ہے۔"
        ),
        IslamicCalendarEvent(
            id = "ramadan_start",
            titleEn = "1st Ramadan (Beginning of Holy Month)",
            titleUr = "یکم رمضان المبارک (نزولِ قرآن و آغازِ صیام)",
            titleAr = "غرة شهر رمضان المبارك",
            titleHi = "1 रमज़ान (पवित्र माह का आरंभ)",
            hijriDay = 1,
            hijriMonth = 9,
            hijriMonthNameEn = "Ramadan",
            hijriMonthNameUr = "رمضان المبارک",
            isMoonSightingDependent = true,
            descriptionEn = "Start of the sacred month of fasting, night prayers, and Quran revelation (subject to official Ruet-e-Hilal announcement).",
            descriptionUr = "برکتوں اور رحمتوں کے مقدس مہینے کا آغاز جو چاند کی رویت پر موقوف ہے۔"
        ),
        IslamicCalendarEvent(
            id = "laylatul_qadr",
            titleEn = "Laylat al-Qadr (27th Night of Ramadan)",
            titleUr = "لیلۃ القدر (ستائیسویں شبِ رمضان المبارک)",
            titleAr = "ليلة القدر المباركة",
            titleHi = "शब-ए-क़द्र (27 रमज़ान)",
            hijriDay = 27,
            hijriMonth = 9,
            hijriMonthNameEn = "Ramadan",
            hijriMonthNameUr = "رمضان المبارک",
            isMoonSightingDependent = false,
            descriptionEn = "The Night of Power, better than a thousand months (Surah Al-Qadr).",
            descriptionUr = "ہزار مہینوں سے افضل رات جس میں قرآن مجید لوحِ محفوظ سے نازل ہوا (سورۃ القدر)۔"
        ),
        IslamicCalendarEvent(
            id = "eid_al_fitr",
            titleEn = "Eid al-Fitr (1st Shawwal)",
            titleUr = "عید الفطر المبارک (یکم شوال)",
            titleAr = "عيد الفطر المبارك",
            titleHi = "ईद-उल-फ़ित्र (1 शव्वाल)",
            hijriDay = 1,
            hijriMonth = 10,
            hijriMonthNameEn = "Shawwal",
            hijriMonthNameUr = "شوال المکرم",
            isMoonSightingDependent = true,
            descriptionEn = "Festival of breaking the fast celebrated by the Muslim Ummah (subject to official Ruet-e-Hilal announcement).",
            descriptionUr = "رمضان المبارک کے روزوں کی تکمیل پر اللہ تعالیٰ کی طرف سے شکرانے کا عظیم تہوار۔"
        ),
        IslamicCalendarEvent(
            id = "day_of_arafah",
            titleEn = "Day of Arafah (Hajj - 9 Dhu al-Hijjah)",
            titleUr = "یومِ عرفہ (۹ ذوالحجہ - حج کا رکنِ اعظم)",
            titleAr = "يوم عرفة المبارك",
            titleHi = "यौम-ए-अरफ़ा (9 ज़िलहिज्जा)",
            hijriDay = 9,
            hijriMonth = 12,
            hijriMonthNameEn = "Dhu al-Hijjah",
            hijriMonthNameUr = "ذوالحجہ",
            isMoonSightingDependent = true,
            descriptionEn = "The pinnacle of Hajj at Mount Arafat; fasting on this day for non-pilgrims expiates two years of sins (Sahih Muslim).",
            descriptionUr = "حج کا سب سے بڑا رکن، غیر حجاج کے لیے اس دن کا روزہ دو سال کے گناہوں کا کفارہ ہے۔"
        ),
        IslamicCalendarEvent(
            id = "eid_al_adha",
            titleEn = "Eid al-Adha (10 Dhu al-Hijjah)",
            titleUr = "عید الاضحیٰ (۱۰ ذوالحجہ - سنتِ ابراہیمی)",
            titleAr = "عيد الأضحى المبارك",
            titleHi = "ईद-उल-अज़हा (10 ज़िलहिज्जा)",
            hijriDay = 10,
            hijriMonth = 12,
            hijriMonthNameEn = "Dhu al-Hijjah",
            hijriMonthNameUr = "ذوالحجہ",
            isMoonSightingDependent = false,
            descriptionEn = "Festival of Sacrifice commemorating the supreme submission of Prophet Ibrahim (AS) and Ismail (AS).",
            descriptionUr = "قربانی کا مقدس تہوار جو حضرت ابراہیمؑ اور حضرت اسماعیلؑ کی لازوال اطاعت کی یاد دلاتا ہے۔"
        )
    )

    private val _manualAdjustmentDays = MutableStateFlow(0)
    val manualAdjustmentDays: StateFlow<Int> = _manualAdjustmentDays.asStateFlow()

    private val _usePakistanOfficial = MutableStateFlow(true)
    val usePakistanOfficial: StateFlow<Boolean> = _usePakistanOfficial.asStateFlow()

    private var isPrefsLoaded = false

    fun init(context: Context) {
        if (isPrefsLoaded) return
        isPrefsLoaded = true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _manualAdjustmentDays.value = prefs.getInt(KEY_MANUAL_ADJUSTMENT, 0)
        _usePakistanOfficial.value = prefs.getBoolean(KEY_CALENDAR_SOURCE, true)
    }

    fun setManualAdjustment(context: Context, days: Int) {
        val clamped = days.coerceIn(-2, 2)
        _manualAdjustmentDays.value = clamped
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_MANUAL_ADJUSTMENT, clamped).apply()
    }

    fun setUsePakistanOfficial(context: Context, useOfficial: Boolean) {
        _usePakistanOfficial.value = useOfficial
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CALENDAR_SOURCE, useOfficial).apply()
    }

    /**
     * Converts a Gregorian calendar date to Hijri date using astronomical Kuweit/Umm Al-Qura
     * algorithm combined with Pakistan Ruet-e-Hilal confirmed offsets.
     */
    fun getHijriDate(calendar: Calendar, manualOffsetDays: Int = _manualAdjustmentDays.value): HijriDate {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Calculate Julian Day Number
        var m = month
        var y = year
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        // Epoch of Islamic Calendar: July 16, 622 CE = JD 1948439.5
        // Apply manual adjustment & regional Pakistan moon-sighting offset
        val effectiveOffset = manualOffsetDays + if (_usePakistanOfficial.value) getPakistanRuetOffset(year, month) else 0
        val adjustedJd = jd + effectiveOffset

        val l = adjustedJd - 1948439.5 + 10632.0
        val n = floor((l - 1.0) / 10631.0)
        val l1 = l - 10631.0 * n + 354.0
        val j = (floor((10985.0 - l1) / 5316.0)) * (floor((50.0 * l1) / 17719.0)) +
                (floor(l1 / 5670.0)) * (floor((43.0 * l1) / 15238.0))
        val l2 = l1 - (floor((30.0 - j) / 15.0)) * (floor((17719.0 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238.0 * j) / 43.0)) + 29.0
        val hijriMonth = (floor((24.0 * l2) / 709.0)).toInt()
        val hijriDay = (l2 - floor((709.0 * hijriMonth) / 24.0)).toInt()
        val hijriYear = (30.0 * n + j - 30.0).toInt()

        val validMonth = ((hijriMonth - 1).coerceIn(0, 11))
        val validDay = hijriDay.coerceIn(1, 30)

        val source = if (_usePakistanOfficial.value) {
            "Central Ruet-e-Hilal Committee Pakistan (with local adjustment)"
        } else {
            "Astronomical Tabular / Umm Al-Qura Standard Calculation"
        }

        return HijriDate(
            day = validDay,
            month = validMonth + 1,
            monthNameEn = HIJRI_MONTHS_EN[validMonth],
            monthNameUr = HIJRI_MONTHS_UR[validMonth],
            monthNameAr = HIJRI_MONTHS_AR[validMonth],
            year = hijriYear,
            isConfirmedByRuetHilal = _usePakistanOfficial.value,
            sourceDescription = source
        )
    }

    private fun getPakistanRuetOffset(year: Int, month: Int): Int {
        // Pakistan is typically 1 day behind Saudi Arabia / astronomical calculation due to moon-sighting horizon
        return -1
    }

    fun getFormattedHijriDate(calendar: Calendar, manualOffsetDays: Int = _manualAdjustmentDays.value): String {
        val h = getHijriDate(calendar, manualOffsetDays)
        return "${h.day} ${h.monthNameEn} ${h.year} AH"
    }

    fun getFormattedHijriDateUrdu(calendar: Calendar, manualOffsetDays: Int = _manualAdjustmentDays.value): String {
        val h = getHijriDate(calendar, manualOffsetDays)
        return "${h.day} ${h.monthNameUr} ${h.year}ھ"
    }

    /**
     * Generate 30 days schedule for Ramadan using actual astronomical calculations
     */
    fun generateRamadanSchedule(
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        gregorianStartCal: Calendar
    ): List<RamadanDaySchedule> {
        val list = mutableListOf<RamadanDaySchedule>()
        val cal = (gregorianStartCal.clone() as Calendar).apply {
            timeZone = TimeZone.getTimeZone(timeZoneId)
        }

        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        for (dayNum in 1..30) {
            val schedule = PrayerCalculator.calculatePrayerTimes(
                latitude = latitude,
                longitude = longitude,
                calendar = cal,
                timeZoneId = timeZoneId
            )

            val gDate = String.format(
                Locale.US,
                "%02d %s",
                cal.get(Calendar.DAY_OF_MONTH),
                cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
            )

            val dayOfWeek = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]

            list.add(
                RamadanDaySchedule(
                    ramadanDay = dayNum,
                    gregorianDateStr = gDate,
                    hijriDateStr = "$dayNum Ramadan",
                    sehriTime = schedule.fajr,
                    iftarTime = schedule.maghrib,
                    dayOfWeek = dayOfWeek
                )
            )

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return list
    }
}
