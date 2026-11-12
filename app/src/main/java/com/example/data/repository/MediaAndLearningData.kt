package com.example.data.repository

import com.example.data.model.*

object MediaAndLearningData {

    val lectureCategories = listOf(
        "All",
        "Quran Explanation",
        "Hadith Lessons",
        "Islamic History",
        "Seerah of Prophet Muhammad ﷺ",
        "Islamic Character",
        "Daily Islamic Guidance",
        "Family and Social Life",
        "Islamic Education"
    )

    val lectures = listOf(
        Lecture(
            id = "lec_1",
            titleEn = "Finding Peace in Times of Anxiety and Hardship",
            titleUr = "پریشانی اور آزمائش کے لمحات میں سکونِ قلب کی تلاش",
            titleHi = "कठिनाई और चिंता के समय मन की शांति",
            scholar = "Mufti Ismail Menk",
            categoryEn = "Daily Islamic Guidance",
            categoryUr = "روزمرہ اسلامی رہنمائی",
            duration = "28:45",
            videoId = "0l2hHqxS-0M",
            isTrending = true,
            datePublished = "2024-05-12"
        ),
        Lecture(
            id = "lec_2",
            titleEn = "Deep Reflections on the Holy Quran",
            titleUr = "فہم القرآن: قرآنی حکمتوں اور فکری گہرائیوں کا بیان",
            titleHi = "पवित्र क़ुरआन की गहरी समझ और विचार",
            scholar = "Ustadh Nouman Ali Khan",
            categoryEn = "Quran Explanation",
            categoryUr = "فہم القرآن و تفسیر",
            duration = "45:12",
            videoId = "rQcj09k6TIw",
            isTrending = true,
            datePublished = "2024-06-20"
        ),
        Lecture(
            id = "lec_3",
            titleEn = "Haqiqat-e-Iman (The Essence of True Faith)",
            titleUr = "حقیقتِ ایمان اور قرآنی منہجِ انقلاب",
            titleHi = "ईमान की वास्तविकता और क़ुरआनी मार्गदर्शन",
            scholar = "Dr. Israr Ahmed (رحمه الله)",
            categoryEn = "Hadith Lessons",
            categoryUr = "دروسِ حدیث و عقیدہ",
            duration = "52:10",
            videoId = "yKJ05juuxBI",
            isTrending = false,
            datePublished = "Archive Selection"
        ),
        Lecture(
            id = "lec_4",
            titleEn = "Zindagi Khushaal Bananay Ka Nuskha (Prosperous Family Life)",
            titleUr = "گھریلو زندگی کو خوشحال اور پرسکون بنانے کا نسخہ",
            titleHi = "पारिवारिक जीवन को खुशहाल बनाने का तरीक़ा",
            scholar = "Maulana Tariq Jameel",
            categoryEn = "Family and Social Life",
            categoryUr = "خاندانی اور معاشرتی زندگی",
            duration = "36:20",
            videoId = "ZwgrfuSjX_c",
            isTrending = true,
            datePublished = "2024-07-15"
        ),
        Lecture(
            id = "lec_5",
            titleEn = "Lessons from the Prophetic Seerah",
            titleUr = "سیرتِ نبوی ﷺ: مدنی زندگی کے تابناک نقوش اور اسباق",
            titleHi = "मदीना का जीवन: पैगंबर ﷺ की सीरत से सबक",
            scholar = "Dr. Yasir Qadhi",
            categoryEn = "Seerah of Prophet Muhammad ﷺ",
            categoryUr = "سیرتِ نبوی ﷺ",
            duration = "1:02:15",
            videoId = "boocPQ4imHA",
            isTrending = false,
            datePublished = "2024-03-10"
        ),
        Lecture(
            id = "lec_6",
            titleEn = "Dealing with Hardships and Trials",
            titleUr = "صبر، استقامت اور مصائب کا سامنا کیسے کریں؟",
            titleHi = "मुसीबतों और आज़माइशों का सामना कैसे करें?",
            scholar = "Mufti Ismail Menk",
            categoryEn = "Daily Islamic Guidance",
            categoryUr = "روزمرہ اسلامی رہنمائی",
            duration = "22:30",
            videoId = "PYbU9cRrtFc",
            isTrending = true,
            datePublished = "2024-08-01"
        ),
        Lecture(
            id = "lec_7",
            titleEn = "Finding Hope in Times of Darkness",
            titleUr = "مایوسی کے اندھیروں میں قرآنی امید کی کرن",
            titleHi = "निराशा के समय में क़ुरआनी उम्मीद",
            scholar = "Ustadh Nouman Ali Khan",
            categoryEn = "Quran Explanation",
            categoryUr = "فہم القرآن و تفسیر",
            duration = "34:10",
            videoId = "8wJN3GecQY0",
            isTrending = false,
            datePublished = "2024-07-22"
        ),
        Lecture(
            id = "lec_8",
            titleEn = "Maa Baap Ki Azmat Aur Huqooq (Status of Parents)",
            titleUr = "والدین کی عظمت اور اولاد کے فرائض",
            titleHi = "माता-पिता का सम्मान और संतान के कर्तव्य",
            scholar = "Maulana Tariq Jameel",
            categoryEn = "Islamic Character",
            categoryUr = "اخلاق و تربیت",
            duration = "41:50",
            videoId = "M6crlVWXC-k",
            isTrending = true,
            datePublished = "2024-06-05"
        ),
        Lecture(
            id = "lec_9",
            titleEn = "Dawat-e-Quran Aur Inqilabi Fikr",
            titleUr = "دعوتِ قرآن اور امتِ مسلمہ کا مطلوبہ کردار",
            titleHi = "क़ुरआन का संदेश और उम्मत की ज़िम्मेदारी",
            scholar = "Dr. Israr Ahmed (رحمه الله)",
            categoryEn = "Islamic Education",
            categoryUr = "تعلیم و تربیت",
            duration = "48:15",
            videoId = "TVk8m31rmw4",
            isTrending = false,
            datePublished = "Archive Selection"
        )
    )

    val podcasts = listOf(
        Podcast(
            id = "pod_1",
            title = "Heartwork: The Soul of the Believer",
            speaker = "Dr. Omar Suleiman",
            episodeTitle = "Episode 12: Purifying the Heart from Malice and Envy",
            description = "Exploring internal spiritual ailments and the prophetic cure through sincere repentance and Istighfar.",
            duration = "24:18",
            language = "English",
            publisher = "Yaqeen Institute",
            publishDate = "2024-08-01",
            audioUrl = "https://example.com/podcasts/yaqeen_ep12.mp3"
        ),
        Podcast(
            id = "pod_2",
            title = "Quranic Wisdom for Daily Life",
            speaker = "Ustadh Nouman Ali Khan",
            episodeTitle = "Finding Hope When Doors Close (Surah Ad-Duha)",
            description = "A compassionate reflection on Allah's promises to the Prophet ﷺ during times of isolation.",
            duration = "31:40",
            language = "English / Urdu",
            publisher = "Bayyinah Institute",
            publishDate = "2024-08-15",
            audioUrl = "https://example.com/podcasts/bayyinah_duha.mp3"
        ),
        Podcast(
            id = "pod_3",
            title = "Dars-e-Quran wa Hidayat",
            speaker = "Mufti Muhammad Taqi Usmani",
            episodeTitle = "Halal Earnings and Barakah in the Modern World",
            description = "Principles of Islamic ethics, honest trade, and safeguarding oneself from dubious financial gains.",
            duration = "38:50",
            language = "Urdu",
            publisher = "Darul Uloom Karachi",
            publishDate = "2024-07-28",
            audioUrl = "https://example.com/podcasts/taqi_usmani_barakah.mp3"
        )
    )

    val arabicAlphabet = listOf(
        ArabicLetter("ا", "أَلِف", "Alif", "Produced from the emptiness of the mouth and throat (Jawf).", "حلق اور منہ کے خالی حصے (جوف) سے ادا ہوتا ہے۔", "गले और मुँह के खाली हिस्से (जौफ़) से निकलता है।", "ا", "ا", "ـا", "ـا", "أَبٌ", "Father"),
        ArabicLetter("ب", "بَاء", "Baa", "Produced by closing the wet inner part of both lips firmly.", "دونوں ہونٹوں کے تری والے اندرونی حصے کو آپس میں ملانے سے۔", "दोनों होंठों کے بھیتری گیلے بھاگ کو ملانے سے۔", "ب", "بـ", "ـبـ", "ـب", "بَيْتٌ", "House"),
        ArabicLetter("ت", "تَاء", "Taa", "Tip of the tongue touching the roots of the top two incisors.", "زبان کی نوک جب اوپر والے سامنے کے دو دانتوں کی جڑ سے لگے۔", "ज़बान کی نوک اوپر کے سامنے والے دو دانتوں کی جڑ سے لگے۔", "ت", "تـ", "ـتـ", "ـت", "تِينٌ", "Fig"),
        ArabicLetter("ث", "ثَاء", "Thaa", "Tip of the tongue touching the edges of the top two incisors.", "زبان کے سرے کو اوپر کے سامنے والے دونوں دانتوں کے کناروں سے ملانے سے۔", "ज़बान کے سرے کو اوپر کے سامنے والے دانتوں کے کنارے سے لگے।", "ث", "ثـ", "ـثـ", "ـث", "ثَمَرٌ", "Fruit"),
        ArabicLetter("ج", "جِيم", "Jeem", "Middle of the tongue touching the upper palate.", "زبان کا درمیانی حصہ جب اوپر کے تالو سے لگے۔", "ज़बान کا مدھیہ بھاگ اوپر کے تالو سے لگے।", "ج", "جـ", "ـجـ", "ـج", "جَنَّةٌ", "Paradise"),
        ArabicLetter("ح", "حَاء", "Haa", "Middle of the throat (Wasat al-Halq), clear and aspirated.", "حلق کے درمیانی حصے سے صاف اور کھنکھار کے بغیر ادا ہوتا ہے۔", "گلے کے مدھیہ بھاگ سے صاف نکلتا ہے।", "ح", "حـ", "ـحـ", "ـح", "حَقٌّ", "Truth"),
        ArabicLetter("خ", "خَاء", "Khaa", "Top of the throat nearest to the mouth (Adna al-Halq).", "حلق کا وہ حصہ جو منہ کی طرف سب سے قریب ہے۔", "گلے کا وہ اوپری بھاگ جو منہ کے سب سے قریب ہے۔", "خ", "خـ", "ـخـ", "ـخ", "خَيْرٌ", "Good"),
        ArabicLetter("د", "دَال", "Daal", "Tip of the tongue touching the roots of top incisors (Qalqalah letter).", "زبان کی نوک اوپر کے سامنے والے دانتوں کی جڑ سے لگے (حروفِ قلقلہ میں سے)۔", "ज़بان کی نوک اوپر کے دانتوں کی جڑ سے لگے (قلقلہ اکھشر)۔", "د", "د", "ـد", "ـد", "دِينٌ", "Religion"),
        ArabicLetter("ر", "رَاء", "Raa", "Tip of the tongue with slight back touching the gum of incisors.", "زبان کی نوک کی پشت جب سامنے کے دانتوں کے مسوڑھے سے لگے۔", "ज़بان کی نوک کی پیٹھ مسوڑے سے لگے।", "ر", "ر", "ـر", "ـر", "رَحْمَةٌ", "Mercy"),
        ArabicLetter("س", "سِين", "Seen", "Tip of the tongue just above the bottom teeth, making a sharp whistle.", "زبان کا سرا جب نیچے کے سامنے کے دانتوں کے اندرونی کنارے سے لگے۔", "ज़بان کا سرا جب نیچے کے سامنے کے دانتوں کے اندرونی کنارے سے لگے।", "س", "سـ", "ـسـ", "ـس", "سَلَامٌ", "Peace")
    )

    val tajweedRules = listOf(
        TajweedRule(
            id = "noon_sakinah",
            titleEn = "Rules of Noon Sakinah & Tanween",
            titleUr = "احکامِ نون ساکن و تنوین",
            titleAr = "أحكام النون الساكنة والتنوين",
            titleHi = "नून साकिना और तनवीन के नियम",
            category = "Core Tajweed",
            explanationEn = "Noon Sakinah (نْ) and Tanween (ً ٍ ٌ) have four rules: 1. Izhar (Clear pronunciation without Ghunnah before throat letters: ء ه ع ح غ خ); 2. Idgham (Merging into ي ر م ل و ن); 3. Iqlab (Changing to Meem before ب); 4. Ikhfa (Hiding with nasal sound before the remaining 15 letters).",
            explanationUr = "نون ساکن اور تنوین کے چار بنیادی احکام ہیں: 1. اظہار (حروفِ حلقی کے سامنے بغیر غنہ کے ظاہر کرنا)؛ 2. ادغام (یرملون کے حروف میں ملانا)؛ 3. اقلاب (ب کے سامنے نون کو میم سے بدلنا)؛ 4. اخفاء (باقی 15 حروف کے سامنے ناک میں ہلکی آواز چھپانا)۔",
            explanationHi = "नून साकिना और तनवीन के 4 नियम हैं: इज़हार (हलक़ी अक्षरों से पहले बिना ग़ुन्ना पढ़ना), इदग़ाम (मिलाकर पढ़ना), इक़्लाब (नून को मीम में बदलना), और इख़्फ़ा (नाक में हल्की आवाज़ छिपाना)।",
            examples = listOf(
                TajweedExample("مِنْ حَكِيمٍ", "Surah Fussilat: 42", "Izhar: Noon Sakinah before letter Haa (ح)"),
                TajweedExample("مَن يَقُولُ", "Surah Al-Baqarah: 8", "Idgham with Ghunnah: Noon Sakinah before Yaa (ي)"),
                TajweedExample("مِن بَعْدِ", "Surah Al-Baqarah: 27", "Iqlab: Noon Sakinah changed to Meem before Baa (ب)"),
                TajweedExample("مِن قَبْلِ", "Surah Al-Baqarah: 25", "Ikhfa: Noon Sakinah hidden with nasalization before Qaaf (ق)")
            )
        ),
        TajweedRule(
            id = "qalqalah",
            titleEn = "Rule of Qalqalah (Echoing / Bouncing)",
            titleUr = "قاعدۂ قلقلہ (گونجدار آواز)",
            titleAr = "أحكام القلقلة",
            titleHi = "क़लक़ला का नियम (गूंजती आवाज़)",
            category = "Pronunciation",
            explanationEn = "When any of the five letters of Qutb Jadd (ق ط ب ج د) carries a Sukun (or you stop upon it), the articulation point is shaken to produce a sharp echoing bounce.",
            explanationUr = "جب حروفِ قلقلہ (ق، ط، ب، ج، د - قطب جد) پر جزم (سکون) ہو یا ان پر وقف کیا جائے، تو مخرج سے جھٹکے کے ساتھ گونج پیدا کی جاتی ہے۔",
            explanationHi = "जब पाँच अक्षरों (क़ुतब जद्द: क़, त़, ब, ज, द) पर सुकूं हो या वक़्फ़ किया जाए, तो मख़रज से हल्की गूंजती आवाज़ निकाली जाती है।",
            examples = listOf(
                TajweedExample("قُلْ هُوَ اللَّهُ أَحَدٌ ۝", "Surah Al-Ikhlas: 1", "Major Qalqalah at stop on letter Daal (د)"),
                TajweedExample("مِن فَلَقٍ ۝", "Surah Al-Falaq: 1", "Major Qalqalah at stop on letter Qaaf (ق)"),
                TajweedExample("فِي حَبْلٍ", "Surah Al-Masad: 5", "Minor Qalqalah in middle on letter Baa (ب)")
            )
        )
    )

    fun calculateZakat(input: ZakatCalculationInput): ZakatResult {
        val goldValue = input.goldGrams * input.goldGramPrice
        val silverValue = input.silverGrams * input.silverGramPrice
        val totalAssets = input.cashOnHand + input.bankBalance + goldValue + silverValue +
                input.businessInventory + input.receivables + input.otherInvestments

        val netWealth = (totalAssets - input.debtsAndLiabilities).coerceAtLeast(0.0)

        // Standard Nisab thresholds:
        // Gold Nisab = 87.48 grams (7.5 Tola)
        // Silver Nisab = 612.36 grams (52.5 Tola)
        val nisabThreshold = if (input.selectedNisabType == NisabType.GOLD) {
            87.48 * (if (input.goldGramPrice > 0) input.goldGramPrice else 75.0)
        } else {
            612.36 * (if (input.silverGramPrice > 0) input.silverGramPrice else 0.95)
        }

        val isEligible = netWealth >= nisabThreshold
        val zakatPayable = if (isEligible) netWealth * 0.025 else 0.0

        return ZakatResult(
            totalAssets = totalAssets,
            deductibleLiabilities = input.debtsAndLiabilities,
            netZakatableWealth = netWealth,
            nisabThreshold = nisabThreshold,
            isEligible = isEligible,
            zakatPayable = zakatPayable,
            currency = input.currency
        )
    }
}
