package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.text.Html
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.example.data.model.AppLanguage
import com.example.data.model.Ayah
import com.example.data.model.AyahRevelationContext
import com.example.data.model.ScholarTafseer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
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
        val attribution: String,
        val language: AppLanguage
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .build()

    private val cache = ConcurrentHashMap<String, ScholarTafseer>()
    @Volatile private var offlinePrefs: SharedPreferences? = null
    @Volatile private var appContext: Context? = null

    data class OfflineDownloadResult(val success: Boolean, val completed: Int, val total: Int, val error: String? = null)
    private data class UrduSource(val scholarKey: String, val slug: String, val label: String)
    private val urduSources = listOf(
        UrduSource("ibn_kathir", "tafseer-ibn-e-kaseer-urdu", "تفسیر ابن کثیر"),
        UrduSource("sayyid_qutb", "tafsir-fe-zalul-quran-syed-qatab", "فی ظلال القرآن"),
        UrduSource("bayan_ul_quran", "tafsir-bayan-ul-quran", "بیان القرآن"),
        UrduSource("tazkirul_quran", "tazkiru-quran-ur", "تذکیر القرآن"),
        UrduSource("saadi", "ur-tafsir-as-saadi-urdu", "تفسیر السعدی")
    )

    /** Initialize persistent storage once from the application context. */
    fun init(context: Context) {
        appContext = context.applicationContext
        if (offlinePrefs == null) {
            synchronized(this) {
                if (offlinePrefs == null) {
                    offlinePrefs = context.applicationContext.getSharedPreferences(
                        "quran_tafseer_offline_v1",
                        Context.MODE_PRIVATE
                    )
                }
            }
        }
    }

    private fun editionDir(context: Context, slug: String) = File(context.filesDir, "tafseer_offline/$slug")

    fun isAllUrduTafseerDownloaded(context: Context): Boolean =
        urduSources.all { source ->
            val dir = editionDir(context, source.slug)
            File(dir, "_complete").exists() && (1..114).all { File(dir, "$it.json").exists() }
        }

    suspend fun downloadAllUrduTafseer(context: Context, onProgress: (Int, Int, String) -> Unit): OfflineDownloadResult =
        withContext(Dispatchers.IO) {
            init(context)
            val total = urduSources.size * 114
            var completed = 0
            try {
                for (source in urduSources) {
                    val dir = editionDir(context, source.slug)
                    if (File(dir, "_complete").exists() && (1..114).all { File(dir, "$it.json").exists() }) {
                        completed += 114
                        onProgress(completed, total, source.label)
                        continue
                    }
                    dir.mkdirs()
                    File(dir, "_complete").delete()
                    for (surah in 1..114) {
                        val target = File(dir, "$surah.json")
                        if (target.exists()) {
                            try {
                                val old = JSONArray(target.readText(Charsets.UTF_8))
                                val expected = QuranData.surahs.first { it.number == surah }.ayahCount
                                if (old != null && old.length() == expected &&
                                    (0 until old.length()).all { old.optJSONObject(it)?.optString("text")?.isNotBlank() == true }) {
                                    completed++
                                    onProgress(completed, total, source.label)
                                    continue
                                }
                            } catch (_: Exception) { }
                            target.delete()
                        }
                        val primaryUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/${source.slug}/$surah.json"
                        val fallbackUrl = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/${source.slug}/$surah.json"
                        val body = fetchSourceText(primaryUrl, fallbackUrl, "${source.label}, سورۃ $surah")
                        val ayahs = JSONArray(body)
                        val expected = QuranData.surahs.first { it.number == surah }.ayahCount
                        if (ayahs.length() != expected) throw IllegalStateException("${source.label}, سورۃ $surah: ${ayahs.length()}/$expected آیات ملیں")
                        for (index in 0 until ayahs.length()) {
                            val item = ayahs.optJSONObject(index) ?: throw IllegalStateException("خراب آیت ریکارڈ")
                            if (item.optInt("ayah") != index + 1 || item.optString("text").isBlank()) {
                                throw IllegalStateException("${source.label}, سورۃ $surah، آیت ${index + 1}: متن نامکمل ہے")
                            }
                        }
                        val temp = File(dir, "$surah.json.tmp")
                        temp.writeText(ayahs.toString(), Charsets.UTF_8)
                        if (target.exists()) target.delete()
                        if (!temp.renameTo(target)) throw IllegalStateException("فائل محفوظ نہیں ہو سکی")
                        completed++
                        onProgress(completed, total, source.label)
                    }
                    File(dir, "_complete").writeText("verified=114;slug=${source.slug}", Charsets.UTF_8)
                }
                OfflineDownloadResult(true, completed, total)
            } catch (error: Exception) {
                OfflineDownloadResult(false, completed, total, error.message ?: "Download failed")
            }
        }

    /**
     * Fetch original source text with a GitHub Raw primary and jsDelivr fallback.
     * Both endpoints serve the same versioned upstream JSON; no text is generated.
     */
    private fun fetchSourceText(primaryUrl: String, fallbackUrl: String, label: String): String {
        var lastError: Exception? = null
        for (url in listOf(primaryUrl, fallbackUrl)) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Noor-e-Hidayat/1.0 (Quran study app)")
                    .get()
                    .build()
                return client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("$label: HTTP ${response.code}")
                    }
                    response.body?.string()?.takeIf { it.isNotBlank() }
                        ?: throw IllegalStateException("$label: empty source response")
                }
            } catch (error: Exception) {
                lastError = error
            }
        }
        throw IllegalStateException("$label: both source mirrors failed (${lastError?.message ?: "unknown error"})")
    }

    private fun readDownloadedAyah(slug: String, surah: Int, ayah: Int): String? {
        return try {
            val context = appContext ?: return null
            val file = File(editionDir(context, slug), "$surah.json")
            if (!file.exists()) return null
            val list = JSONArray(file.readText(Charsets.UTF_8))
            (0 until list.length()).firstNotNullOfOrNull { index ->
                val item = list.optJSONObject(index)
                if (item?.optInt("ayah") == ayah) item.optString("text").takeIf { it.isNotBlank() } else null
            }
        } catch (_: Exception) { null }
    }

    private fun offlineKey(cacheKey: String): String =
        "tafseer_" + cacheKey.replace("/", "_").replace("-", "_")

    private fun readOffline(cacheKey: String): ScholarTafseer? {
        val raw = offlinePrefs?.getString(offlineKey(cacheKey), null) ?: return null
        return try {
            val json = JSONObject(raw)
            val pointsJson = json.optJSONArray("keyPoints")
            val points = buildList {
                if (pointsJson != null) {
                    for (i in 0 until pointsJson.length()) add(pointsJson.optString(i))
                }
            }
            ScholarTafseer(
                scholarKey = json.getString("scholarKey"),
                authorName = json.getString("authorName"),
                bookTitle = json.getString("bookTitle"),
                eraAndCredentials = json.getString("eraAndCredentials"),
                tafseerText = json.getString("tafseerText"),
                keyPoints = points,
                sourceAttribution = json.optString("sourceAttribution"),
                isMachineTranslated = json.optBoolean("isMachineTranslated"),
                translationNotice = json.optString("translationNotice")
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun writeOffline(cacheKey: String, tafseer: ScholarTafseer) {
        val prefs = offlinePrefs ?: return
        val points = org.json.JSONArray().apply {
            tafseer.keyPoints.forEach { put(it) }
        }
        val json = JSONObject().apply {
            put("scholarKey", tafseer.scholarKey)
            put("authorName", tafseer.authorName)
            put("bookTitle", tafseer.bookTitle)
            put("eraAndCredentials", tafseer.eraAndCredentials)
            put("tafseerText", tafseer.tafseerText)
            put("keyPoints", points)
            put("sourceAttribution", tafseer.sourceAttribution)
            put("isMachineTranslated", tafseer.isMachineTranslated)
            put("translationNotice", tafseer.translationNotice)
        }
        prefs.edit().putString(offlineKey(cacheKey), json.toString()).apply()
    }

    /**
     * Only editions listed by the upstream corpus are offered. The list is language-specific;
     * Hindi currently has one listed edition, not five invented scholar translations.
     */
    /**
     * The tafseer reader uses five Urdu source editions. Other display languages
     * are translations of the selected Urdu source, never a different scholar list.
     */
    fun getTop5Scholars(language: AppLanguage): List<ScholarInfo> = listOf(
        ScholarInfo("ibn_kathir", "حافظ ابن کثیر", "تفسیر القرآن العظیم", "وفات ۷۷۴ھ", "حدیث و آثار"),
        ScholarInfo("sayyid_qutb", "سید قطب", "فی ظلال القرآن", "وفات ۱۳۸۶ھ", "قرآنی تدبر"),
        ScholarInfo("bayan_ul_quran", "ڈاکٹر اسرار احمد", "بیان القرآن", "معاصر", "قرآن کی تشریح"),
        ScholarInfo("tazkirul_quran", "مولانا وحید الدین خان", "تذکیر القرآن", "معاصر", "دعوت و تدبر"),
        ScholarInfo("saadi", "شیخ عبدالرحمن السعدی", "تیسیر الکریم الرحمن", "وفات ۱۳۷۶ھ", "آسان تشریح")
    )

    /** Map each visible source choice to its exact catalogue edition. */
    private fun editionFor(scholarKey: String, language: AppLanguage): Edition? = when (scholarKey) {
        "sayyid_qutb" -> if (language == AppLanguage.URDU)
            Edition("tafsir-fe-zalul-quran-syed-qatab", "Fi Zilal al-Quran by Sayyid Ibrahim Qutb; spa5k/tafsir_api", AppLanguage.URDU) else null
        "ibn_kathir" -> when (language) {
            AppLanguage.URDU -> Edition("tafseer-ibn-e-kaseer-urdu", "Urdu Tafsir Ibn Kathir; corpus mirror: spa5k/tafsir_api", AppLanguage.URDU)
            AppLanguage.ARABIC -> Edition("ar-tafsir-ibn-kathir", "Arabic Tafsir Ibn Kathir; corpus mirror: spa5k/tafsir_api", AppLanguage.ARABIC)
            AppLanguage.ENGLISH -> Edition("en-tafisr-ibn-kathir", "English Tafsir Ibn Kathir; corpus mirror: spa5k/tafsir_api", AppLanguage.ENGLISH)
            AppLanguage.HINDI -> null
        }
        "bayan_ul_quran" -> if (language == AppLanguage.URDU)
            Edition("tafsir-bayan-ul-quran", "Tafsir Bayan ul Quran by Dr. Israr Ahmad; spa5k/tafsir_api", AppLanguage.URDU) else null
        "tazkirul_quran" -> when (language) {
            AppLanguage.URDU -> Edition("tazkiru-quran-ur", "Tazkirul Quran Urdu; spa5k/tafsir_api", AppLanguage.URDU)
            AppLanguage.ENGLISH -> Edition("en-tazkirul-quran", "Tazkirul Quran English; spa5k/tafsir_api", AppLanguage.ENGLISH)
            else -> null
        }
        "saadi" -> when (language) {
            AppLanguage.URDU -> Edition("ur-tafsir-as-saadi-urdu", "Tafsir al-Sa'di Urdu; spa5k/tafsir_api", AppLanguage.URDU)
            AppLanguage.ARABIC -> Edition("ar-tafseer-al-saddi", "Tafsir al-Sa'di Arabic; spa5k/tafsir_api", AppLanguage.ARABIC)
            else -> null
        }
        "maariful_quran" -> if (language == AppLanguage.ENGLISH)
            Edition("en-tafsir-maarif-ul-quran", "Ma'ariful Qur'an by Mufti Muhammad Shafi; spa5k/tafsir_api", AppLanguage.ENGLISH) else null
        "jalalayn" -> if (language == AppLanguage.ENGLISH)
            Edition("en-al-jalalayn", "Al-Jalalayn English; spa5k/tafsir_api", AppLanguage.ENGLISH) else null
        "mukhtasar" -> when (language) {
            AppLanguage.ENGLISH -> Edition("en-tafsir-al-mukhtasar", "English Al-Mukhtasar; spa5k/tafsir_api", AppLanguage.ENGLISH)
            AppLanguage.ARABIC -> Edition("ar-tafsir-al-mukhtasar", "Arabic Al-Mukhtasar; spa5k/tafsir_api", AppLanguage.ARABIC)
            else -> null
        }
        "tabari" -> if (language == AppLanguage.ARABIC)
            Edition("ar-tafsir-al-tabari", "Jami' al-Bayan by Imam al-Tabari; spa5k/tafsir_api", AppLanguage.ARABIC) else null
        "qurtubi" -> if (language == AppLanguage.ARABIC)
            Edition("ar-tafseer-al-qurtubi", "Al-Jami' li-Ahkam al-Qur'an by Imam al-Qurtubi; spa5k/tafsir_api", AppLanguage.ARABIC) else null
        "baghawi" -> if (language == AppLanguage.ARABIC)
            Edition("ar-tafsir-al-baghawi", "Tafsir al-Baghawi; spa5k/tafsir_api", AppLanguage.ARABIC) else null
        "muyassar" -> if (language == AppLanguage.ARABIC)
            Edition("ar-tafsir-muyassar", "Tafsir al-Muyassar; spa5k/tafsir_api", AppLanguage.ARABIC) else null
        "hindi_mukhtasar" -> if (language == AppLanguage.HINDI)
            Edition("hindi-mokhtasar", "Hindi Mokhtasar; spa5k/tafsir_api", AppLanguage.HINDI) else null
        else -> null
    }

    /**
     * A missing published target-language edition falls back to a verified source edition.
     * Translation is performed locally and explicitly labelled as machine translation.
     */
    /**
     * Choose a real published source edition before translating. For Hindi, prefer
     * Urdu/English editions; for Arabic, keep a published Arabic edition when one
     * exists, otherwise use Urdu/English. If a scholar has no connected Urdu/English
     * edition, fall back only to the connected Arabic edition and disclose its source.
     */
    private fun sourceEditionFor(scholarKey: String, targetLanguage: AppLanguage): Edition? {
        // The five displayed commentaries are Urdu source editions. Keep that
        // source fixed when the reader chooses English, Hindi, or Arabic output.
        val preferredLanguages = listOf(AppLanguage.URDU, targetLanguage, AppLanguage.ENGLISH, AppLanguage.ARABIC).distinct()
        return preferredLanguages.firstNotNullOfOrNull { sourceLanguage ->
            editionFor(scholarKey, sourceLanguage)
        }
    }

    private fun mlKitLanguage(language: AppLanguage): String = when (language) {
        AppLanguage.ARABIC -> TranslateLanguage.ARABIC
        AppLanguage.ENGLISH -> TranslateLanguage.ENGLISH
        AppLanguage.HINDI -> TranslateLanguage.HINDI
        AppLanguage.URDU -> TranslateLanguage.URDU
    }

    private fun splitForTranslation(text: String, maxChars: Int = 3000): List<String> {
        val chunks = mutableListOf<String>()
        var start = 0
        val boundaries = charArrayOf('\n', '۔', '؟', '.', '!', '?', ' ')
        while (start < text.length) {
            var end = minOf(start + maxChars, text.length)
            if (end < text.length) {
                val boundary = text.lastIndexOfAny(boundaries, end - 1)
                if (boundary > start + maxChars / 2) end = boundary + 1
            }
            chunks += text.substring(start, end)
            start = end
        }
        return chunks
    }

    private fun translateOnDevice(sourceText: String, sourceLanguage: AppLanguage, targetLanguage: AppLanguage): String {
        if (sourceLanguage == targetLanguage) return sourceText
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(mlKitLanguage(sourceLanguage))
            .setTargetLanguage(mlKitLanguage(targetLanguage))
            .build()
        val translator = Translation.getClient(options)
        try {
            Tasks.await(translator.downloadModelIfNeeded())
            return splitForTranslation(sourceText).joinToString(separator = "") { chunk ->
                Tasks.await(translator.translate(chunk))
            }.trim()
        } finally {
            translator.close()
        }
    }

    /**
     * The al-Sa'di mirror repeats the full surah text in every ayah file.
     * Extract only the explicitly numbered paragraph for the requested ayah.
     */
    private fun extractSaadiAyahSection(rawText: String, editionSlug: String, ayahNumber: Int): String {
        val markerPattern = if (editionSlug.startsWith("ur-")) {
            Regex("(?m)^[\\t ]*\\[$ayahNumber\\][\\t ]*")
        } else {
            Regex("(?m)^[\\t ]*\\{$ayahNumber\\}[\\t ]*")
        }
        val marker = markerPattern.find(rawText)
            ?: throw IllegalStateException("The al-Sa'di source has no explicit marker for ayah $ayahNumber")
        val nextMarkerPattern = if (editionSlug.startsWith("ur-")) {
            Regex("(?m)^[\\t ]*\\[\\d+\\][\\t ]*")
        } else {
            Regex("(?m)^[\\t ]*\\{\\d+\\}[\\t ]*")
        }
        val nextMarker = nextMarkerPattern.find(rawText, marker.range.last + 1)
        val sectionEnd = nextMarker?.range?.first ?: rawText.length
        val section = rawText.substring(marker.range.last + 1, sectionEnd).trim()
        if (section.isBlank()) throw IllegalStateException("Empty al-Sa'di ayah section: $ayahNumber")
        return section
    }

    private fun machineTranslationNotice(language: AppLanguage, sourceLanguage: AppLanguage): String = when (language) {
        AppLanguage.URDU -> "یہ Google ML Kit کے ذریعے ${sourceLanguage.displayName} سے مشینی ترجمہ ہے، شائع شدہ یا عالم کی منظور شدہ ترجمانی نہیں۔ علمی مطالعے کے لیے اصل متن سے موازنہ کریں۔"
        AppLanguage.ARABIC -> "هذه ترجمة آلية عبر Google ML Kit من ${sourceLanguage.displayName}، وليست ترجمة منشورة أو معتمدة من المؤلف. قارنها بالنص الأصلي عند الدراسة العلمية."
        AppLanguage.HINDI -> "यह Google ML Kit द्वारा ${sourceLanguage.displayName} से किया गया मशीन अनुवाद है; यह प्रकाशित या लेखक द्वारा अनुमोदित अनुवाद नहीं है। अध्ययन के लिए मूल पाठ से मिलान करें।"
        AppLanguage.ENGLISH -> "Machine-translated by Google ML Kit from the ${sourceLanguage.displayName} source. This is not a published or scholar-approved translation; compare with the original for study."
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
        // Urdu is the canonical source shown in this reader. Non-Urdu choices
        // always translate that same Urdu edition instead of silently switching books.
        val edition = if (language == AppLanguage.URDU) {
            editionFor(scholar.key, AppLanguage.URDU)
        } else {
            sourceEditionFor(scholar.key, language)
        }
        if (edition == null) {
            return@withContext ScholarTafseer(
                scholarKey = scholar.key,
                authorName = scholar.displayName,
                bookTitle = scholar.bookTitle,
                eraAndCredentials = scholar.era,
                tafseerText = unavailable(language, scholar.key),
                keyPoints = emptyList(),
                sourceAttribution = "No verified source edition is connected."
            )
        }

        val cacheKey = "${edition.slug}/${ayah.surahNumber}/${ayah.ayahNumber}/${language.code}"
        cache[cacheKey]?.let { return@withContext it }

        if (language == AppLanguage.URDU || language == AppLanguage.ENGLISH) {
            readDownloadedAyah(edition.slug, ayah.surahNumber, ayah.ayahNumber)?.let { raw ->
                val scoped = if (scholar.key == "saadi") extractSaadiAyahSection(raw, edition.slug, ayah.ayahNumber) else raw
                val plain = Html.fromHtml(scoped, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                if (plain.isNotBlank()) {
                    val displayed = if (language == AppLanguage.ENGLISH) translateOnDevice(plain, AppLanguage.URDU, AppLanguage.ENGLISH) else plain
                    val saved = ScholarTafseer(
                        scholar.key, scholar.displayName, scholar.bookTitle, scholar.era, displayed, emptyList(),
                        if (language == AppLanguage.ENGLISH) "${edition.attribution}; on-device machine translation from Urdu" else "${edition.attribution}; offline copy",
                        isMachineTranslated = language == AppLanguage.ENGLISH,
                        translationNotice = if (language == AppLanguage.ENGLISH) machineTranslationNotice(AppLanguage.ENGLISH, AppLanguage.URDU) else ""
                    )
                    cache[cacheKey] = saved
                    writeOffline(cacheKey, saved)
                    return@withContext saved
                }
            }
        }
        if (language == AppLanguage.URDU || language == AppLanguage.ENGLISH || language == AppLanguage.HINDI || language == AppLanguage.ARABIC) {
            readOffline(cacheKey)?.let {
                cache[cacheKey] = it
                return@withContext it
            }
        }

        try {
            val primaryUrl = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/${edition.slug}/${ayah.surahNumber}/${ayah.ayahNumber}.json"
            val fallbackUrl = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/${edition.slug}/${ayah.surahNumber}/${ayah.ayahNumber}.json"
            val sourceBody = fetchSourceText(primaryUrl, fallbackUrl, "Surah ${ayah.surahNumber}, ayah ${ayah.ayahNumber}")
            run {
                val json = JSONObject(sourceBody)
                val rawText = json.optString("text").trim()
                if (rawText.isBlank() || rawText == "null") {
                    throw IllegalStateException("No source text exists for this ayah in this edition")
                }
                // al-Sa'di is stored as surah-wide text repeated in each ayah file.
                // Slice its explicit verse marker before displaying it as ayah-specific.
                val ayahScopedText = if (scholar.key == "saadi") {
                    extractSaadiAyahSection(rawText, edition.slug, ayah.ayahNumber)
                } else {
                    rawText
                }
                val plainText = Html.fromHtml(ayahScopedText, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                if (plainText.isBlank()) throw IllegalStateException("Source text is empty after decoding")
                val needsMachineTranslation = edition.language != language
                val displayedText = if (needsMachineTranslation) translateOnDevice(plainText, edition.language, language) else plainText
                if (displayedText.isBlank()) throw IllegalStateException("Translated tafseer is empty")
                val result = ScholarTafseer(
                    scholarKey = scholar.key,
                    authorName = scholar.displayName,
                    bookTitle = scholar.bookTitle,
                    eraAndCredentials = scholar.era,
                    tafseerText = displayedText,
                    keyPoints = emptyList(),
                    sourceAttribution = if (needsMachineTranslation) "${edition.attribution}; on-device machine translation from ${edition.language.displayName} to ${language.displayName}. Not a published translation." else edition.attribution,
                    isMachineTranslated = needsMachineTranslation,
                    translationNotice = if (needsMachineTranslation) machineTranslationNotice(language, edition.language) else ""
                )
                cache[cacheKey] = result
                writeOffline(cacheKey, result)
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
                sourceAttribution = "Source request/translation failed: ${error.message ?: "unknown error"}",
                isMachineTranslated = false
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

    /**
     * Loads Al-Wahidi's published English Asbab al-Nuzul entry for this exact ayah.
     * The corpus does not cover every ayah; missing entries remain explicitly unavailable.
     * Non-English display is ML Kit translation and is disclosed as machine translated.
     */
    suspend fun getTranslationContext(ayah: Ayah, language: AppLanguage): AyahRevelationContext =
        withContext(Dispatchers.IO) {
            init(appContext ?: throw IllegalStateException("Repository not initialized"))
            val base = getRevelationContext(ayah, language)
            val key = "translation_context_${ayah.surahNumber}_${ayah.ayahNumber}_${language.code}"
            offlinePrefs?.getString(key, null)?.let { cached ->
                try {
                    val j = JSONObject(cached)
                    return@withContext AyahRevelationContext(
                        j.optString("sabab"), base.zamanaNuzool, base.maqsadHikmat, base.fazaailBenefits
                    )
                } catch (_: Exception) { }
            }
            try {
                val primary = "https://raw.githubusercontent.com/spa5k/tafsir_api/main/tafsir/en-asbab-al-nuzul-by-al-wahidi/${ayah.surahNumber}/${ayah.ayahNumber}.json"
                val fallback = "https://cdn.jsdelivr.net/gh/spa5k/tafsir_api@main/tafsir/en-asbab-al-nuzul-by-al-wahidi/${ayah.surahNumber}/${ayah.ayahNumber}.json"
                val json = JSONObject(fetchSourceText(primary, fallback, "Asbab al-Nuzul"))
                val englishText = json.optString("text").trim()
                if (englishText.isBlank() || englishText == "null") return@withContext base
                val translated = if (language == AppLanguage.ENGLISH) englishText
                    else translateOnDevice(englishText, AppLanguage.ENGLISH, language)
                val notice = when (language) {
                    AppLanguage.URDU -> "\\n\\nماخذ: اسباب النزول، امام واحدی (انگریزی متن کا Google ML Kit سے مشینی ترجمہ؛ اصل مطبوعہ اردو ترجمہ نہیں)۔"
                    AppLanguage.ARABIC -> "\\n\\nالمصدر: أسباب النزول للواحدي (ترجمة آلية من النص الإنجليزي عبر Google ML Kit وليست ترجمة عربية منشورة)."
                    AppLanguage.HINDI -> "\\n\\nस्रोत: अल-वाहिदी की असबाब अल-नुज़ूल (अंग्रेज़ी से Google ML Kit द्वारा मशीन अनुवाद; प्रकाशित हिंदी अनुवाद नहीं)।"
                    AppLanguage.ENGLISH -> "\\n\\nSource: Asbab al-Nuzul by al-Wahidi (English source text)."
                }
                val resultText = translated + notice
                offlinePrefs?.edit()?.putString(key, JSONObject().put("sabab", resultText).toString())?.apply()
                base.copy(sababNuzool = resultText)
            } catch (_: Exception) {
                base
            }
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
