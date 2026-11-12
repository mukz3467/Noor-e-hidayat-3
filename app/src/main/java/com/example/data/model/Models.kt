package com.example.data.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    ENGLISH("en", "English", "English", false),
    URDU("ur", "Urdu", "اردو", true),
    ARABIC("ar", "Arabic", "العربية", true),
    HINDI("hi", "Hindi", "हिन्दी", false)
}

enum class AppTheme(
    val id: String,
    val titleEn: String,
    val titleUr: String,
    val titleAr: String,
    val titleHi: String
) {
    EMERALD_GOLD("emerald_gold", "Emerald & Gold", "زمردی و سنہری", "الزمرد والذهب", "पन्ना और स्वर्ण"),
    SOFT_LIGHT("soft_light", "Soft Light", "نورانی روشنی", "النور الهادئ", "कोमल प्रकाश"),
    DARK_READING("dark_reading", "Dark Reading", "قراءت لیلی (رات)", "القراءة الليلية", "रात्रि पठन"),
    WARM_CREAM("warm_cream", "Warm Cream", "کریم پرسکون", "البيج الدافئ", "हल्का क्रीम")
}

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameUrdu: String,
    val nameHindi: String,
    val englishMeaning: String,
    val ayahCount: Int,
    val revelationType: RevelationType,
    val startingPara: Int
)

enum class RevelationType {
    MAKKI, MADANI
}

data class Para(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameUrdu: String,
    val nameHindi: String,
    val startSurahNumber: Int,
    val startAyahNumber: Int
)

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val paraNumber: Int,
    val translations: List<AyahTranslation>,
    val tafseers: List<AyahTafseer>
)

data class AyahTranslation(
    val id: String,
    val language: AppLanguage,
    val translatorName: String,
    val editionName: String,
    val translationText: String
)

data class AyahTafseer(
    val id: String,
    val language: AppLanguage,
    val authorName: String,
    val bookTitle: String,
    val tafseerText: String,
    val sourceAttribution: String
)

data class ScholarTafseer(
    val scholarKey: String, // "ibn_kathir", "tabari", "qurtubi", "saadi", "maariful_quran"
    val authorName: String,
    val bookTitle: String,
    val eraAndCredentials: String,
    val tafseerText: String,
    val keyPoints: List<String>,
    val sourceAttribution: String = "",
    val isMachineTranslated: Boolean = false,
    val translationNotice: String = ""
)

data class AyahRevelationContext(
    val sababNuzool: String,       // Reason/Occasion of Revelation (کیوں نازل ہوئی - سبب النزول)
    val zamanaNuzool: String,      // Timing/Period of Revelation (کس وقت نازل ہوئی - زمانہ نزول)
    val maqsadHikmat: String,      // Divine Purpose/Core Message (اس کا مقصد اور حکمت کیا ہے)
    val fazaailBenefits: String    // Virtues & Recitation Benefits (اس کے پڑھنے کے فضائل اور برکات)
)

data class Reciter(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val nameUrdu: String,
    val style: String,
    val audioBaseUrl: String
)

data class Hadith(
    val id: String,
    val collection: String,
    val bookNumber: Int,
    val bookName: String,
    val hadithNumber: Int,
    val chapterArabic: String,
    val chapterEnglish: String,
    val chapterUrdu: String,
    val chapterHindi: String,
    val textArabic: String,
    val textEnglish: String,
    val textUrdu: String,
    val textHindi: String,
    val narrator: String,
    val grade: String,
    val sourceAttribution: String
)

data class Dua(
    val id: String,
    val categoryId: String,
    val titleEn: String,
    val titleUr: String,
    val titleAr: String,
    val titleHi: String,
    val arabic: String,
    val transliteration: String,
    val translationEn: String,
    val translationUr: String,
    val translationHi: String,
    val reference: String,
    val occasionEn: String,
    val occasionUr: String = "",
    val occasionAr: String = "",
    val occasionHi: String = "",
    val bookName: String = "",
    val hadithOrAyahNumber: String = "",
    val grade: String = "Sahih",
    val recommendedTime: String = "",
    val virtuesBenefitsEn: String = "",
    val virtuesBenefitsUr: String = "",
    val virtuesBenefitsAr: String = "",
    val virtuesBenefitsHi: String = "",
    val repeatCount: Int = 1,
    val audioAssetPath: String? = null
)

data class Lecture(
    val id: String,
    val titleEn: String,
    val titleUr: String,
    val titleHi: String,
    val scholar: String,
    val categoryEn: String,
    val categoryUr: String,
    val duration: String,
    val videoId: String,
    val isTrending: Boolean,
    val datePublished: String
)

data class Podcast(
    val id: String,
    val title: String,
    val speaker: String,
    val episodeTitle: String,
    val description: String,
    val duration: String,
    val language: String,
    val publisher: String,
    val publishDate: String,
    val audioUrl: String
)

data class PrayerSchedule(
    val dateStr: String,
    val hijriDate: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val calculationMethod: String,
    val cityName: String
)

data class BookmarkItem(
    val id: String,
    val type: BookmarkType,
    val title: String,
    val subtitle: String,
    val surahNumber: Int? = null,
    val ayahNumber: Int? = null,
    val hadithId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class BookmarkType {
    QURAN, HADITH, DUA
}

data class DownloadedSurahAudio(
    val surahNumber: Int,
    val reciterId: String,
    val sizeBytes: Long,
    val isCompleted: Boolean,
    val progressPercent: Int = 100,
    val localFilePath: String
)

data class ZakatCalculationInput(
    val cashOnHand: Double = 0.0,
    val bankBalance: Double = 0.0,
    val goldGrams: Double = 0.0,
    val goldGramPrice: Double = 0.0,
    val silverGrams: Double = 0.0,
    val silverGramPrice: Double = 0.0,
    val businessInventory: Double = 0.0,
    val receivables: Double = 0.0,
    val otherInvestments: Double = 0.0,
    val debtsAndLiabilities: Double = 0.0,
    val selectedNisabType: NisabType = NisabType.SILVER,
    val currency: String = "USD"
)

enum class NisabType {
    SILVER, GOLD
}

data class ZakatResult(
    val totalAssets: Double,
    val deductibleLiabilities: Double,
    val netZakatableWealth: Double,
    val nisabThreshold: Double,
    val isEligible: Boolean,
    val zakatPayable: Double,
    val currency: String
)

data class ArabicLetter(
    val letter: String,
    val nameArabic: String,
    val nameTranslit: String,
    val makhrajEn: String,
    val makhrajUr: String,
    val makhrajHi: String,
    val isolated: String,
    val initial: String,
    val medial: String,
    val finalPos: String,
    val exampleArabic: String,
    val exampleMeaning: String
)

data class TajweedRule(
    val id: String,
    val titleEn: String,
    val titleUr: String,
    val titleAr: String,
    val titleHi: String,
    val category: String,
    val explanationEn: String,
    val explanationUr: String,
    val explanationHi: String,
    val examples: List<TajweedExample>,
    val explanationAr: String = explanationEn
)

data class TajweedExample(
    val arabicText: String,
    val surahReference: String,
    val ruleApplicationNote: String
)
