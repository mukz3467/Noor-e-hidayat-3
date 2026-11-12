package com.example.data.repository

import com.example.data.model.AppLanguage
import com.example.data.model.Ayah
import com.example.data.model.AyahRevelationContext
import com.example.data.model.ScholarTafseer

/**
 * Never generate commentary and attribute it to a named scholar.
 * Verified source editions and ayah-specific asbab al-nuzul reports must be bundled
 * with precise source metadata before being displayed as scholarly material.
 */
object QuranTafseerRepository {

    data class ScholarInfo(
        val key: String,
        val displayName: String,
        val bookTitle: String,
        val era: String,
        val methodologyBadge: String
    )

    fun getTop5Scholars(language: AppLanguage): List<ScholarInfo> = when (language) {
        AppLanguage.URDU -> listOf(
            ScholarInfo("ibn_kathir", "حافظ ابن کثیر", "تفسیر القرآن العظیم", "وفات ۷۷۴ھ", "حدیث و آثار"),
            ScholarInfo("maududi", "سید ابوالاعلیٰ مودودی", "تفہیم القرآن", "وفات ۱۳۹۹ھ", "موضوعاتی تفسیر"),
            ScholarInfo("maariful_quran", "مفتی محمد شفیع عثمانی", "معارف القرآن", "وفات ۱۳۹۶ھ", "فقہی و لغوی"),
            ScholarInfo("jalalayn", "امام جلال الدین محلی و امام سیوطی", "تفسیر الجلالین", "وفات ۸۶۴ھ / ۹۱۱ھ", "مختصر لغوی"),
            ScholarInfo("saadi", "شیخ عبدالرحمن السعدی", "تیسیر الکریم الرحمن", "وفات ۱۳۷۶ھ", "آسان تشریح")
        )
        AppLanguage.ARABIC -> listOf(
            ScholarInfo("ibn_kathir", "ابن كثير", "تفسير القرآن العظيم", "توفي ٧٧٤هـ", "بالمأثور"),
            ScholarInfo("maududi", "أبو الأعلى المودودي", "تفهيم القرآن", "توفي ١٣٩٩هـ", "موضوعي"),
            ScholarInfo("maariful_quran", "محمد شفيع العثماني", "معارف القرآن", "توفي ١٣٩٦هـ", "فقهي ولغوي"),
            ScholarInfo("jalalayn", "المحلي والسيوطي", "تفسير الجلالين", "توفي ٨٦٤هـ / ٩١١هـ", "لغوي موجز"),
            ScholarInfo("saadi", "عبد الرحمن السعدي", "تيسير الكريم الرحمن", "توفي ١٣٧٦هـ", "تفسير ميسر")
        )
        AppLanguage.HINDI -> listOf(
            ScholarInfo("ibn_kathir", "इब्ने कसीर", "तफ़सीरुल क़ुरआनिल अज़ीम", "मृत्यु 774 हिजरी", "हदीस व आसार"),
            ScholarInfo("maududi", "अबुल आला मौदूदी", "तफ़हीमुल क़ुरआन", "मृत्यु 1399 हिजरी", "विषयगत"),
            ScholarInfo("maariful_quran", "मुहम्मद शफ़ी उसमानी", "मआरिफ़ुल क़ुरआन", "मृत्यु 1396 हिजरी", "फ़िक़्ही"),
            ScholarInfo("jalalayn", "अल-महल्लि व अस-सुयूती", "तफ़सीर अल-जलालैन", "मृत्यु 864 / 911 हिजरी", "भाषाई"),
            ScholarInfo("saadi", "अब्दुर रहमान अस-सादी", "तयसिरुल करीमुर रहमान", "मृत्यु 1376 हिजरी", "सरल व्याख्या")
        )
        AppLanguage.ENGLISH -> listOf(
            ScholarInfo("ibn_kathir", "Ibn Kathir", "Tafsir al-Qur'an al-Azim", "d. 774 AH", "Hadith-based"),
            ScholarInfo("maududi", "Abul A'la Maududi", "Tafhim al-Qur'an", "d. 1399 AH", "Thematic"),
            ScholarInfo("maariful_quran", "Mufti Muhammad Shafi", "Ma'ariful Qur'an", "d. 1396 AH", "Fiqh and language"),
            ScholarInfo("jalalayn", "Al-Mahalli and Al-Suyuti", "Tafsir al-Jalalayn", "d. 864 / 911 AH", "Concise linguistic"),
            ScholarInfo("saadi", "Abdur Rahman al-Sa'di", "Taysir al-Karim al-Rahman", "d. 1376 AH", "Accessible")
        )
    }

    private fun unavailable(language: AppLanguage): String = when (language) {
        AppLanguage.URDU -> "اس آیت کی مستند، اصل تفسیر کا متن اس ایپ کے آف لائن ڈیٹا میں موجود نہیں۔ تصدیق شدہ ماخذ شامل ہونے تک اسے کسی عالم کی طرف منسوب نہیں کیا جائے گا۔"
        AppLanguage.ARABIC -> "النص الأصلي الموثق لتفسير هذه الآية غير متوفر ضمن بيانات التطبيق. لن ننسب شرحاً مولداً إلى عالم."
        AppLanguage.HINDI -> "इस आयत की प्रमाणित मूल तफ़सीर ऐप के ऑफ़लाइन डेटा में उपलब्ध नहीं है। किसी सामान्य व्याख्या को विद्वान के नाम से नहीं जोड़ा जाएगा।"
        AppLanguage.ENGLISH -> "The verified original commentary for this ayah is not bundled offline. Generated explanations will not be attributed to a named scholar."
    }

    fun getScholarTafseer(ayah: Ayah, scholarKey: String, language: AppLanguage): ScholarTafseer {
        val scholar = getTop5Scholars(language).firstOrNull { it.key == scholarKey }
            ?: getTop5Scholars(language).first()
        return ScholarTafseer(
            scholarKey = scholar.key,
            authorName = scholar.displayName,
            bookTitle = scholar.bookTitle,
            eraAndCredentials = scholar.era,
            tafseerText = unavailable(language),
            keyPoints = emptyList()
        )
    }

    fun getScholarTafseer(
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
        val message = when (language) {
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
        return AyahRevelationContext(message, period, unavailable(language), unavailable(language))
    }

    fun getRevelationContext(surahNumber: Int, ayahNumber: Int, language: AppLanguage): AyahRevelationContext {
        val ayah = QuranData.getAyahsForSurah(surahNumber).firstOrNull { it.ayahNumber == ayahNumber }
            ?: Ayah(surahNumber, ayahNumber, "", 1, emptyList(), emptyList())
        return getRevelationContext(ayah, language)
    }
}
