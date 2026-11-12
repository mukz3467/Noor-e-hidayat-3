package com.example.data.repository

import android.text.Html
import com.example.data.model.AppLanguage
import com.example.data.model.Ayah
import com.example.data.model.AyahRevelationContext
import com.example.data.model.ScholarTafseer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Loads unchanged, verse-indexed source commentary. Never invents or paraphrases tafseer
 * and never attributes another author's work to a selected scholar.
 *
 * Upstream corpus: spa5k/tafsir_api, which indexes editions from Quran.com and
 * the Quranic Universal Library (Tarteel). Text is fetched by exact surah/ayah.
 */
object QuranTafseerRepository {
    data class ScholarInfo(
        val key: String,
        val displayName: String,
        val bookTitle: String,
        val era: String,
        val methodologyBadge: String
    )

    private data class Edition(
        val slug: String,
        val attribution: String
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    private val cache = ConcurrentHashMap<String, ScholarTafseer>()

    fun getTop5Scholars(language: AppLanguage): List<ScholarInfo> = when (language) {
        AppLanguage.URDU -> listOf(
            ScholarInfo("ibn_kathir", "حافظ ابن کثیر", "تفسیر القرآن العظیم", "وفات ۷۷۴ھ", "حدیث و آثار"),
            ScholarInfo("tabari", "امام طبری", "جامع البیان عن تأویل آی القرآن", "وفات ۳۱۰ھ", "روایت و لغت"),
            ScholarInfo("qurtubi", "امام قرطبی", "الجامع لأحکام القرآن", "وفات ۶۷۱ھ", "فقہ و احکام"),
            ScholarInfo("saadi", "شیخ عبدالرحمن السعدی", "تیسیر الکریم الرحمن", "وفات ۱۳۷۶ھ", "آسان تشریح"),
            ScholarInfo("maariful_quran", "مفتی محمد شفیع", "معارف القرآن", "وفات ۱۳۹۶ھ", "فقہی و لغوی")
        )
        AppLanguage.ARABIC -> listOf(
            ScholarInfo("ibn_kathir", "ابن كثير", "تفسير القرآن العظيم", "توفي ٧٧٤هـ", "بالمأثور"),
            ScholarInfo("tabari", "الإمام الطبري", "جامع البيان عن تأويل آي القرآن", "توفي ٣١٠هـ", "الرواية واللغة"),
            ScholarInfo("qurtubi", "الإمام القرطبي", "الجامع لأحكام القرآن", "توفي ٦٧١هـ", "فقه وأحكام"),
            ScholarInfo("saadi", "عبد الرحمن السعدي", "تيسير الكريم الرحمن", "توفي ١٣٧٦هـ", "تفسير ميسر"),
            ScholarInfo("maariful_quran", "مفتي محمد شفيع", "معارف القرآن", "توفي ١٣٩٦هـ", "فقهي ولغوي")
        )
        AppLanguage.HINDI -> listOf(
            ScholarInfo("ibn_kathir", "इब्ने कसीर", "तफ़सीरुल क़ुरआनिल अज़ीम", "मृत्यु 774 हिजरी", "हदीस व आसार"),
            ScholarInfo("tabari", "इमाम तबरी", "जामिउल बयान", "मृत्यु 310 हिजरी", "रिवायत व भाषा"),
            ScholarInfo("qurtubi", "इमाम कुर्तुबी", "अल-जामिअ लि-अहकामिल क़ुरआन", "मृत्यु 671 हिजरी", "फ़िक़्ह व अहकाम"),
            ScholarInfo("saadi", "अब्दुर रहमान अस-सादी", "तयसिरुल करीमुर रहमान", "मृत्यु 1376 हिजरी", "सरल व्याख्या"),
            ScholarInfo("maariful_quran", "मुफ़्ती मुहम्मद शफ़ी", "मआरिफ़ुल क़ुरआन", "मृत्यु 1396 हिजरी", "फ़िक़्ही व भाषाई")
        )
        AppLanguage.ENGLISH -> listOf(
            ScholarInfo("ibn_kathir", "Ibn Kathir", "Tafsir al-Qur'an al-Azim", "d. 774 AH", "Hadith-based"),
            ScholarInfo("tabari", "Imam al-Tabari", "Jami' al-Bayan", "d. 310 AH", "Narration and language"),
            ScholarInfo("qurtubi", "Imam al-Qurtubi", "Al-Jami' li-Ahkam al-Qur'an", "d. 671 AH", "Jurisprudence"),
            ScholarInfo("saadi", "Abdur Rahman al-Sa'di", "Taysir al-Karim al-Rahman", "d. 1376 AH", "Accessible"),
            ScholarInfo("maariful_quran", "Mufti Muhammad Shafi", "Ma'ariful Qur'an", "d. 1396 AH", "Fiqh and language")
        )
    }

    /**
     * Only editions actually present in the upstream verse-indexed corpus are mapped.
     * Missing language editions deliberately return null; the app must not fabricate a
     * translation or attribute a generated paraphrase to the mufassir.
     */
    private fun editionFor(scholarKey: String, language: AppLanguage): Edition? = when (scholarKey) {
        "ibn_kathir" -> when (language) {
            AppLanguage.URDU -> Edition("ur-tafseer-ibn-e-kaseer", "Urdu Tafsir Ibn Kathir; corpus mirror: spa5k/tafsir_api")
            AppLanguage.ARABIC -> Edition("ar-tafsir-ibn-kathir", "Arabic Tafsir Ibn Kathir; corpus mirror: spa5k/tafsir_api")
            AppLanguage.ENGLISH -> Edition("en-tafisr-ibn-kathir", "English Tafsir Ibn Kathir; corpus mirror: spa5k/tafsir_api")
            AppLanguage.HINDI -> null
        }
        "tabari" -> if (language == AppLanguage.ARABIC)
            Edition("ar-tafsir-al-tabari", "Arabic Jami' al-Bayan by Imam al-Tabari; corpus mirror: spa5k/tafsir_api")
        else null
        "qurtubi" -> if (language == AppLanguage.ARABIC)
            Edition("ar-tafseer-al-qurtubi", "Arabic Al-Jami' li-Ahkam al-Qur'an by Imam al-Qurtubi; corpus mirror: spa5k/tafsir_api")
        else null
        "saadi" -> when (language) {
            AppLanguage.URDU -> Edition("ur-tafsir-as-saadi-urdu", "Urdu Tafsir al-Sa'di; corpus mirror: spa5k/tafsir_api")
            AppLanguage.ARABIC -> Edition("ar-tafsir-as-saadi", "Arabic Tafsir al-Sa'di; corpus mirror: spa5k/tafsir_api")
            else -> null
        }
        "maariful_quran" -> if (language == AppLanguage.ENGLISH)
            Edition("en-tafsir-maarif-ul-quran", "English Ma'ariful Qur'an by Mufti Muhammad Shafi; corpus mirror: spa5k/tafsir_api")
        else null
        else -> null
    }

    private fun unavailable(language: AppLanguage, scholarKey: String): String {
        val message = when (language) {
            AppLanguage.URDU -> "اس عالم کی اس زبان میں آیت وار مستند تفسیر کا تصدیق شدہ متن دستیاب نہیں۔ اس لیے کوئی وضاحتی متن ان کے نام سے منسوب نہیں کیا گیا۔"
            AppLanguage.ARABIC -> "لا يتوفر نص موثق لتفسير هذا العالم بهذه اللغة على مستوى الآية؛ لذلك لم تُنسب إليه أي كتابة تفسيرية."
            AppLanguage.HINDI -> "इस भाषा में इस विद्वान की आयत-दर-आयत प्रमाणित तफ़सीर उपलब्ध नहीं है। इसलिए कोई व्याख्या उनके नाम से नहीं जोड़ी गई।"
            AppLanguage.ENGLISH -> "A verified ayah-indexed edition in this language is not available in the connected source. No generated explanation is attributed to this scholar."
        }
        return if (scholarKey == "maududi") message else message
    }

    suspend fun getScholarTafseer(
        ayah: Ayah,
        scholarKey: String,
        language: AppLanguage
    ): ScholarTafseer = withContext(Dispatchers.IO) {
        val scholar = getTop5Scholars(language).firstOrNull { it.key == scholarKey }
            ?: getTop5Scholars(language).first()
        val edition = editionFor(scholar.key, language)
        if (edition == null) {
            return@withContext ScholarTafseer(
                scholarKey = scholar.key,
                authorName = scholar.displayName,
                bookTitle = scholar.bookTitle,
                eraAndCredentials = scholar.era,
                tafseerText = unavailable(language, scholar.key),
                keyPoints = emptyList(),
                sourceAttribution = "Source edition not available for this language; no substitute text used."
            )
        }

        val cacheKey = "${edition.slug}/${ayah.surahNumber}/${ayah.ayahNumber}"
        cache[cacheKey]?.let { return@withContext it }

        try {
            val url = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/${edition.slug}/${ayah.surahNumber}/${ayah.ayahNumber}.json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Noor-e-Hidayat/1.0 (Quran study app)")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("Tafseer source returned HTTP ${response.code}")
                val json = JSONObject(response.body?.string().orEmpty())
                val rawText = json.optString("text").trim()
                if (rawText.isBlank() || rawText == "null") {
                    throw IllegalStateException("No source text exists for this ayah in this edition")
                }
                val plainText = Html.fromHtml(rawText, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                if (plainText.isBlank()) throw IllegalStateException("Source text is empty after decoding")
                val result = ScholarTafseer(
                    scholarKey = scholar.key,
                    authorName = scholar.displayName,
                    bookTitle = scholar.bookTitle,
                    eraAndCredentials = scholar.era,
                    tafseerText = plainText,
                    keyPoints = emptyList(),
                    sourceAttribution = edition.attribution
                )
                cache[cacheKey] = result
                result
            }
        } catch (error: Exception) {
            ScholarTafseer(
                scholarKey = scholar.key,
                authorName = scholar.displayName,
                bookTitle = scholar.bookTitle,
                eraAndCredentials = scholar.era,
                tafseerText = when (language) {
                    AppLanguage.URDU -> "مستند ماخذ سے تفسیر حاصل نہیں ہو سکی۔ انٹرنیٹ چیک کریں اور دوبارہ کوشش کریں۔"
                    AppLanguage.ARABIC -> "تعذر تحميل التفسير من المصدر الموثق. تحقق من الاتصال ثم أعد المحاولة."
                    AppLanguage.HINDI -> "प्रमाणित स्रोत से तफ़सीर लोड नहीं हो सकी। इंटरनेट जाँचकर पुनः प्रयास करें।"
                    AppLanguage.ENGLISH -> "Could not load commentary from the verified source. Check your internet connection and try again."
                },
                keyPoints = emptyList(),
                sourceAttribution = "Source request failed: ${error.message ?: "unknown error"}"
            )
        }
    }

    suspend fun getScholarTafseer(
        surahNumber: Int,
        ayahNumber: Int,
        scholarKey: String,
        language: AppLanguage
    ): ScholarTafseer {
        val ayah = QuranData.getAyahsForSurah(surahNumber).firstOrNull { it.ayahNumber == ayahNumber }
            ?: Ayah(surahNumber, ayahNumber, "", 1, emptyList(), emptyList())
        return getScholarTafseer(ayah, scholarKey, language)
    }

    fun getRevelationContext(ayah: Ayah, language: AppLanguage): AyahRevelationContext {
        val unavailable = when (language) {
            AppLanguage.URDU -> "اس مخصوص آیت کے سببِ نزول کی مستند روایت آف لائن ڈیٹا میں موجود نہیں۔ غیر مصدقہ سببِ نزول بیان نہیں کیا جائے گا۔"
            AppLanguage.ARABIC -> "لا تتوفر رواية موثقة لسبب نزول هذه الآية في البيانات المحلية؛ لذلك لن نذكر سبباً غير مثبت."
            AppLanguage.HINDI -> "इस आयत के विशेष कारण-ए-नुज़ूल का प्रमाणित संदर्भ ऑफ़लाइन डेटा में उपलब्ध नहीं है। अप्रमाणित विवरण नहीं दिया जाएगा।"
            AppLanguage.ENGLISH -> "A verified, ayah-specific occasion-of-revelation report is not bundled offline. No unverified sabab al-nuzul is supplied."
        }
        val surah = QuranData.surahs.firstOrNull { it.number == ayah.surahNumber }
        val period = when (language) {
            AppLanguage.URDU -> if (surah?.revelationType?.name == "MAKKI") "سورۃ کی درجہ بندی مکی ہے؛ یہ مخصوص آیت کے وقتِ نزول کی الگ تصدیق نہیں۔" else "سورۃ کی درجہ بندی مدنی ہے؛ یہ مخصوص آیت کے وقتِ نزول کی الگ تصدیق نہیں۔"
            AppLanguage.ARABIC -> if (surah?.revelationType?.name == "MAKKI") "تصنيف السورة مكي، ولا يثبت وحده توقيت نزول الآية بعينها." else "تصنيف السورة مدني، ولا يثبت وحده توقيت نزول الآية بعينها."
            AppLanguage.HINDI -> if (surah?.revelationType?.name == "MAKKI") "सूरह को मक्की वर्गीकृत किया गया है; इससे इस आयत का सटीक समय प्रमाणित नहीं होता।" else "सूरह को मदनी वर्गीकृत किया गया है; इससे इस आयत का सटीक समय प्रमाणित नहीं होता।"
            AppLanguage.ENGLISH -> if (surah?.revelationType?.name == "MAKKI") "The surah is classified as Makki; that alone does not establish this ayah's exact revelation occasion." else "The surah is classified as Madani; that alone does not establish this ayah's exact revelation occasion."
        }
        return AyahRevelationContext(unavailable, period, unavailable, unavailable)
    }

    fun getRevelationContext(surahNumber: Int, ayahNumber: Int, language: AppLanguage): AyahRevelationContext {
        val ayah = QuranData.getAyahsForSurah(surahNumber).firstOrNull { it.ayahNumber == ayahNumber }
            ?: Ayah(surahNumber, ayahNumber, "", 1, emptyList(), emptyList())
        return getRevelationContext(ayah, language)
    }
}
