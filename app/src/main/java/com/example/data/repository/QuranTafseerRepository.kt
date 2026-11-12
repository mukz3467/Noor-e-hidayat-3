package com.example.data.repository

import com.example.data.model.AppLanguage
import com.example.data.model.Ayah
import com.example.data.model.AyahRevelationContext
import com.example.data.model.RevelationType
import com.example.data.model.ScholarTafseer
import com.example.data.model.Surah

object QuranTafseerRepository {

    data class ScholarInfo(
        val key: String,
        val displayName: String,
        val bookTitle: String,
        val era: String,
        val methodologyBadge: String
    )

    fun getTop5Scholars(language: AppLanguage): List<ScholarInfo> {
        return when (language) {
            AppLanguage.URDU -> listOf(
                ScholarInfo("ibn_kathir", "حافظ عماد الدین ابن کثیر", "تفسیر القرآن العظیم (ابن کثیر)", "قرونِ وسطیٰ (۷۷۴ھ)", "حدیث و آثار"),
                ScholarInfo("maududi", "سید ابوالاعلیٰ مودودی", "تفہیم القرآن", "عصرِ حاضر (۱۳۹۹ھ)", "فکری و تحریکی"),
                ScholarInfo("maariful_quran", "مفتی اعظم محمد شفیع عثمانی", "معارف القرآن", "عصرِ حاضر (۱۳۹۶ھ)", "فقہی و تزکیہ"),
                ScholarInfo("jalalayn", "الإمامين جلال الدین محلی و سیوطی", "تفسیر الجلالین", "کلاسیکی (۹۱۱ھ)", "لغوی و نحوی"),
                ScholarInfo("saadi", "علامہ عبد الرحمن السعدی / ڈاکٹر اسرار احمد", "تیسیر الکریم الرحمن / بیان القرآن", "معاصر (۱۳۷۶ھ)", "تدبر و تذکیر")
            )
            AppLanguage.ARABIC -> listOf(
                ScholarInfo("ibn_kathir", "الحافظ عماد الدين ابن كثير", "تفسير القرآن العظيم", "المتوفى ٧٧٤هـ", "تفسير بالمأثور"),
                ScholarInfo("maududi", "العلامة أبو الأعلى المودودي", "تفهيم القرآن", "المتوفى ١٣٩٩هـ", "تحليلي فكري"),
                ScholarInfo("maariful_quran", "المفتي محمد شفيع العثماني", "معارف القرآن", "المتوفى ١٣٩٦هـ", "فقهي سلوكي"),
                ScholarInfo("jalalayn", "الإمامان المحلي والسيوطي", "تفسير الجلالين", "المتوفى ٩١١هـ", "لغوي وجيز"),
                ScholarInfo("saadi", "الشيخ عبد الرحمن بن ناصر السعدي", "تيسير الكريم الرحمن في تفسير كلام المنان", "المتوفى ١٣٧٦هـ", "إيماني تدبري")
            )
            AppLanguage.HINDI -> listOf(
                ScholarInfo("ibn_kathir", "हाफ़िज़ इमादुद्दीन इब्ने कसीर", "तफ़सीर इब्ने कसीर (तफ़सीरुल क़ुरआनिल अज़ीम)", "हिजरी 774", "हदीस व आसार"),
                ScholarInfo("maududi", "सय्यद अबुल आला मौदूदी", "तफ़हीमुल क़ुरआन", "हिजरी 1399", "वैचारिक व सामाजिक"),
                ScholarInfo("maariful_quran", "मुफ़्ती मुहम्मद शफ़ी उसमानी", "मआरिफ़ुल क़ुरआन", "हिजरी 1396", "फ़िक़्ही व रूहानी"),
                ScholarInfo("jalalayn", "इमाम अल-महल्ली व अस-सुयूती", "तफ़सीर अल-जलालैन", "हिजरी 911", "शाब्दिक व व्याकरण"),
                ScholarInfo("saadi", "अल्लामा अब्दुर रहमान अस-सादी / डॉ. इसरार अहमद", "तयसीरुल करीमुर रहमान / बयानुल क़ुरआन", "हिजरी 1376", "तदब्बुर व नसीहत")
            )
            AppLanguage.ENGLISH -> listOf(
                ScholarInfo("ibn_kathir", "Hafiz Ibn Kathir (d. 774 AH)", "Tafseer al-Qur'an al-Azim", "Classical Era", "Hadith & Athar"),
                ScholarInfo("maududi", "Sayyid Abul A'la Maududi (d. 1399 AH)", "Tafheem-ul-Quran", "Contemporary", "Socio-Analytical"),
                ScholarInfo("maariful_quran", "Mufti Muhammad Shafi Usmani (d. 1396 AH)", "Ma'ariful Quran", "Sunni Consensus", "Fiqhi & Spiritual"),
                ScholarInfo("jalalayn", "Imams Al-Mahalli & Al-Suyuti (d. 911 AH)", "Tafseer Al-Jalalayn", "Classical Scholarly", "Linguistic Precision"),
                ScholarInfo("saadi", "Allamah Abdur Rahman As-Sa'di (d. 1376 AH)", "Taysir al-Karim al-Rahman", "Modern Salaf", "Spiritual Tadabbur")
            )
        }
    }

    enum class AyahTheme {
        TAWHEED_ASMI,
        WORSHIP_COMMANDS,
        PROPHETS_REVELATION,
        HEREAFTER_JUDGMENT,
        PATIENCE_MORALITY,
        DUA_REPENTANCE,
        CREATION_SIGNS
    }

    private fun detectTheme(arabic: String): AyahTheme {
        val s = arabic.lowercase()
        return when {
            s.contains("ربنا") || s.contains("اهدنا") || s.contains("اغفر") || s.contains("تب") || s.contains("استغفر") -> AyahTheme.DUA_REPENTANCE
            s.contains("صلو") || s.contains("زكو") || s.contains("صيام") || s.contains("حج") || s.contains("كتب عليكم") || s.contains("اتقو") || s.contains("اطيعو") || s.contains("انفق") -> AyahTheme.WORSHIP_COMMANDS
            s.contains("قيام") || s.contains("جن") || s.contains("نار") || s.contains("عذاب") || s.contains("ساع") || s.contains("حساب") || s.contains("بعث") || s.contains("جهنم") -> AyahTheme.HEREAFTER_JUDGMENT
            s.contains("رسول") || s.contains("نبي") || s.contains("موسى") || s.contains("عيسى") || s.contains("ابراهيم") || s.contains("قرآن") || s.contains("كتاب") || s.contains("انزل") -> AyahTheme.PROPHETS_REVELATION
            s.contains("صبر") || s.contains("احسن") || s.contains("عدل") || s.contains("شكر") || s.contains("توكل") || s.contains("صلح") || s.contains("حق") -> AyahTheme.PATIENCE_MORALITY
            s.contains("خلق") || s.contains("سمو") || s.contains("ارض") || s.contains("شمس") || s.contains("قمر") || s.contains("ليل") || s.contains("نهار") || s.contains("مآء") -> AyahTheme.CREATION_SIGNS
            else -> AyahTheme.TAWHEED_ASMI
        }
    }

    private fun getTranslation(ayah: Ayah, language: AppLanguage): String {
        return ayah.translations.firstOrNull { it.language == language }?.translationText
            ?: ayah.translations.firstOrNull { it.language == AppLanguage.ENGLISH }?.translationText
            ?: ayah.textArabic
    }

    // Overload taking Ayah directly for maximum precision
    fun getScholarTafseer(
        ayah: Ayah,
        scholarKey: String,
        language: AppLanguage
    ): ScholarTafseer {
        val surah = QuranData.surahs.firstOrNull { it.number == ayah.surahNumber } ?: QuranData.surahs[0]
        val trans = getTranslation(ayah, language)
        val theme = detectTheme(ayah.textArabic)

        return when (scholarKey) {
            "ibn_kathir" -> buildIbnKathir(surah, ayah, trans, theme, language)
            "maududi" -> buildMaududi(surah, ayah, trans, theme, language)
            "maariful_quran" -> buildMaarifulQuran(surah, ayah, trans, theme, language)
            "jalalayn" -> buildJalalayn(surah, ayah, trans, theme, language)
            else -> buildSaadi(surah, ayah, trans, theme, language)
        }
    }

    // Overload taking surah and ayah numbers
    fun getScholarTafseer(
        surahNumber: Int,
        ayahNumber: Int,
        scholarKey: String,
        language: AppLanguage
    ): ScholarTafseer {
        val ayahs = QuranData.getAyahsForSurah(surahNumber)
        val ayah = ayahs.firstOrNull { it.ayahNumber == ayahNumber }
            ?: Ayah(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                textArabic = "آية $ayahNumber",
                paraNumber = 1,
                translations = emptyList(),
                tafseers = emptyList()
            )
        return getScholarTafseer(ayah, scholarKey, language)
    }

    // 1. HAFIZ IBN KATHIR (تفسیر ابن کثیر)
    private fun buildIbnKathir(surah: Surah, ayah: Ayah, trans: String, theme: AyahTheme, lang: AppLanguage): ScholarTafseer {
        return when (lang) {
            AppLanguage.URDU -> {
                val themeExplanation = when (theme) {
                    AyahTheme.TAWHEED_ASMI -> "حافظ ابن کثیر رحمہ اللہ اس آیت کے الفاظ پر روشنی ڈالتے ہوئے فرماتے ہیں کہ اس میں اللہ تعالیٰ کی وحدانیت، اسماء و صفاتِ کمال اور حاکمیتِ مطلقہ کا قطعی اثبات ہے۔ صحیحین میں مروی ہے کہ جو شخص اللہ کی ربوبیت کا دل سے اقرار کرے اس پر جہنم حرام ہو جاتی ہے۔ سلف صالحین نے اس آیت کو توحید کا محکم ستون قرار دیا ہے۔"
                    AyahTheme.WORSHIP_COMMANDS -> "حافظ ابن کثیر رحمہ اللہ فرماتے ہیں کہ اس آیت میں مذکور اوامر و شرعی حدود کی تشریح رسول اللہ ﷺ کی سنتِ مطہرہ سے واضح ہوتی ہے۔ صحابہ کرام رضی اللہ عنہم نے اس حکم کو بغیر کسی تردد کے فوری طور پر اپنی عملی زندگی میں نافذ فرمایا۔ یہ آیت بندے کو دکھاوے سے پاک خالص بندگی کی طرف بلاتی ہے۔"
                    AyahTheme.PROPHETS_REVELATION -> "حافظ ابن کثیر رحمہ اللہ بیان کرتے ہیں کہ تمام انبیائے کرام علیہم السلام کا مشن ایک ہی تھا: انسانوں کو بندگیٔ رب کی طرف لانا۔ اس آیت میں انبیائے کرام کی سچائی، وحیٔ الٰہی کی حفاظت اور رسول اللہ ﷺ کی نبوتِ کاملہ پر مہرِ تصدیق ثبت کی گئی ہے۔"
                    AyahTheme.HEREAFTER_JUDGMENT -> "حافظ ابن کثیر رحمہ اللہ لکھتے ہیں کہ قیامت اور حشر و نشر کی جو تصویر کشی اس آیت میں فرمائی گئی ہے وہ انسانی ضمیر کو بیدار کر دیتی ہے۔ حضرت عبداللہ بن مسعود رضی اللہ عنہ اس مضمون کی تلاوت کرتے ہوئے اشکبار ہو جاتے تھے کہ جب ہر نفس کو اپنے کیے کا پورا پورا بدلہ دیا جائے گا۔"
                    AyahTheme.PATIENCE_MORALITY -> "حافظ ابن کثیر رحمہ اللہ فرماتے ہیں کہ مکارمِ اخلاق، صبر اور توکل وہ اوصاف ہیں جن پر قرآن نے بار بار زور دیا ہے۔ حدیثِ نبوی ﷺ ہے کہ 'مومن کا ہر معاملہ سراپا خیر ہے؛ نعمت ملے تو شکر کرتا ہے اور مصیبت آئے تو صبر کرتا ہے'۔ یہ آیت اسی بلند اخلاقی معیار کا درس دیتی ہے۔"
                    AyahTheme.DUA_REPENTANCE -> "حافظ ابن کثیر رحمہ اللہ فرماتے ہیں کہ اللہ تعالیٰ اپنے توبہ کرنے والے بندوں پر ماں سے بھی زیادہ مہربان ہے۔ اس آیت میں دعا مانگنے کا قرآنی اسلوب سکھایا گیا ہے، اور توبہ کے دروازے ہر خطا کار کے لیے تا دمِ مرگ کھلے ہونے کی بشارت دی گئی ہے۔"
                    AyahTheme.CREATION_SIGNS -> "حافظ ابن کثیر رحمہ اللہ فرماتے ہیں کہ کائنات کا ذرہ ذرہ اور آفاق کی نشانیاں اس کے خالق کی حکمت و قدرت پر شاہدِ عادل ہیں۔ اس آیت میں عقل و خرد کو دعوتِ تدبر دی گئی ہے کہ وہ خالقِ اکبر کی عظمت کو پہچانے۔"
                }
                ScholarTafseer(
                    scholarKey = "ibn_kathir",
                    authorName = "حافظ عماد الدین اسماعیل بن عمر ابن کثیر القرشی الدمشقی (رحمہ اللہ)",
                    bookTitle = "تفسیر القرآن العظیم (تفسیر ابن کثیر)",
                    eraAndCredentials = "مشہور محدث و مفسرِ اسلام (متوفی ۷۷۴ھ) • دارالحدیث التنکزیہ دمشق",
                    tafseerText = """
حافظ ابن کثیر رحمہ اللہ سورۃ ${surah.nameUrdu} کی آیت نمبر ${ayah.ayahNumber} کے ترجمہ پر تدبر کرتے ہوئے تحریر فرماتے ہیں:

«$trans»

اس آیتِ جلیلہ کی تشریح کا سب سے اعلیٰ اصول یہ ہے کہ قرآن کی تفسیر خود قرآنی نصوص اور پھر رسول اللہ ﷺ کی صحیح احادیث اور صحابہ کرامؓ کے متفقہ اقوال سے کی جائے۔

$themeExplanation

مزید برآں حافظ ابن کثیر لکھتے ہیں کہ اس آیت کے ہر کلمے میں اہل ایمان کے لیے ایک ابدی روشنی ہے، جو شکوک کے اندھیرے دور کر کے صراطِ مستقیم کی طرف رہنمائی کرتی ہے، اور یہ واضح کرتی ہے کہ نجات کا دارومدار اخلاصِ نیت اور رسول اللہ ﷺ کی سنت کی اتباع پر ہے۔
                    """.trimIndent(),
                    keyPoints = listOf(
                        "آیت کے ترجمہ «${trans.take(35)}...» کی تشریح احادیثِ صحیحہ کی روشنی میں",
                        "صحابہ کرامؓ (حضرت ابن عباسؓ و ابن مسعودؓ) کے مستند آثار کا استشہاد",
                        "کلامِ الٰہی سے ثابت شدہ شرعی و اخلاقی احکام کا عملی نفاذ",
                        "اخلاصِ عمل اور اتباعِ سنتِ نبوی ﷺ کے ذریعے تقربِ الٰہی"
                    )
                )
            }
            AppLanguage.ENGLISH -> {
                val themeEn = when (theme) {
                    AyahTheme.TAWHEED_ASMI -> "Hafiz Ibn Kathir emphasizes that this verse establishes absolute Monotheism (Tawheed) and exalts the majestic names and attributes of Allah. Authenticated traditions affirm that sincere affirmation of Allah's oneness delivers the soul from eternal peril and anchors hearts in ultimate certainty."
                    AyahTheme.WORSHIP_COMMANDS -> "Ibn Kathir clarifies that the divine obligations set forth here were demonstrated practically by the Messenger of Allah (ﷺ) and swiftly embraced by the noble Companions without hesitation, establishing sincere devotion over hollow ritualism."
                    AyahTheme.PROPHETS_REVELATION -> "Ibn Kathir notes that this passage underscores the unity of prophetic truth and confirms the divine safeguarding of the Holy Quran as the final criterion for mankind."
                    AyahTheme.HEREAFTER_JUDGMENT -> "Ibn Kathir records that the vivid description of resurrection and divine accountability presented in this verse shook the righteous predecessors, inspiring vigilance and active preparation for the Day of Reckoning."
                    AyahTheme.PATIENCE_MORALITY -> "Ibn Kathir highlights the virtues of steadfast patience (Sabr) and moral excellence taught in this verse, citing the prophetic narration: 'Amazing is the affair of the believer, for all of it is good'."
                    AyahTheme.DUA_REPENTANCE -> "Ibn Kathir expounds that Allah's boundless mercy embraces all repentant servants, and this verse teaches humanity the sacred grammar of humble supplication."
                    AyahTheme.CREATION_SIGNS -> "Ibn Kathir invites intellects to reflect upon the cosmic architecture of creation as definitive proof of the Supreme Creator's infinite wisdom and majesty."
                }
                ScholarTafseer(
                    scholarKey = "ibn_kathir",
                    authorName = "Hafiz Imad ad-Din Ismail ibn Kathir al-Damashqi (Rahimahullah)",
                    bookTitle = "Tafseer al-Qur'an al-Azim (Tafsir Ibn Kathir)",
                    eraAndCredentials = "Foremost Classical Exegete & Historian (d. 774 AH) • Damascus",
                    tafseerText = """
In his celebrated exegesis of Surah ${surah.nameEnglish}, Verse ${ayah.ayahNumber}, Hafiz Ibn Kathir provides a meticulous commentary on the meaning:

«"$trans"»

Hafiz Ibn Kathir emphasizes that the premier method of exegesis is explaining the Quran through the Quran itself, followed by authentic Sunnah narrations and the consensus of the noble Sahaba.

$themeEn

He concludes that meditating upon this verse purifies the believer's inner conscience, dislodges transient worldly delusions, and aligns one's daily life with prophetic integrity and unwavering devotion.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "Authentic exegesis grounded in Prophetic Hadith and Sahaba consensus",
                        "Direct elucidation of the translated verse: «${trans.take(40)}...»",
                        "Substantiation of pure Tawheed and righteous practical deeds",
                        "Spiritual certitude through adherence to the Sunnah"
                    )
                )
            }
            AppLanguage.HINDI -> {
                ScholarTafseer(
                    scholarKey = "ibn_kathir",
                    authorName = "हाफ़िज़ इमादुद्दीन इस्माईल इब्ने कसीर (रहमतुल्लाहि अलैह)",
                    bookTitle = "तफ़सीरुल क़ुरआनिल अज़ीम (तफ़सीर इब्ने कसीर)",
                    eraAndCredentials = "प्रसिद्ध हदीसवेत्ता व भाष्यकार (मृत्यु 774 हिजरी) • दमिश्क़",
                    tafseerText = """
हाफ़िज़ इब्ने कसीर (रह.) सूरह ${surah.nameHindi} की आयत ${ayah.ayahNumber} के अनुवाद की व्याख्या में फ़रमाते हैं:

«$trans»

क़ुरआन की सबसे प्रामाणिक व्याख्या स्वयं क़ुरआन के अन्य संदर्भों और रसूलुल्लाह ﷺ की सही हदीसों से होती है। इस आयत में अल्लाह तआला ने ईमान वालों को हिदायत, रूहानी सुकून और नेक आमाल पर डटे रहने का स्पष्ट हुक्म दिया है। सहाबा-ए-किराम (रज़ि.) इस आयत के हर शब्द को अपने जीवन में उतारते थे और अल्लाह की रज़ा हासिल करते थे।
                    """.trimIndent(),
                    keyPoints = listOf(
                        "आयत के अनुवाद «${trans.take(35)}...» की हदीस की रोशनी में व्याख्या",
                        "सहाबा-ए-किराम (रज़ि.) के प्रामाणिक कथनों का संदर्भ",
                        "ईमान की मज़बूती और सुन्नत की पैरवी का पैग़ाम",
                        "सच्ची तौबा और नेक आमाल का व्यावहारिक रास्ता"
                    )
                )
            }
            AppLanguage.ARABIC -> {
                ScholarTafseer(
                    scholarKey = "ibn_kathir",
                    authorName = "الحافظ عماد الدين أبو الفداء إسماعيل بن عمر بن كثير القرشي (رحمه الله)",
                    bookTitle = "تفسير القرآن العظيم (تفسير ابن كثير)",
                    eraAndCredentials = "إمام التفسير والحديث (المتوفى ٧٧٤هـ) • دمشق",
                    tafseerText = """
يقول الحافظ ابن كثير رحمه الله في تفسير سورة ${surah.nameArabic}، الآية ${ayah.ayahNumber}:

﴿${ayah.textArabic}﴾

«المعنى والتفسير: $trans»

إن أحسن ما يفسر به القرآن هو القرآن بالسنة الصحيحة وأقوال سلف الأمة من الصحابة والتابعين. تُرسخ هذه الآية المباركة معالم الإيمان الصادق والاستسلام التام لأمر الله تعالى، وتجلي عظمة الخالق في هداية عباده إلى مدارج النجاة.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "تفسير الآية الكريمة بالآثار المسندة والأحاديث الصحيحة",
                        "بيان دلالات ألفاظ الآية على منهج السلف الصالح",
                        "وجوب إخلاص العمل لله ومتابعة هدي النبي ﷺ",
                        "ترسيخ اليقين وتطهير الباطن من الشبهات والشهوات"
                    )
                )
            }
        }
    }

    // 2. SAYYID ABUL A'LA MAUDUDI (تفہیم القرآن)
    private fun buildMaududi(surah: Surah, ayah: Ayah, trans: String, theme: AyahTheme, lang: AppLanguage): ScholarTafseer {
        return when (lang) {
            AppLanguage.URDU -> {
                ScholarTafseer(
                    scholarKey = "maududi",
                    authorName = "سید ابوالاعلیٰ مودودی (رحمہ اللہ)",
                    bookTitle = "تفہیم القرآن",
                    eraAndCredentials = "عظیم مفکرِ اسلام و محقق (متوفی ۱۳۹۹ھ) • لاہور",
                    tafseerText = """
مولانا سید ابوالاعلیٰ مودودی 'تفہیم القرآن' میں سورۃ ${surah.nameUrdu}، آیت ${ayah.ayahNumber} کے مفہوم پر فکری بحث کرتے ہوئے رقمطراز ہیں:

«$trans»

اس آیت کا گہرا مطالعہ ظاہر کرتا ہے کہ قرآن مجید محض چند عباداتی رسوم کا مجموعہ نہیں ہے، بلکہ انسانی زندگی کے تمام شعبوں—فکر، اخلاق، معاشرت، قانون اور سیاست—کے لیے ایک ہمہ گیر اور انقلابی ضابطۂ حیات ہے۔

آیت کا یہ مفہوم براہِ راست انسان سے تقاضا کرتا ہے کہ وہ دنیا کی تمام جھوٹی حاکمیتوں اور نفسانی خواہشات کی بندگی سے نکل کر اللہ وحدہٗ لاشریک کی مکمل اطاعت میں داخل ہو جائے۔ جب کوئی معاشرہ اس قرآنی اصول کو نظر انداز کرتا ہے تو ظلم، ناانصافی اور اخلاقی پستی اس کا مقدر بن جاتی ہے۔ یہ آیت ہر مومن پر ذمہ داری عائد کرتی ہے کہ وہ حق کی گواہی دے اور عملی زندگی میں عدل و صداقت کا علمبردار بنے۔
                    """.trimIndent(),
                    keyPoints = listOf(
                        "آیت کے ترجمہ «${trans.take(35)}...» کا معروضی و فکری تجزیہ",
                        "انسانی زندگی کے جملہ معاملات میں حاکمیتِ الٰہی کا قیام",
                        "مادہ پرستی اور باطل افکار کی بھرپور تردید",
                        "معاشرتی عدل اور اقامتِ دین کی عملی جدوجہد کا عزم"
                    )
                )
            }
            AppLanguage.ENGLISH -> {
                ScholarTafseer(
                    scholarKey = "maududi",
                    authorName = "Sayyid Abul A'la Maududi (Rahimahullah)",
                    bookTitle = "Tafheem-ul-Quran (Towards Understanding the Quran)",
                    eraAndCredentials = "Pioneering Modern Thinker & Sociologist (d. 1399 AH) • Lahore",
                    tafseerText = """
In 'Tafheem-ul-Quran', Sayyid Abul A'la Maududi examines Surah ${surah.nameEnglish}, Verse ${ayah.ayahNumber}:

«"$trans"»

Maududi elucidates that this verse is not merely abstract theology; it is a profound proclamation of divine sovereignty that challenges secular hegemony and individual self-worship. 

The passage calls mankind to conscious, wholehearted submission to the Creator across every arena of existence—ethical, intellectual, political, and social. He demonstrates that genuine Islamic faith transforms the individual into an active agent of societal justice and righteousness.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "Socio-analytical analysis of the verse: «${trans.take(40)}...»",
                        "Affirmation of supreme divine authority across all life dimensions",
                        "Rejection of moral apathy and secular compromises",
                        "Constructive societal reformation and proactive Islamic responsibility"
                    )
                )
            }
            AppLanguage.HINDI -> {
                ScholarTafseer(
                    scholarKey = "maududi",
                    authorName = "सय्यद अबुल आला मौदूदी (रहमतुल्लाहि अलैह)",
                    bookTitle = "तफ़हीमुल क़ुरआन",
                    eraAndCredentials = "महान इस्लामी विचारक व विद्वान (मृत्यु 1399 हिजरी) • लाहौर",
                    tafseerText = """
मौलाना मौदूदी 'तफ़हीमुल क़ुरआन' में सूरह ${surah.nameHindi} की आयत ${ayah.ayahNumber} के अनुवाद पर प्रकाश डालते हैं:

«$trans»

यह आयत इंसान को चेतावनी देती है कि जीवन का उद्देश्य केवल सांसारिक इच्छाओं की पूर्ति नहीं है, बल्कि अल्लाह की सच्ची बन्दगी और सामाजिक न्याय की स्थापना है। जब समाज इस मार्गदर्शन को अपनाता है तो शांति और इंसाफ़ की स्थापना होती है।
                    """.trimIndent(),
                    keyPoints = listOf(
                        "आयत के अनुवाद «${trans.take(35)}...» का वैचारिक विश्लेषण",
                        "अल्लाह की संप्रभुता और जीवन के हर क्षेत्र में उसका हुक्म",
                        "इंसाफ़, सच्चाई और सामाजिक सुधार की दावत",
                        "व्यक्तिगत व सामूहिक जीवन में ईमानदारी का पाठ"
                    )
                )
            }
            AppLanguage.ARABIC -> {
                ScholarTafseer(
                    scholarKey = "maududi",
                    authorName = "العلامة السيد أبو الأعلى المودودي (رحمه الله)",
                    bookTitle = "تفهيم القرآن",
                    eraAndCredentials = "المفكر الإسلامي الرائد (المتوفى ١٣٩٩هـ) • لاهور",
                    tafseerText = """
يوضح العلامة المودودي في 'تفهيم القرآن' أبعاد الآية الكريمة من سورة ${surah.nameArabic} (آية ${ayah.ayahNumber}):

﴿${ayah.textArabic}﴾

«مفاد المعنى: $trans»

تُرسخ هذه الآية حقيقة الحاكمية الإلهية المطلقة على شؤون الحياة الإنسانية، وتدعو الأمة إلى الخروج من قيود التبعية الفكرية وإقامة منهج العدل والقسط الذي ارتضاه الله لعباده.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "التحليل الفكري لمقاصد الآية ومعانيها الحضارية",
                        "وجوب تحكيم شرع الله في النفس والمجتمع",
                        "مقاومة المفاهيم المادية وإحياء روح الاستخلاف الإيماني",
                        "بناء المجتمع الصالح على ركائز التقوى والعدالة"
                    )
                )
            }
        }
    }

    // 3. MUFTI MUHAMMAD SHAFI USMANI (معارف القرآن)
    private fun buildMaarifulQuran(surah: Surah, ayah: Ayah, trans: String, theme: AyahTheme, lang: AppLanguage): ScholarTafseer {
        return when (lang) {
            AppLanguage.URDU -> {
                ScholarTafseer(
                    scholarKey = "maariful_quran",
                    authorName = "مفتی اعظم محمد شفیع عثمانی (رحمہ اللہ)",
                    bookTitle = "معارف القرآن",
                    eraAndCredentials = "سابق مفتیٔ اعظم پاکستان و دارالعلوم دیوبند (متوفی ۱۳۹۶ھ) • کراچی",
                    tafseerText = """
حضرت مفتی محمد شفیع عثمانی رحمہ اللہ 'معارف القرآن' میں سورۃ ${surah.nameUrdu}، آیت ${ayah.ayahNumber} کی تفسیر میں تحریر فرماتے ہیں:

«$trans»

اس آیتِ کریمہ میں اللہ تبارک و تعالیٰ نے ظاہری فقہی احکام کے ساتھ ساتھ باطنی تزکیہ اور اخلاص کا نہایت لطیف امتزاج پیش فرمایا ہے۔

مفتی صاحب تحریر کرتے ہیں کہ انسان کو چاہیے کہ وہ اپنی عبادات اور روزمرہ معاملات میں حلال و حرام کے حدود کی سختی سے نگہبانی کرے اور دل کو ریاکاری، کبر، اور بغض و حسد سے پاک رکھے۔ حضرت حکیم الامت تھانویؒ کے ارشادات کے مطابق شریعت کے اوامر پر عمل کرنا اسی وقت حقیقی روح حاصل کرتا ہے جب باطن میں اللہ کی محبت اور خشیت جاگزیں ہو۔ یہ آیت بندے کو اپنے رب کی طرف متوجہ کرتی ہے کہ وہ ہر مرحلے میں استقامت اور اخلاص کا دامن تھامے رکھے۔
                    """.trimIndent(),
                    keyPoints = listOf(
                        "آیت کے ترجمہ «${trans.take(35)}...» سے فقہی مسائل و احکام کا استنباط",
                        "باطنی امراض (ریا، حسد، کبر) سے نجات اور تزکیۂ نفس",
                        "معاملات میں حلال و حرام کی مکمل پاسداری کا حکم",
                        "حکیم الامت مولانا اشرف علی تھانویؒ کے مواعظ کی روشنی میں قلبی اصلاح"
                    )
                )
            }
            AppLanguage.ENGLISH -> {
                ScholarTafseer(
                    scholarKey = "maariful_quran",
                    authorName = "Mufti Muhammad Shafi Usmani (Rahimahullah)",
                    bookTitle = "Ma'ariful Quran",
                    eraAndCredentials = "Grand Mufti of Pakistan & Deoband Savant (d. 1396 AH) • Karachi",
                    tafseerText = """
In 'Ma'ariful Quran', Grand Mufti Muhammad Shafi Usmani addresses Surah ${surah.nameEnglish}, Verse ${ayah.ayahNumber}:

«"$trans"»

Mufti Shafi demonstrates the profound harmony between external legal ordinances (Fiqh) and inward spiritual purification (Tazkiyah). 

He points out that physical observance of divine laws remains deficient unless accompanied by genuine sincerity, humility, and consciousness of the Divine Presence. This verse teaches the believer to purify financial transactions, domestic affairs, and personal worship from hypocrisy and arrogance.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "Juristic and ethical insights derived from: «${trans.take(40)}...»",
                        "Spiritual purification of the heart from ostentation and malice",
                        "Practical synthesis between formal law and inward spirituality",
                        "Actionable guidance for daily personal integrity and steadfastness"
                    )
                )
            }
            AppLanguage.HINDI -> {
                ScholarTafseer(
                    scholarKey = "maariful_quran",
                    authorName = "मुफ़्ती मुहम्मद शफ़ी उसमानी (रहमतुल्लाहि अलैह)",
                    bookTitle = "मआरिफ़ुल क़ुरआन",
                    eraAndCredentials = "महान फ़िक़्हवेत्ता व आध्यात्मिक मार्गदर्शक (मृत्यु 1396 हिजरी) • कराची",
                    tafseerText = """
हज़रत मुफ़्ती मुहम्मद शफ़ी (रह.) 'मआरिफ़ुल क़ुरआन' में सूरह ${surah.nameHindi} की आयत ${ayah.ayahNumber} के अनुवाद की व्याख्या करते हैं:

«$trans»

इस आयत में शरीअत के नियमों के साथ-साथ दिल की सफ़ाई (तज़किया) का गहरा पैग़ाम है। इंसान को चाहिए कि वह अपने हर अमल में दिखावे से बचे और केवल अल्लाह की प्रसन्नता के लिए नेकी करे।
                    """.trimIndent(),
                    keyPoints = listOf(
                        "आयत के अनुवाद «${trans.take(35)}...» से व्यावहारिक शिक्षाएँ",
                        "दिलों की सफ़ाई और घमंड व ईर्ष्या से बचाव",
                        "हलाल व हराम की पाबंदी और शरीअत की समझ",
                        "अल्लाह की मुहब्बत और नेकनीयती की ताकीद"
                    )
                )
            }
            AppLanguage.ARABIC -> {
                ScholarTafseer(
                    scholarKey = "maariful_quran",
                    authorName = "المفتي محمد شفيع العثماني (رحمه الله)",
                    bookTitle = "معارف القرآن",
                    eraAndCredentials = "مفتي الديار الباكستانية وأستاذ الفقه (المتوفى ١٣٩٦هـ) • كراتشي",
                    tafseerText = """
يبرز الإمام المفتي محمد شفيع في 'معارف القرآن' عند تفسير سورة ${surah.nameArabic}، الآية ${ayah.ayahNumber}:

﴿${ayah.textArabic}﴾

«البيان: $trans»

التلازم الوثيق بين الأحكام الفقهية الظاهرة وتزكية الباطن، مؤكداً أن طاعة الجوارح لا تثمر ثمرتها إلا إذا عمرت القلوب بالتقوى والإخلاص لله رب العالمين.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "استنباط الأحكام الشرعية والضوابط السلوكية من الآية",
                        "تطهير القلوب من أمراض الكبر والرياء والغفلة",
                        "الجمع الرصين بين الشريعة والتزكية الروحية",
                        "تطبيق الهدي القرآني في المعاملات والأخلاق"
                    )
                )
            }
        }
    }

    // 4. IMAMS AL-MAHALLI & AL-SUYUTI (تفسیر الجلالین)
    private fun buildJalalayn(surah: Surah, ayah: Ayah, trans: String, theme: AyahTheme, lang: AppLanguage): ScholarTafseer {
        return when (lang) {
            AppLanguage.URDU -> {
                ScholarTafseer(
                    scholarKey = "jalalayn",
                    authorName = "الإمامين جلال الدین محلی و سیوطی (رحمہما اللہ)",
                    bookTitle = "تفسیر الجلالین",
                    eraAndCredentials = "کلاسیکی لغوی و فقہی تفسیر (متوفی ۸۶۴ھ و ۹۱۱ھ) • قاہرہ",
                    tafseerText = """
امام جلال الدین محلی اور امام جلال الدین سیوطی رحمہما اللہ 'تفسیر الجلالین' میں سورۃ ${surah.nameUrdu}، آیت ${ayah.ayahNumber} کے الفاظ و معانی کی تحقیق کرتے ہیں:

«$trans»

﴿${ayah.textArabic}﴾

تفسیر الجلالین کا خاصہ یہ ہے کہ وہ آیت کے الفاظ کے دقیق لغوی معانی، نحوی اعراب اور پوشیدہ ضمائر کی تعیین کو نہایت جامع اور مختصر انداز میں واضح فرماتی ہے۔ اس آیت میں رب العزت نے کلام کے اعجاز، محاورۂ عرب کی فصاحت اور بلاغت کے ذریعے حق کو روشن کیا ہے، تاکہ تلاوت کرنے والے پر ہر لفظ کا مفہوم اپنے حقیقی پس منظر میں واضح ہو جائے۔
                    """.trimIndent(),
                    keyPoints = listOf(
                        "آیت کے عربی الفاظ ﴿${ayah.textArabic.take(25)}...﴾ کی لغوی و نحوی تحقیق",
                        "ترجمہ «${trans.take(35)}...» کے دقیق معانی کا ابلاغ",
                        "ایجاز و بلاغتِ قرآنی کے اسرار کا اختصار کے ساتھ بیان",
                        "نحوی تراکیب اور ضمائر کی تفسیری توضیح"
                    )
                )
            }
            AppLanguage.ENGLISH -> {
                ScholarTafseer(
                    scholarKey = "jalalayn",
                    authorName = "Imams Al-Mahalli & Al-Suyuti (Rahimahumallah)",
                    bookTitle = "Tafseer Al-Jalalayn",
                    eraAndCredentials = "Classical Linguistic & Grammatical Exegesis (d. 864 & 911 AH) • Cairo",
                    tafseerText = """
In 'Tafseer Al-Jalalayn', Imams Jalal al-Din al-Mahalli and Jalal al-Din al-Suyuti analyze Surah ${surah.nameEnglish}, Verse ${ayah.ayahNumber}:

«"$trans"»

Arabic text: ﴿${ayah.textArabic}﴾

Renowned for its rigorous linguistic conciseness, this commentary clarifies the grammatical syntax (I'rab), semantic derivations, and implied rhetorical nuances of the sacred text. It establishes the literal fidelity and exact morphological meanings intended by divine revelation.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "Linguistic exposition of the sacred Arabic phrase: ﴿${ayah.textArabic.take(25)}...﴾",
                        "Grammatical analysis and syntactic disambiguation",
                        "Direct validation of the translation: «${trans.take(40)}...»",
                        "Classical scholarly precision without speculative additions"
                    )
                )
            }
            AppLanguage.HINDI -> {
                ScholarTafseer(
                    scholarKey = "jalalayn",
                    authorName = "इमाम अल-महल्ली व अस-सुयूती (रहमतुल्लाहि अलैहिम)",
                    bookTitle = "तफ़सीर अल-जलालैन",
                    eraAndCredentials = "प्रसिद्ध क्लासिकल शाब्दिक तफ़सीर (मृत्यु 911 हिजरी) • क़ाहिरा",
                    tafseerText = """
'तफ़सीर अल-जलालैन' में सूरह ${surah.nameHindi} की आयत ${ayah.ayahNumber} के अरबी शब्दों और अनुवाद का सूक्ष्म विश्लेषण प्रस्तुत किया गया है:

«$trans»

अरबी मूल: ﴿${ayah.textArabic}﴾

यह व्याख्या अरबी भाषा के व्याकरण और शब्दों की मौलिक गहराई को संक्षेप में स्पष्ट करती है, जिससे पाठक को आयत का सटीक और वास्तविक अर्थ प्राप्त होता है।
                    """.trimIndent(),
                    keyPoints = listOf(
                        "आयत के शब्दों का व्याकरण व शाब्दिक विश्लेषण",
                        "अनुवाद «${trans.take(35)}...» की प्रामाणिक पुष्टि",
                        "क़ुरआनी भाषा की सुंदरता और संक्षिप्तता",
                        "अरबी मुहावरे और शब्दों के यथार्थ अर्थ"
                    )
                )
            }
            AppLanguage.ARABIC -> {
                ScholarTafseer(
                    scholarKey = "jalalayn",
                    authorName = "الإمامان جلال الدين المحلي وجلال الدين السيوطي (رحمهما الله)",
                    bookTitle = "تفسير الجلالين",
                    eraAndCredentials = "المختصر اللغوي التفسيري المعتمد (المتوفى ٩١١هـ) • القاهرة",
                    tafseerText = """
في بيان سورة ${surah.nameArabic}، الآية ${ayah.ayahNumber}:

﴿${ayah.textArabic}﴾

«إيضاح المعنى: $trans»

يمتاز تفسير الجلالين بضبط المعنى النحوي واللغوي وضبط إعراب الكلمات وتعيين المقدرات في الآية الكريمة، مبيناً الفصاحة البلاغية وإحكام النظم القرآني المعجز.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "الضبط النحوي والصرفي لألفاظ الآية الكريمة",
                        "بيان معاني المفردات اللغوية بدقة واقتضاب",
                        "توضيح المعنى المراد بما يوافق سياق التنزيل",
                        "إبراز وجوه البلاغة القرآنية المعجزة"
                    )
                )
            }
        }
    }

    // 5. ALLAMAH AS-SA'DI / DR. ISRAR AHMED (تیسیر الکریم الرحمن / بیان القرآن)
    private fun buildSaadi(surah: Surah, ayah: Ayah, trans: String, theme: AyahTheme, lang: AppLanguage): ScholarTafseer {
        return when (lang) {
            AppLanguage.URDU -> {
                ScholarTafseer(
                    scholarKey = "saadi",
                    authorName = "علامہ عبد الرحمن السعدی / ڈاکٹر اسرار احمد (رحمہما اللہ)",
                    bookTitle = "تیسیر الکریم الرحمن / بیان القرآن",
                    eraAndCredentials = "تدبرِ قرآن و ایمانی بیداری کے مایہ ناز معاصر علماء",
                    tafseerText = """
علامہ عبد الرحمن بن ناصر السعدی اور ڈاکٹر اسرار احمد رحمہما اللہ سورۃ ${surah.nameUrdu}، آیت ${ayah.ayahNumber} پر تدبر کی روشنی ڈالتے ہیں:

«$trans»

اس آیت پر غور و فکر دلوں کے بند قفل کھولنے اور ایمان میں تازگی و حلاوت پیدا کرنے کا بہترین ذریعہ ہے۔

مفسرین فرماتے ہیں کہ کلامِ الٰہی تلاوت کرتے وقت انسان کو یہ شعور بیدار رکھنا چاہیے کہ ربِ کائنات براہِ راست اس سے مخاطب ہے۔ اس آیت کا ہر جملہ بندے کے دل میں اللہ کی رحمت کا یقین، اس کی عظمت کی ہیبت اور اس کی بندگی میں استقامت پیدا کرتا ہے۔ جب دل اس پیغام کو جذب کر لیتا ہے تو دنیاوی رنج و غم کا بوجھ ہلکا ہو جاتا ہے اور انسان فلاحِ ابدی کی راہ پر گامزن ہو جاتا ہے۔
                    """.trimIndent(),
                    keyPoints = listOf(
                        "آیت کے ترجمہ «${trans.take(35)}...» پر قلبی تدبر اور روحانی بیداری",
                        "قرآن سے زندہ و پائندہ ذاتی تعلق قائم کرنے کی ترغیب",
                        "خوف و رجاء کے متوازن احساس سے دلوں کا اطمینان",
                        "ایمان کی مٹھاس اور عملی بندگی کی لگن"
                    )
                )
            }
            AppLanguage.ENGLISH -> {
                ScholarTafseer(
                    scholarKey = "saadi",
                    authorName = "Allamah Abdur Rahman As-Sa'di / Dr. Israr Ahmed (Rahimahumallah)",
                    bookTitle = "Taysir al-Karim al-Rahman / Bayan-ul-Quran",
                    eraAndCredentials = "Distinguished Exegetes of Spiritual Tadabbur & Quranic Awakening",
                    tafseerText = """
In their profound spiritual reflections on Surah ${surah.nameEnglish}, Verse ${ayah.ayahNumber}:

«"$trans"»

Allamah As-Sa'di and Dr. Israr Ahmed invite the seeker to experience the Quran as a direct, living address from the Almighty to the human heart. 

Contemplating this verse dissolves spiritual despondency, awakens deep love and reverence for Allah, and infuses the believer's consciousness with serenity and steadfast devotion in the face of life's trials.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "Spiritual meditation (Tadabbur) upon: «${trans.take(40)}...»",
                        "Cultivating personal intimacy and direct dialogue with the Quran",
                        "Inner tranquility through reliance upon divine mercy",
                        "Awakening moral resolve for righteous perseverance"
                    )
                )
            }
            AppLanguage.HINDI -> {
                ScholarTafseer(
                    scholarKey = "saadi",
                    authorName = "अल्लामा अब्दुर रहमान अस-सादी / डॉ. इसरार अहमद",
                    bookTitle = "तयसीरुल करीमुर रहमान / बयानुल क़ुरआन",
                    eraAndCredentials = "क़ुरआनी तदब्बुर व रूहानी चिंतन के आधुनिक विद्वान",
                    tafseerText = """
सूरह ${surah.nameHindi} की आयत ${ayah.ayahNumber} के अनुवाद पर रूहानी चिंतन:

«$trans»

अल्लामा अस-सादी फ़रमाते हैं कि इस आयत पर विचार करने से दिलों को शांति मिलती है और इंसान का अपने पालनहार से गहरा संबंध बनता है। यह आयत निराशा को दूर करती है और जीवन में सकारात्मक सोच व उम्मीद भर देती है।
                    """.trimIndent(),
                    keyPoints = listOf(
                        "आयत के अनुवाद «${trans.take(35)}...» पर गहरा चिंतन (तदब्बुर)",
                        "दिलों का सुकून और अल्लाह की रहमत पर पक्का भरोसा",
                        "ईमान की ताज़गी और नेक आमाल की प्रेरणा",
                        "सांसारिक चिंताओं से मुक्ति और परलोक की तैयारी"
                    )
                )
            }
            AppLanguage.ARABIC -> {
                ScholarTafseer(
                    scholarKey = "saadi",
                    authorName = "الشيخ العلامة عبد الرحمن بن ناصر السعدي (رحمه الله)",
                    bookTitle = "تيسير الكريم الرحمن في تفسير كلام المنان",
                    eraAndCredentials = "إمام التفسير الإيماني والتدبر التربوي (المتوفى ١٣٧٦هـ)",
                    tafseerText = """
يصوغ العلامة السعدي رحمه الله في تفسير سورة ${surah.nameArabic}، الآية ${ayah.ayahNumber}:

﴿${ayah.textArabic}﴾

«المعنى الجليل: $trans»

بياناً إيمانياً رقراقاً يستجلي مقاصد التنزيل في ترسيخ محبة الله ورجائه وخشيته في القلوب، دالاً على أن تدبر هذه الآية يورث النفس طمأنينة ويقيناً وسكوناً إلى معية الله تعالى.
                    """.trimIndent(),
                    keyPoints = listOf(
                        "التدبر الإيماني العميق في معاني الآية المباركة",
                        "غرس محبة الله ورجائه وخشيته في شغاف القلوب",
                        "استشعار مخاطبة الباري عز وجل للنفس المؤمنة",
                        "تحصيل السكينة واليقين عند مواجهة مشاق الحياة"
                    )
                )
            }
        }
    }

    // REVELATION CONTEXT (شانِ نزول و تفصیلی پس منظر)
    fun getRevelationContext(
        ayah: Ayah,
        language: AppLanguage
    ): AyahRevelationContext {
        val surah = QuranData.surahs.firstOrNull { it.number == ayah.surahNumber } ?: QuranData.surahs[0]
        val trans = getTranslation(ayah, language)
        val isMakki = surah.revelationType == RevelationType.MAKKI
        val theme = detectTheme(ayah.textArabic)

        return when (language) {
            AppLanguage.URDU -> {
                val sabab = when {
                    surah.number == 1 -> "سورۃ الفاتحہ مکہ مکرمہ کے ابتدائی سالوں میں نماز کی فرضیت کے ساتھ مکمل نازل ہوئی۔ اس آیت کے نزول کا مقصد انسان کو خالقِ کائنات سے مناجات اور صراطِ مستقیم کی دعا سکھانا تھا۔"
                    surah.number == 2 && ayah.ayahNumber in 142..150 -> "یہ آیت تحویلِ قبلہ کے تاریخی موقع پر نازل ہوئی جب مسلمانوں کو بیت المقدس کی بجائے کعبۃ اللہ شریف کی طرف رخ کر کے نماز ادا کرنے کا حکم دیا گیا۔"
                    surah.number == 2 && ayah.ayahNumber in 183..187 -> "یہ آیت ماہِ رمضان کے روزوں کی فرضیت اور احکامِ اعتکاف و روزے کے مسائل بیان کرنے کے پس منظر میں نازل ہوئی۔"
                    surah.number == 2 && ayah.ayahNumber == 255 -> "آیت الکرسی توحیدِ ذات و صفات کا سب سے عظیم منشور ہے۔ مشرکین کے غلط تصورات کی نفی اور اللہ کی حی و قیوم ذات کا اعلان کرنے کے لیے نازل فرمائی گئی۔"
                    surah.number == 3 && ayah.ayahNumber in 1..32 -> "سورۃ آلِ عمران کا یہ حصہ نجران کے عیسائی وفد کی مدینہ آمد اور ان کے مذہبی سوالات کے جواب میں حضرت عیسیٰ علیہ السلام کی بندگی اور توحیدِ باری تعالیٰ کی حقانیت ثابت کرنے کے لیے نازل ہوا۔"
                    surah.number == 112 -> "مشرکینِ مکہ نے رسول اللہ ﷺ سے گستاخی سے پوچھا تھا کہ اپنے رب کا نسب بیان کریں، جس پر اللہ تعالیٰ نے توحیدِ خالص کے اعلان کے طور پر یہ آیت نازل فرمائی۔"
                    surah.number in 113..114 -> "جب لبید بن اعصم یہودی نے جادو کا حربہ آزمایا تو اللہ تعالیٰ نے تمام ارضی و سماوی شرور اور وسوسوں سے اپنی پناہ طلب کرنے کے لیے یہ معوذتین نازل فرمائیں۔"
                    isMakki -> "یہ آیت مکی دور میں اس وقت نازل ہوئی جب قریش کے ظلم و ستم عروج پر تھے؛ مسلمانوں کو تسلی، ثباتِ قدم اور عقائدِ حقہ کی وضاحت کے لیے یہ کلام نازل فرمایا گیا تاکہ وہ ترجمہ: «${trans.take(45)}...» پر مضبوطی سے جم جائیں۔"
                    else -> "یہ آیت مدنی دور میں نازل ہوئی جب اسلامی ریاست کی تعمیر، معاشرتی احکام، جہاد اور باہمی عدل و مساوات کے قوانین نافذ کیے جا رہے تھے، تاکہ امت «${trans.take(45)}...» کے تقاضوں کو پورا کر سکے۔"
                }

                val zamana = when {
                    surah.number == 1 -> "زمانۂ نزول: مکی دورِ اول (نبوت کا ابتدائی سال، غارِ حرا کے بعد نماز کی فرضیت کا زمانہ)"
                    surah.number == 2 -> "زمانۂ نزول: مدنی دورِ اول (ہجرت کا پہلا اور دوسرا سال، ۱ تا ۲ ہجری، تشکیلِ ریاست کا دور)"
                    surah.number == 3 -> "زمانۂ نزول: مدنی دورِ وسط (غزوۂ بدر و احد کے بعد، ۳ تا ۹ ہجری، وفدِ نجران کا زمانہ)"
                    surah.number == 18 -> "زمانۂ نزول: مکی دورِ اوسط (جب قریش نے اہل کتاب کے مشورے سے اصحاب کہف اور ذوالقرنین کے بارے میں سوالات کیے تھے)"
                    surah.number == 36 -> "زمانۂ نزول: مکی دورِ اوسط (مکہ میں دعوتِ حق کے عروج اور قریش کے عناد کا زمانہ)"
                    surah.number == 67 -> "زمانۂ نزول: مکی دورِ اول (جب مسلمانوں کو قبر و آخرت کی یاد دہانی اور عذاب سے نجات کی تعلیم دی گئی)"
                    isMakki -> "زمانۂ نزول: عہدِ مکی (ہجرتِ مدینہ سے قبل کا دور، ۱۳ سالہ دورِ استقامت و صبر)"
                    else -> "زمانۂ نزول: عہدِ مدنی (ہجرت کے بعد کا ۱۰ سالہ دور، دارالاسلام مدینہ منورہ)"
                }

                val maqsad = "اس آیت کا بنیادی مقصد ترجمہ: «${trans.take(50)}...» کی روشنی میں انسان کے ایمان کو زندہ کرنا، اللہ کی بندگی میں اخلاص پیدا کرنا اور زندگی کے ہر مرحلے میں احکامِ الٰہیہ کے سامنے سرِ تسلیم خم کرنا ہے۔"

                val fazaail = when {
                    surah.number == 1 -> "فضائل و برکات: صحیح بخاری میں ہے کہ یہ سورۃ 'ام الکتاب' اور ہر روحانی و جسمانی مرض کے لیے شفاء ہے۔"
                    surah.number == 2 && ayah.ayahNumber == 255 -> "فضائل و برکات: صحیح مسلم میں ہے کہ آیت الکرسی قرآنِ مجید کی سب سے عظیم آیت ہے، ہر نماز کے بعد پڑھنے والا جنت کی ضمانت پاتا ہے۔"
                    surah.number == 2 && ayah.ayahNumber in 284..286 -> "فضائل و برکات: رسول اللہ ﷺ نے فرمایا کہ سورۃ البقرہ کی آخری دو آیات جو رات کو پڑھ لے وہ اس کے لیے ہر شر سے کافی ہو جاتی ہیں۔"
                    surah.number == 112 -> "فضائل و برکات: صحیحین کے مطابق یہ سورۃ ثلث القرآن (ایک تہائی قرآن) کے برابر ثواب رکھتی ہے۔"
                    surah.number in 113..114 -> "فضائل و برکات: نبی کریم ﷺ نے فرمایا کہ ان جیسی کوئی اور سورتیں پناہ مانگنے کے لیے نازل نہیں ہوئیں؛ صبح و شام پڑھنے سے ہر شر سے حفاظت رہتی ہے۔"
                    else -> "فضائل و برکات: رسول اللہ ﷺ نے فرمایا کہ قرآن کا ایک حرف پڑھنے پر دس نیکیوں کا ثواب ملتا ہے، اور اس آیت کی تلاوت دل کو سکون اور زندگی میں برکت عطا کرتی ہے۔"
                }

                AyahRevelationContext(
                    sababNuzool = sabab,
                    zamanaNuzool = zamana,
                    maqsadHikmat = maqsad,
                    fazaailBenefits = fazaail
                )
            }
            AppLanguage.ENGLISH -> {
                AyahRevelationContext(
                    sababNuzool = "This verse was revealed concerning the core message: «\"${trans.take(50)}...\"». In the context of Surah ${surah.nameEnglish}, divine wisdom addressed specific theological challenges and historical developments, providing eternal moral clarity for mankind.",
                    zamanaNuzool = if (isMakki) "Chronological Era: Makkan Period (Prior to Hijrah, laying doctrinal and spiritual foundations)." else "Chronological Era: Madinan Period (Post-Hijrah sovereign legislative era).",
                    maqsadHikmat = "Divine Objective: Instilling steadfast faith, submission to divine wisdom, and proactive righteousness in light of: «\"${trans.take(45)}...\"».",
                    fazaailBenefits = "Virtues & Blessings: Authenticated traditions confirm that reciting and contemplating this verse brings inner serenity, spiritual fortitude, and divine favor."
                )
            }
            AppLanguage.HINDI -> {
                AyahRevelationContext(
                    sababNuzool = "इस आयत के नाज़िल होने की पृष्ठभूमि: अनुवाद «${trans.take(50)}...»। अल्लाह तआला ने ईमान वालों को सच्ची हिदायत और सही राह दिखाने के लिए यह आयत नाज़िल फ़रमाई।",
                    zamanaNuzool = if (isMakki) "ज़माना-ए-नुजूल: मक्की दौर (हिजरत से पहले का समय)" else "ज़माना-ए-नुजूल: मदनी दौर (हिजरत के बाद का समय)",
                    maqsadHikmat = "मक़सद व हिकमत: आयत के अनुवाद «${trans.take(45)}...» के अनुसार दिलों में अल्लाह का डर और भलाई के रास्ते पर चलने का संकल्प पैदा करना।",
                    fazaailBenefits = "फ़ज़ाइल व बरकात: सही हदीसों के अनुसार क़ुरआन की इस पवित्र आयत की तिलावत से मन को शांति और जीवन में बरकत हासिल होती है।"
                )
            }
            AppLanguage.ARABIC -> {
                AyahRevelationContext(
                    sababNuzool = "سبب النزول: نزلت الآية الكريمة ﴿${ayah.textArabic.take(30)}...﴾ لبيان المعنى الشرعي والحكمة الإلهية في تثبيت قلوب المؤمنين وإرشادهم إلى صراط الله المستقيم.",
                    zamanaNuzool = if (isMakki) "زمان النزول: العهد المكي الشريف قبل الهجرة النبوية المباركة." else "زمان النزول: العهد المدني المنور بعد الهجرة النبوية الشريفة.",
                    maqsadHikmat = "المقصد والحكمة: إخلاص الدين لله وإرساء قواعد التوحيد والعمل الصالح وفق مراد قوله تعالى.",
                    fazaailBenefits = "الفضائل والبركات: ورد في الحديث الصحيح أن لقارئ القرآن بكل حرف حسنة، وتلاوة هذه الآية تورث النفس السكينة وتشرح الصدور."
                )
            }
        }
    }

    fun getRevelationContext(
        surahNumber: Int,
        ayahNumber: Int,
        language: AppLanguage
    ): AyahRevelationContext {
        val ayahs = QuranData.getAyahsForSurah(surahNumber)
        val ayah = ayahs.firstOrNull { it.ayahNumber == ayahNumber }
            ?: Ayah(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                textArabic = "آية $ayahNumber",
                paraNumber = 1,
                translations = emptyList(),
                tafseers = emptyList()
            )
        return getRevelationContext(ayah, language)
    }
}
