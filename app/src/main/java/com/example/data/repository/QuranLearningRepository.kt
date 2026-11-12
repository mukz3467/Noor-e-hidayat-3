package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*

object QuranLearningRepository {

    private const val PREFS_NAME = "noor_learning_academy_prefs"
    private const val KEY_COMPLETED_LESSONS = "completed_lesson_ids"
    private const val KEY_MEMORIZED_SURAHS = "memorized_surah_ids"
    private const val KEY_LEARNED_WORDS = "learned_word_ids"
    private const val KEY_QUIZZES_PASSED = "quizzes_passed_count"
    private const val KEY_LAST_MODULE = "last_active_module"
    private const val KEY_LAST_LESSON_TITLE = "last_lesson_title"
    private const val KEY_STREAK_DAYS = "streak_days"

    // ==========================================
    // 1. NOORANI QAIDA (8 Core Comprehensive Lessons)
    // ==========================================
    val qaidaLessons: List<QaidaLesson> = listOf(
        QaidaLesson(
            id = "qaida_1",
            lessonNumber = 1,
            titleEn = "Lesson 1: Individual Letters (Al-Mufradat)",
            titleUr = "سبق نمبر 1: مفردات (حروفِ تہجی)",
            titleAr = "الدرس الأول: الحروف المفردة",
            titleHi = "सबक़ 1: अलग-अलग अक्षर (मुफ़्रदात)",
            descriptionEn = "Recognize and articulate all 28 Arabic letters in their pure isolated forms with correct Makhraj.",
            descriptionUr = "تمام 28 عربی حروف کی الگ الگ شکلوں کی درست مخارج اور تلفظ کے ساتھ پہچان۔",
            descriptionAr = "التعرف على الحروف الهجائية الثمانية والعشرين بأشكالها المفردة ومخارجها الصحيحة.",
            descriptionHi = "सभी 28 अरबी अक्षरों को उनके सही उच्चारण (मख़ारिज) के साथ पहचानना।",
            items = listOf(
                QaidaItem("ا", "Alif", "Empty space of mouth & throat", "منہ اور حلق کا خالی حصہ", "مخرج الجوف", "मुँह और गले का खाली भाग"),
                QaidaItem("ب", "Baa", "Wet part of lips pressed together", "دونوں ہونٹوں کے تری والے حصے کو ملا کر", "من باطن الشفتين", "दोनों होंठों को मिलाकर"),
                QaidaItem("ت", "Taa", "Tip of tongue touching root of upper incisors", "زبان کی نوک اوپر کے سامنے والے دانتوں کی جڑ سے", "طرف اللسان مع أصول الثنايا العليا", "ज़बान की नोक ऊपर के दाँतों की जड़ से"),
                QaidaItem("ث", "Thaa", "Tip of tongue touching edge of upper incisors (Soft)", "زبان کا سرا اوپر کے دانتوں کے کنارے سے (نرمی کے ساتھ)", "طرف اللسان مع أطراف الثنايا العليا", "ज़बान का सिरा ऊपर के दाँतों के किनारे से"),
                QaidaItem("ج", "Jeem", "Middle of tongue touching hard palate", "زبان کا درمیانی حصہ تالو سے", "وسط اللسان مع ما يحاذيه من الحنك الأعلى", "ज़बान का बीच का हिस्सा तालू से"),
                QaidaItem("ح", "Haa", "Middle of the throat (Clear, breathy)", "حلق کے درمیانی حصے سے صاف", "وسط الحلق", "गले के बीच से साफ़ आवाज़"),
                QaidaItem("خ", "Khaa", "Upper throat nearest to mouth (Full mouth)", "حلق کا اوپری حصہ جو منہ کے قریب ہے (پر / موٹا)", "أدنى الحلق مما يلي الفم", "गले का ऊपरी हिस्सा (भारी आवाज़)"),
                QaidaItem("د", "Daal", "Tip of tongue touching root of upper incisors (Echoing)", "زبان کی نوک سامنے کے دانتوں کی جڑ سے (قلقلہ)", "طرف اللسان مع أصول الثنايا العليا", "ज़बान की नोک ऊपर के दाँतों की जड़ से"),
                QaidaItem("ذ", "Zhaal", "Tip of tongue touching edge of upper incisors (Soft)", "زبان کی نوک اوپر کے دانتوں کے کنارے سے (نرمی سے)", "طرف اللسان مع أطراف الثنايا العليا", "ज़बान की नोک दाँतों के किनारे से (नरमी से)"),
                QaidaItem("ر", "Raa", "Tip and back of tongue near gums (Vibrant)", "زبان کے سرے کی پشت مسوڑھوں سے", "طرف اللسان مع ما يحاذيه من لثة الثنايا العليا", "ज़बान का सिरा मसूड़ों से"),
                QaidaItem("ز", "Zaa", "Tip of tongue near lower teeth (Whistle sound)", "زبان کا سرا نچلے دانتوں کے اندرونی کنارے سے (سیٹی کی آواز)", "طرف اللسان مع فويق الثنايا السفلى (صفير)", "ज़बान का सिरा नीचे के दाँतों से (सीटी जैसी आवाज़)"),
                QaidaItem("س", "Seen", "Tip of tongue near lower teeth (Soft whistle)", "زبان کا سرا نچلے دانتوں سے (باریک سیٹی)", "طرف اللسان مع ما بين الثنايا العليا والسفلى", "हल्की सीटी जैसी आवाज़"),
                QaidaItem("ش", "Sheen", "Middle of tongue spreading sound in mouth", "زبان کا درمیانی حصہ، ہوا منہ میں پھیلتی ہے", "وسط اللسان مع ما يحاذيه من الحنك الأعلى", "ज़बान के बीच से हवा मुँह में फैलाकर"),
                QaidaItem("ص", "Saad", "Heavy whistling letter from tip of tongue", "زبان کے سرے سے موٹی سیٹی دار آواز (پر)", "طرف اللسان مع ما بين الثنايا (حرف مستعلٍ ومطبق)", "भारी सीटी की आवाज़"),
                QaidaItem("ض", "Daad", "Edge of tongue against upper molars (Unique to Arabic)", "زبان کی ایک یا دونوں کروٹیں اوپر والی داڑھوں سے", "إحدى حافتي اللسان أو كلتيهما مع الأضراس العليا", "ज़बान की करवट ऊपर की दाढ़ों से"),
                QaidaItem("ط", "Taa", "Heaviest letter touching root of incisors", "زبان کی نوک اوپر کے دانتوں کی جڑ سے (نہایت موٹا)", "طرف اللسان مع أصول الثنايا العليا (مستعل ومطبق)", "सबसे भारी आवाज़ दाँतों की जड़ से"),
                QaidaItem("ظ", "Zhaa", "Heavy soft letter touching edge of incisors", "زبان کی نوک اوپر کے دانتوں کے کنارے سے (موٹا اور نرم)", "طرف اللسان مع أطراف الثنايا العليا (مستعل ومطبق)", "भारी और नर्म आवाज़"),
                QaidaItem("ع", "Ayn", "Deep middle of the throat", "حلق کے بالکل درمیانی حصے سے", "وسط الحلق", "गले के बिल्कुल बीच से"),
                QaidaItem("غ", "Ghayn", "Upper throat nearest mouth (Heavy vibrating)", "اوپری حلق سے موٹی کھنکھار کے ساتھ", "أدنى الحلق", "गले के ऊपरी भाग से भारी आवाज़"),
                QaidaItem("ف", "Faa", "Bottom lip inner wet surface touching top incisors", "اوپر کے دانت نچلے ہونٹ کے اندرونی کنارے پر", "بطن الشفة السفلى مع أطراف الثنايا العليا", "ऊपरी दाँत निचले होंठ के अंदरूनी भाग पर"),
                QaidaItem("ق", "Qaaf", "Deepest back of tongue touching soft palate (Heavy)", "زبان کی جڑ تالو کے نرم کوے والے حصے سے (موٹا)", "أقصى اللسان مع ما يحاذيه من الحنك اللحمي", "ज़बान की जड़ नर्म तालू से (भारी)"),
                QaidaItem("ك", "Kaaf", "Back of tongue slightly forward of Qaaf (Light)", "زبان کی جڑ قاف سے ذرا آگے سخت تالو سے (باریک)", "أقصى اللسان مع ما يحاذيه من الحنك العظمي", "क़ाफ़ से थोड़ा आगे (हल्की आवाज़)"),
                QaidaItem("ل", "Laam", "Side and tip of tongue to upper gums", "زبان کے کنارے اور نوک سامنے والے مسوڑھوں سے", "أدنى حافة اللسان إلى منتهى طرفها مع لثة الثنايا", "ज़बान का सिरा ऊपर के मसूड़े से"),
                QaidaItem("م", "Meem", "Dry outer surfaces of both lips", "دونوں ہونٹوں کے خشکی والے حصے کو ملا کر", "بانطباق الشفتين", "दोनों होंठों के बाहरी हिस्से को मिलाकर"),
                QaidaItem("ن", "Noon", "Tip of tongue touching gums with nasal Ghunnah", "زبان کا سرا اوپر کے مسوڑھوں سے اور ناک سے آواز", "طرف اللسان مع لثة الثنايا مع الغنة من الخيشوم", "ज़बान की नोक मसूड़े से और नाक की आवाज़"),
                QaidaItem("و", "Waw", "Incomplete circle of both lips", "دونوں ہونٹوں کو گول کر کے بغیر ملائے", "بانضمام الشفتين دون انطباق تام", "होंठों को गोल करके"),
                QaidaItem("ه", "Haa", "Deepest base of throat near chest", "حلق کی انتہا سے سینے کے قریب", "أقصى الحلق مما يلي الصدر", "गले के बिल्कुल निचले हिस्से से"),
                QaidaItem("ي", "Yaa", "Middle of tongue touching palate", "زبان کا درمیانی حصہ تالو سے", "وسط اللسان مع ما يحاذيه من الحنك", "ज़बान के बीच से")
            )
        ),
        QaidaLesson(
            id = "qaida_2",
            lessonNumber = 2,
            titleEn = "Lesson 2: Compound Letters (Al-Murakkabat)",
            titleUr = "سبق نمبر 2: مرکبات (ملے جلے حروف)",
            titleAr = "الدرس الثاني: الحروف المركبة",
            titleHi = "सबक़ 2: मिले-जुले अक्षर (मुरक्कबात)",
            descriptionEn = "Learn how Arabic letters connect and change shapes at the beginning, middle, and end of words.",
            descriptionUr = "حروف کے باہمی ملاپ اور الفاظ کے شروع، درمیان اور آخر میں ان کی مختلف شکلوں کی مشق۔",
            descriptionAr = "كيفية اتصال الحروف وتغير أشكالها في أول الكلمة ووسطها وآخرها.",
            descriptionHi = "अक्षरों के मेल और शब्दों के शुरू, बीच और अंत में उनके बदलते रूपों का अभ्यास।",
            items = listOf(
                QaidaItem("لا", "Laam-Alif", "Laam merged with Alif", "لام اور الف کا ملاپ", "لام مع ألف", "लाम और अलिफ़ का जोड़"),
                QaidaItem("بل", "Baa-Laam", "Baa connecting to Laam", "با کو لام سے ملایا گیا", "باء متصلة بلام", "बा और लाम"),
                QaidaItem("تج", "Taa-Jeem", "Taa connecting to Jeem", "تا کو جیم سے ملایا گیا", "تاء مع جيم", "ता और जीम"),
                QaidaItem("نم", "Noon-Meem", "Noon connecting to Meem", "نون کو میم سے ملایا گیا", "نون مع ميم", "नून और मीम"),
                QaidaItem("خت", "Khaa-Taa", "Khaa connecting to Taa", "خا کو تا سے ملایا گیا", "خاء مع تاء", "ख़ा और ता"),
                QaidaItem("يـث", "Yaa-Thaa", "Yaa connecting to Thaa", "یا کو ثا سے ملایا گیا", "ياء مع ثاء", "या और सा"),
                QaidaItem("بلد", "Baa-Laam-Daal", "Three connected letters: Ba-La-Da", "تین حروف کا ملاپ: ب، ل، د", "ثلاثة أحرف: ب، ل، د", "तीन अक्षर: ब-ल-द"),
                QaidaItem("كتب", "Kaaf-Taa-Baa", "Three connected letters: Ka-Ta-Ba", "تین حروف کا ملاپ: ک، ت، ب", "ك، ت، ب", "क-त-ब")
            )
        ),
        QaidaLesson(
            id = "qaida_3",
            lessonNumber = 3,
            titleEn = "Lesson 3: Harakaat (Short Vowels: Fatha, Kasra, Damma)",
            titleUr = "سبق نمبر 3: حرکات (زبر، زیر، پیش)",
            titleAr = "الدرس الثالث: الحركات (الفتحة، الكسرة، الضمة)",
            titleHi = "सबक़ 3: हरकतें (ज़बर, ज़ेर, पेश)",
            descriptionEn = "Master short vowels pronounced briskly without stretching: Zabar (Fatha), Zer (Kasra), and Pesh (Damma).",
            descriptionUr = "حرکات کو بغیر کھینچے جھٹکے کے بغیر جلدی ادا کرنے کی مشق: زبر، زیر اور پیش۔",
            descriptionAr = "إتقان الحركات الثلاث (الفتحة والكسرة والضمة) دون مد أو تمطيط.",
            descriptionHi = "ज़बर (َ), ज़ेर (ِ), और पेश (ُ) को बिना खींचे जल्दी पढ़ने का अभ्यास।",
            items = listOf(
                QaidaItem("أَ", "A", "Alif with Fatha (Open mouth upward)", "الف پر زبر: آواز اوپر اٹھتی ہے", "همزة مفتوحة", "अलिफ़ पर ज़बर"),
                QaidaItem("إِ", "I", "Alif with Kasra (Drop jaw gently)", "الف پر زیر: جبڑا نیچے گرتا ہے", "همزة مكسورة", "अलिफ़ पर ज़ेर"),
                QaidaItem("أُ", "U", "Alif with Damma (Circle lips forward)", "الف پر پیش: دونوں ہونٹ گول ہوتے ہیں", "همزة مضمومة", "अलिफ़ पर पेश"),
                QaidaItem("بَ بِ بُ", "Ba - Bi - Bu", "Baa with three Harakaat", "با پر زبر، زیر، پیش", "بَ بِ بُ", "बَ, बि, बु"),
                QaidaItem("تَ تِ تُ", "Ta - Ti - Tu", "Taa with three Harakaat", "تا پر زبر، زیر، پیش", "تَ تِ تُ", "तَ, ति, तु"),
                QaidaItem("دَ رَ سَ", "Da-Ra-Sa", "Practice word with 3 Fathas", "تینوں پر زبر: دَرَسَ", "دَرَسَ", "द-र-स"),
                QaidaItem("سَمِعَ", "Sa-Mi-'A", "Word combining Fatha, Kasra, Fatha", "زبر، زیر، زبر: سَمِعَ", "سَمِعَ", "स-मि-अ")
            )
        ),
        QaidaLesson(
            id = "qaida_4",
            lessonNumber = 4,
            titleEn = "Lesson 4: Tanween (Double Vowels: ً ٍ ٌ)",
            titleUr = "سبق نمبر 4: تنوین (دو زبر، دو زیر، دو پیش)",
            titleAr = "الدرس الرابع: التنوين (فتحتان، كسرتان، ضمتان)",
            titleHi = "सबक़ 4: तनवीन (दो ज़बर, दो ज़ेर, दो पेश)",
            descriptionEn = "Understand Tanween as a hidden Noon Sakinah at the end of nouns: An (ً), In (ٍ), Un (ٌ).",
            descriptionUr = "تنوین کی حقیقت یعنی لفظ کے آخر میں نون ساکن کی آواز: دو زبر، دو زیر، دو پیش۔",
            descriptionAr = "التنوين نون ساكنة زائدة تلحق آخر الأسماء لفظاً لا خطاً.",
            descriptionHi = "शब्द के अंत में छिपी हुई नून की आवाज़: अन् (ً), इन् (ٍ), उन् (ٌ)।",
            items = listOf(
                QaidaItem("بً", "Ban", "Baa with Tanween Fatha (Two Zabar)", "با پر دو زبر: بَ + نْ = بً", "باء منونة بالفتح", "बा पर दो ज़बर"),
                QaidaItem("بٍ", "Bin", "Baa with Tanween Kasra (Two Zer)", "با پر دو زیر: بِ + نْ = بٍ", "باء منونة بالكسر", "बा पर दो ज़ेर"),
                QaidaItem("بٌ", "Bun", "Baa with Tanween Damma (Two Pesh)", "با پر دو پیش: بُ + نْ = بٌ", "باء منونة بالضم", "बा पर दो पेश"),
                QaidaItem("عَلِيمًا", "'Aleeman", "Word example with Tanween Fatha", "قرآنی مثال: عَلِيمًا", "عَلِيمًا", "अ़लीमन"),
                QaidaItem("حَكِيمٍ", "Hakeemin", "Word example with Tanween Kasra", "قرآنی مثال: حَكِيمٍ", "حَكِيمٍ", "हकीमिन"),
                QaidaItem("غَفُورٌ", "Ghafoorun", "Word example with Tanween Damma", "قرآنی مثال: غَفُورٌ", "غَفُورٌ", "ग़फ़ूरुन")
            )
        ),
        QaidaLesson(
            id = "qaida_5",
            lessonNumber = 5,
            titleEn = "Lesson 5: Sukoon / Jazm (ْ)",
            titleUr = "سبق نمبر 5: جزم / سکون (ْ)",
            titleAr = "الدرس الخامس: السكون (ْ)",
            titleHi = "सबक़ 5: सुकूं / जज़्म (ْ)",
            descriptionEn = "The Sukoon sign indicates that a letter has no vowel and joins the preceding letter firmly.",
            descriptionUr = "جزم (سکون) والا حرف اپنے سے پہلے متحرک حرف سے مل کر ادا ہوتا ہے۔",
            descriptionAr = "السكون علامة خلو الحرف من الحركات، ويتصل بالحرف السابق.",
            descriptionHi = "सुकूं का निशान बताता है कि अक्षर पर कोई हरकत नहीं है और वह पिछले अक्षर से जुड़ता है।",
            items = listOf(
                QaidaItem("أَبْ", "Ab", "Hamzah with Fatha connected to Baa Sakinah", "الف پر زبر، با پر جزم: أَبْ", "همزة مفتوحة مع باء ساكنة", "अलिफ़ ज़बर बा जज़्म = अब"),
                QaidaItem("قُلْ", "Qul", "Qaaf with Damma connected to Laam Sakinah", "قاف پر پیش، لام پر جزم: قُلْ", "قاف مضمومة مع لام ساكنة", "क़ुल"),
                QaidaItem("مِنْ", "Min", "Meem with Kasra connected to Noon Sakinah", "میم پر زیر، نون پر جزم: مِنْ", "ميم مكسورة مع نون ساكنة", "मिन"),
                QaidaItem("الْحَمْدُ", "Al-Hamdu", "Connected word with Sukoon on Laam and Meem", "قرآنی مثال: الْحَمْدُ", "الْحَمْدُ", "अल-हम्दु")
            )
        ),
        QaidaLesson(
            id = "qaida_6",
            lessonNumber = 6,
            titleEn = "Lesson 6: Tashdeed / Shaddah (ّ)",
            titleUr = "سبق نمبر 6: تشدید (ّ)",
            titleAr = "الدرس السادس: الشدة (التشديد)",
            titleHi = "सबक़ 6: तशदीद (ّ)",
            descriptionEn = "A letter with Tashdeed is pronounced twice: first with Sukoon, then with its Harakah firmly.",
            descriptionUr = "مشدد حرف دو مرتبہ پڑھا جاتا ہے: پہلا ساکن اور دوسرا متحرک، مضبوطی کے ساتھ۔",
            descriptionAr = "الحرف المشدد حرفان أولهما ساكن والثاني متحرك يدغمان معاً.",
            descriptionHi = "तशदीद वाला अक्षर दो बार पढ़ा जाता है: पहला सुकूं के साथ और दूसरा हरकत के साथ।",
            items = listOf(
                QaidaItem("رَبِّ", "Rabbi", "Baa is doubled: Rab + bi = Rabbi", "با مشدد ہے: رَبْ + بِ = رَبِّ", "رَبِّ (باء مشددة مكسورة)", "रब्बि"),
                QaidaItem("إِنَّ", "Inna", "Noon with Tashdeed carries compulsory 2-count Ghunnah", "نون مشدد پر واجب غنہ ہوتا ہے", "إِنَّ (نون مشددة مع غنة)", "इन्ना (नाक में ग़ुन्ना)"),
                QaidaItem("ثُمَّ", "Thumma", "Meem with Tashdeed carries compulsory 2-count Ghunnah", "میم مشدد پر واجب غنہ ہوتا ہے", "ثُمَّ (ميم مشددة مع غنة)", "सुम्मा (ग़ुन्ना के साथ)"),
                QaidaItem("اللَّه", "Allah", "The supreme Majestic Name with Tashdeed on Laam", "اسمِ جلالت: اللَّه", "لفظ الجلالة: اللَّه", "अल्लाह")
            )
        ),
        QaidaLesson(
            id = "qaida_7",
            lessonNumber = 7,
            titleEn = "Lesson 7: Madd (Elongation: ٓ)",
            titleUr = "سبق نمبر 7: مد (حروفِ مدہ و طوالت)",
            titleAr = "الدرس السابع: أحكام المدود",
            titleHi = "सबक़ 7: मद (आवाज़ को खींचना)",
            descriptionEn = "Elongating sound using Madd letters (Alif, Waw, Yaa) from 2 up to 4, 5, or 6 counts.",
            descriptionUr = "حروفِ مدہ (الف، واو، یا) کے احکام اور 2 سے لے کر 4، 5 یا 6 حرکات تک لمبا کرنا۔",
            descriptionAr = "إطالة الصوت بحرف من حروف المد الثلاثة بمقدار حركتين إلى ست حركات.",
            descriptionHi = "मद के अक्षरों (अलिफ़, वाव, या) से आवाज़ को 2 से 6 हरकतों तक खींचना।",
            items = listOf(
                QaidaItem("قَالَ", "Qaala", "Natural Madd (Asli / Tabee'i - 2 counts)", "مدِ اصلی (2 حرکات): قَالَ", "مد طبيعي (حركتان)", "क़ा-ला (2 हरकत)"),
                QaidaItem("يَقُولُ", "Yaqoolu", "Natural Madd with Waw Sakinah", "واو مدہ کی مثال: يَقُولُ", "واو مدية طبيعية", "यक़ूलु"),
                QaidaItem("قِيلَ", "Qeela", "Natural Madd with Yaa Sakinah", "یا مدہ کی مثال: قِيلَ", "ياء مدية طبيعية", "क़ीला"),
                QaidaItem("جَاءَ", "Jaaa'a", "Connected Madd (Madd Muttasil - 4 to 5 counts)", "مدِ متصل (ایک ہی کلمہ میں مد اور ہمزہ - 4 تا 5 حرکات)", "مد متصل (4-5 حركات)", "जाऽऽ-अ (4-5 हरकत)"),
                QaidaItem("وَلَا الضَّالِّينَ", "Wa-lad-Daaalleen", "Heavy Compulsory Madd (Madd Lazim - 6 counts)", "مدِ لازم کلمی مثقل (6 حرکات کھینچنا واجب)", "مد لازم كلمي مثقل (6 حركات)", "व-लद्दाऽऽऽल्लीन (6 हरकत)")
            )
        ),
        QaidaLesson(
            id = "qaida_8",
            lessonNumber = 8,
            titleEn = "Lesson 8: Quranic Word & Ayah Reading",
            titleUr = "سبق نمبر 8: قرآنی کلمات اور آیات کی روانی",
            titleAr = "الدرس الثامن: قراءة الكلمات والآيات القرآنية",
            titleHi = "सबक़ 8: क़ुरआनी शब्द और आयतों का पठन",
            descriptionEn = "Combine all previous rules to read authentic Quranic sentences fluently.",
            descriptionUr = "پچھلے تمام اسباق کے احکام کو ملا کر قرآنی کلمات اور مکمل آیات کی باقاعدہ روانی سے تلاوت۔",
            descriptionAr = "تطبيق جميع القواعد السابقة لقراءة الكلمات والجمل القرآنية بطلاقة.",
            descriptionHi = "सभी नियमों को मिलाकर क़ुरआनी शब्दों और आयतों को सही तरीक़े से पढ़ना।",
            items = listOf(
                QaidaItem("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "Bismillahir Rahmanir Raheem", "Opening of the Quran", "شروع اللہ کے نام سے جو بے حد مہربان نہایت رحم والا ہے", "البسملة الكريمة", "बिस्मिल्लाहिर रहमानिर रहीम"),
                QaidaItem("الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Al-Hamdu Lillahi Rabbil 'Aalameen", "Surah Al-Fatihah, Ayah 2", "سب تعریفیں اللہ کے لیے ہیں جو تمام جہانوں کا رب ہے", "الفاتحة: 2", "अल-हम्दु लिल्लाहि रब्बिल आलमीन"),
                QaidaItem("إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "Iyyaaka Na'budu Wa Iyyaaka Nasta'een", "Surah Al-Fatihah, Ayah 5", "ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں", "الفاتحة: 5", "इय्याक नअबुदु व इय्याक नस्तईन")
            )
        )
    )

    // ==========================================
    // 2. TAJWEED ADVANCED RULES
    // ==========================================
    val advancedTajweedRules: List<TajweedRule> = listOf(
        TajweedRule(
            id = "makharij",
            titleEn = "Makharij-ul-Huroof (5 Main Articulation Areas)",
            titleUr = "مخارج الحروف (پانچ بنیادی اعضاءِ تکلم)",
            titleAr = "مخارج الحروف الخمسة العامة",
            titleHi = "मख़ारिज-उल-हुरूफ (5 मुख्य उच्चारण क्षेत्र)",
            category = "Foundations",
            explanationEn = "There are 5 major regions where letters originate: 1. Al-Jawf (Empty space in mouth/throat - 3 Madd letters); 2. Al-Halq (Throat - 6 throat letters); 3. Al-Lisaan (Tongue - 18 letters); 4. Ash-Shafatayn (Lips - 4 letters); 5. Al-Khayshoom (Nasal cavity - Ghunnah sound).",
            explanationUr = "مخارج کے 5 بڑے علاقے ہیں: 1. الجوف (منہ اور حلق کا خالی خلا - حروفِ مدہ)؛ 2. الحلق (گلا - 6 حلقی حروف)؛ 3. اللسان (زبان - 18 حروف)؛ 4. الشفتان (ہونٹ - 4 حروف)؛ 5. الخيشوم (ناک کا بانسہ - غنہ کی آواز)۔",
            explanationHi = "अक्षरों के निकलने के 5 मुख्य क्षेत्र हैं: 1. अल-जौफ़ (खाली स्थान); 2. अल-हलक़ (गला); 3. अल-लिसान (ज़बान); 4. अश-शफ़तैन (होंठ); 5. अल-ख़ैशूम (नाक की हड्डी - ग़ुन्ना)।",
            examples = listOf(
                TajweedExample("قَالَ - يَقُولُ - قِيلَ", "Al-Jawf", "Three Madd letters from the oral cavity"),
                TajweedExample("ء ه ع ح غ خ", "Al-Halq", "Six throat letters from lower, middle and upper throat"),
                TajweedExample("ب م و ف", "Ash-Shafatayn", "Four lip letters")
            )
        ),
        TajweedRule(
            id = "noon_sakin_tanween",
            titleEn = "Noon Sakinah & Tanween (4 Rules: Izhar, Idgham, Iqlab, Ikhfa)",
            titleUr = "احکامِ نون ساکن و تنوین (اظہار، ادغام، اقلاب، اخفاء)",
            titleAr = "أحكام النون الساكنة والتنوين",
            titleHi = "नून साकिना और तनवीन के चार नियम",
            category = "Core Tajweed",
            explanationEn = "1. Izhar: Clearly pronounce Noon before 6 throat letters (ء ه ع ح غ خ). 2. Idgham: Merge Noon into Yarmaloon (ي ر م ل و ن). 3. Iqlab: Turn Noon into Meem before Baa (ب). 4. Ikhfa: Hide Noon with nasal sound before remaining 15 letters.",
            explanationUr = "1. اظہار: حلق کے 6 حروف کے سامنے نون کو بالکل ظاہر پڑھنا؛ 2. ادغام: یرملون کے حروف میں ملا کر پڑھنا؛ 3. اقلاب: ب سے پہلے نون کو میم سے بدلنا؛ 4. اخفاء: باقی 15 حروف کے سامنے ناک میں ہلکی آواز چھپانا۔",
            explanationHi = "1. इज़हार: साफ़ पढ़ना; 2. इदग़ाम: मिलाकर पढ़ना; 3. इक़्लाब: नून को मीम में बदलना; 4. इख़्फ़ा: नाक में आवाज़ छिपाना।",
            examples = listOf(
                TajweedExample("مِنْ حَكِيمٍ", "Izhar", "Noon Sakinah clearly articulated before Haa (ح)"),
                TajweedExample("مَن يَقُولُ", "Idgham with Ghunnah", "Noon Sakinah merged into Yaa with nasalization"),
                TajweedExample("مِن بَعْدِ", "Iqlab", "Noon Sakinah converted to silent Meem before Baa"),
                TajweedExample("مِن قَبْلِ", "Ikhfa", "Noon Sakinah hidden with nasal sound before Qaaf")
            )
        ),
        TajweedRule(
            id = "meem_sakin",
            titleEn = "Rules of Meem Sakinah (3 Rules)",
            titleUr = "احکامِ میم ساکن (ادغام، اخفاء، اظہارِ شفوی)",
            titleAr = "أحكام الميم الساكنة (الإدغام، الإخفاء، الإظهار الشفوي)",
            titleHi = "मीम साकिना के तीन नियम",
            category = "Core Tajweed",
            explanationEn = "Meem Sakinah (مْ) has three rules: 1. Idgham Shafawi (When followed by another Meem, merge with 2-count Ghunnah); 2. Ikhfa Shafawi (When followed by Baa, hide lips lightly with Ghunnah); 3. Izhar Shafawi (When followed by any other letter, pronounce clearly without Ghunnah).",
            explanationUr = "میم ساکن کے تین احکام ہیں: 1. ادغامِ شفوی (جب میم کے بعد دوسرا میم آئے تو غنہ کے ساتھ ملائیں)؛ 2. اخفائے شفوی (جب میم کے بعد با آئے تو ہونٹوں کو نرمی سے ملا کر ناک میں چھپائیں)؛ 3. اظہارِ شفوی (باقی تمام حروف کے سامنے میم کو بغیر غنہ کے صاف پڑھیں)۔",
            explanationHi = "मीम साकिना के 3 नियम: 1. इदग़ाम शफ़वी (मीम के बाद मीम आने पर ग़ुन्ना के साथ मिलाना); 2. इख़्फ़ा शफ़वी (मीम के बाद बा आने पर होंठ हल्के से मिलाना); 3. इज़हार शफ़वी (बाक़ी सभी अक्षरों से पहले साफ़ पढ़ना)।",
            examples = listOf(
                TajweedExample("لَهُم مَّا يَشَاءُونَ", "Idgham Shafawi", "Meem Sakin followed by Meem with Ghunnah"),
                TajweedExample("تَرْمِيهِم بِحِجَارَةٍ", "Ikhfa Shafawi", "Meem Sakin followed by Baa with soft hidden lips"),
                TajweedExample("أَنعَمتَ عَلَيهِم", "Izhar Shafawi", "Meem Sakin pronounced clearly before Taa")
            )
        ),
        TajweedRule(
            id = "qalqalah_advanced",
            titleEn = "Qalqalah (Echoing Bounce: ق ط ب ج د)",
            titleUr = "قاعدۂ قلقلہ (قطب جد کی گونج)",
            titleAr = "أحكام القلقلة ومراتبها",
            titleHi = "क़लक़ला (गूंजती आवाज़: क़, त़, ब, ज, द)",
            category = "Pronunciation",
            explanationEn = "When any of Qutb Jadd (ق ط ب ج د) carries a Sukun or you stop on it: 1. Qalqalah Sughra (Minor bounce in the middle of a word); 2. Qalqalah Kubra (Strong bounce when stopping at end of verse); 3. Qalqalah Akbar (Heaviest bounce when stopping on a Tashdeed letter).",
            explanationUr = "حروفِ قطب جد (ق، ط، ب، ج، د) جب ساکن ہوں: 1. صغریٰ (کلمہ کے درمیان میں ہلکی گونج)؛ 2. کبریٰ (آیت کے آخر میں وقف پر مضبوط گونج)؛ 3. اکبر (تشدید والے حرف پر وقف کرتے وقت سب سے شدید گونج)۔",
            explanationHi = "जब पाँच अक्षर (क़ुतब जद्द) साकिन हों: 1. सुग़रा (शब्द के बीच में हल्की गूंज); 2. कुबरा (वक़्फ़ पर मज़बूत गूंज); 3. अकबर (तशदीद पर रुकते समय सबसे गहरी गूंज)।",
            examples = listOf(
                TajweedExample("يَطْمَعُ", "Qalqalah Sughra", "Letter Taa in middle of word"),
                TajweedExample("قُلْ هُوَ اللَّهُ أَحَدٌ ۝", "Qalqalah Kubra", "Stopping on Daal at end of Ayah"),
                TajweedExample("تَبَّتْ يَدَا أَبِي لَهَبٍ وَتَبَّ ۝", "Qalqalah Akbar", "Stopping on doubled Baa with Tashdeed")
            )
        ),
        TajweedRule(
            id = "waqf_ibtida",
            titleEn = "Waqf & Ibtida (Rules of Stopping & Starting)",
            titleUr = "رموزِ اوقاف و ابتدا (وقف اور دوبارہ شروع کرنے کے اصول)",
            titleAr = "أحكام الوقف والابتداء وعلامات المصحف",
            titleHi = "वक़्फ़ और इब्तिदा (रुकने और शुरू करने के नियम)",
            category = "Reading Mastery",
            explanationEn = "Common stopping signs in the Quran: مـ (Compulsory Stop); ط (Absolute Stop); ج (Permissible Stop); ز (Better to continue); ص (Permitted to breathe); قلى (Stopping is better); صلى (Continuing is better); لا (Do not stop unless at Ayah end).",
            explanationUr = "قرآن کریم کے رموزِ اوقاف: مـ (وقفِ لازم، رکنا ضروری)؛ ط (وقفِ مطلق)؛ ج (وقفِ جائز)؛ ز (وقفِ مجوز، ملانا بہتر)؛ ص (وقفِ مرخص)؛ قلى (وقف بہتر ہے)؛ صلى (وصل بہتر ہے)؛ لا (یہاں نہ رکیں)۔",
            explanationHi = "क़ुरआन के रुकने के निशान: مـ (रुकना ज़रूरी); ط (मुकम्मल वक़्फ़); ج (रुकना जायज़); قلى (रुकना बेहतर); صلى (मिलाकर पढ़ना बेहतर); لا (यहाँ न रुकें)।",
            examples = listOf(
                TajweedExample("فَلَا يَحْزُنكَ قَوْلُهُمْ ۘ إِنَّا نَعْلَمُ", "Waqf Lazim (مـ)", "Must stop to prevent altering the sacred meaning"),
                TajweedExample("ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى", "Mu'anaqah (Three dots)", "Stop at one mark, not both")
            )
        )
    )

    // ==========================================
    // 3. QURANIC ARABIC VOCABULARY (80% Most Frequent Words)
    // ==========================================
    val quranicVocabulary: List<QuranicWord> = listOf(
        QuranicWord("voc_1", "مِنْ", "Min", "From / Of", "سے / میں سے", "من / ابتداء الغاية", "से / में से", 3226, "Preposition", "مِّنَ الْمُؤْمِنِينَ رِجَالٌ", "Al-Ahzab: 23"),
        QuranicWord("voc_2", "فِي", "Fee", "In / Inside", "میں / اندر", "في / الظرفية", "में / अंदर", 1701, "Preposition", "فِي قُلُوبِهِم مَّرَضٌ", "Al-Baqarah: 10"),
        QuranicWord("voc_3", "اللَّه", "Allah", "Allah (The One True God)", "اللہ تعالیٰ", "الله جل جلاله", "अल्लाह", 2699, "Proper Noun", "إِنَّ اللَّهَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Al-Baqarah: 20"),
        QuranicWord("voc_4", "عَلَى", "'Alaa", "On / Upon", "پر / اوپر", "على / الاستعلاء", "पर / ऊपर", 1445, "Preposition", "عَلَىٰ هُدًى مِّن رَّبِّهِمْ", "Al-Baqarah: 5"),
        QuranicWord("voc_5", "إِلَى", "Ilaa", "To / Towards", "کی طرف / تک", "إلى / انتهاء الغاية", "की तरफ़ / तक", 742, "Preposition", "وَإِلَى اللَّهِ تُرْجَعُ الْأُمُورُ", "Al-Baqarah: 210"),
        QuranicWord("voc_6", "هُوَ", "Huwa", "He / It", "وہ (مذکر)", "هو (ضمير غائب)", "वह (पुल्लिंग)", 481, "Pronoun", "قُلْ هُوَ اللَّهُ أَحَدٌ", "Al-Ikhlas: 1"),
        QuranicWord("voc_7", "الَّذِي", "Al-Ladhee", "The one who / Which", "وہ جو کہ / جس نے", "الذي (اسم موصول)", "वह जो / जिसने", 1464, "Relative Pronoun", "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ", "Al-Mulk: 2"),
        QuranicWord("voc_8", "قَالَ", "Qaala", "He said", "اس نے کہا", "قال (فعل ماضٍ)", "उसने कहा", 1722, "Verb", "قَالَ إِنِّي عَبْدُ اللَّهِ", "Maryam: 30"),
        QuranicWord("voc_9", "كَانَ", "Kaana", "He was / Existed", "وہ تھا / ہے", "كان (فعل ماضٍ ناقص)", "वह था", 1358, "Verb", "وَكَانَ اللَّهُ عَلِيمًا حَكِيمًا", "An-Nisa: 17"),
        QuranicWord("voc_10", "آمَنَ", "Aamana", "He believed", "وہ ایمان لایا", "آمن (فعل ماضٍ)", "वह ईमान लाया", 537, "Verb", "آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ", "Al-Baqarah: 285"),
        QuranicWord("voc_11", "رَبّ", "Rabb", "Lord / Sustainer / Cherisher", "پالنے والا رب", "الرب / المربي والمالك", "पालने वाला रब", 975, "Noun", "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Al-Fatihah: 2"),
        QuranicWord("voc_12", "يَوْم", "Yawm", "Day", "دن / روز", "يوم (الزمن)", "दिन / रोज़", 405, "Noun", "مَالِكِ يَوْمِ الدِّينِ", "Al-Fatihah: 4"),
        QuranicWord("voc_13", "جَنَّة", "Jannah", "Paradise / Garden", "جنت / باغ", "الجنة دار النعيم", "जन्नत / स्वर्ग", 147, "Noun", "أُولَٰئِكَ أَصْحَابُ الْجَنَّةِ", "Al-Baqarah: 82"),
        QuranicWord("voc_14", "نَار", "Naar", "Fire / Hellfire", "آگ / جہنم", "النار دار العذاب", "आग / जहन्नम", 145, "Noun", "فَاتَّقُوا النَّارَ الَّتِي وَقُودُهَا النَّاسُ", "Al-Baqarah: 24"),
        QuranicWord("voc_15", "كِتَاب", "Kitaab", "Book / Scripture", "کتاب / فرمان", "الكتاب / القرآن", "किताब / ग्रन्थ", 261, "Noun", "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ فِيهِ", "Al-Baqarah: 2")
    )

    // ==========================================
    // 4. WORD-BY-WORD QURAN TRANSLATION (Authentic Verified Datasets)
    // ==========================================
    val wordByWordSurahFatihah: List<WordByWordAyah> = listOf(
        WordByWordAyah(
            surahNumber = 1,
            ayahNumber = 1,
            fullArabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            translationEn = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
            translationUr = "شروع اللہ کے نام سے جو سب پر مہربان، بہت رحم فرمانے والا ہے۔",
            translationHi = "अल्लाह के नाम से जो सब पर मेहरबान, बहुत रहम करने वाला है।",
            words = listOf(
                WordPart("بِسْمِ", "Bismi", "In the name", "نام کے ساتھ", "بالاسم", "नाम से"),
                WordPart("اللَّهِ", "Allahi", "of Allah", "اللہ کے", "الله", "अल्लाह के"),
                WordPart("الرَّحْمَٰنِ", "Ar-Rahmaani", "the Entirely Merciful", "جو بڑا مہربان ہے", "ذو الرحمة الشاملة", "जो बड़ा मेहरबान है"),
                WordPart("الرَّحِيمِ", "Ar-Raheemi", "the Especially Merciful", "نہایت رحم والا", "الدائم الرحمة", "बहुत रहम वाला")
            )
        ),
        WordByWordAyah(
            surahNumber = 1,
            ayahNumber = 2,
            fullArabic = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
            translationEn = "[All] praise is [due] to Allah, Lord of the worlds.",
            translationUr = "سب تعریفیں اللہ کے لیے ہیں جو تمام جہانوں کا پالنے والا ہے۔",
            translationHi = "सब तारीफ़ें अल्लाह के लिए हैं जो सारे जहाँ का रब है।",
            words = listOf(
                WordPart("الْحَمْدُ", "Al-Hamdu", "All praise", "سب تعریفیں", "كل المحامد والشكر", "सब तारीफ़ें"),
                WordPart("لِلَّهِ", "Lillahi", "is for Allah", "اللہ کے لیے ہیں", "خالصة لله", "अल्लाह के लिए हैं"),
                WordPart("رَبِّ", "Rabbi", "Lord and Sustainer", "جو رب ہے", "خالق ومربي", "जो रब है"),
                WordPart("الْعَالَمِينَ", "Al-'Aalameen", "of all worlds / creation", "تمام جہانوں کا", "جميع الخلائق", "सारे जहाँ का")
            )
        ),
        WordByWordAyah(
            surahNumber = 1,
            ayahNumber = 3,
            fullArabic = "الرَّحْمَٰنِ الرَّحِيمِ",
            translationEn = "The Entirely Merciful, the Especially Merciful,",
            translationUr = "جو بے حد مہربان اور نہایت رحم فرمانے والا ہے،",
            translationHi = "जो बड़ा मेहरबान और बहुत रहम वाला है,",
            words = listOf(
                WordPart("الرَّحْمَٰنِ", "Ar-Rahmaani", "The Entirely Merciful", "بہت مہربان", "واسع الرحمة", "बड़ा मेहरबान"),
                WordPart("الرَّحِيمِ", "Ar-Raheemi", "The Especially Merciful", "نہایت رحم والا", "الراحم بعباده", "बहुत रहम वाला")
            )
        ),
        WordByWordAyah(
            surahNumber = 1,
            ayahNumber = 4,
            fullArabic = "مَالِكِ يَوْمِ الدِّينِ",
            translationEn = "Sovereign of the Day of Recompense.",
            translationUr = "بدلے کے دن کا تنہا مالک ہے۔",
            translationHi = "बदले के दिन का अकेला मालिक है।",
            words = listOf(
                WordPart("مَالِكِ", "Maaliki", "Master / Sovereign", "مالک ہے", "صاحب الأمر والملك", "मालिक है"),
                WordPart("يَوْمِ", "Yawmi", "of the Day", "دن کا", "يوم", "दिन का"),
                WordPart("الدِّينِ", "Ad-Deeni", "of Recompense / Judgment", "بدلے و جزا کے", "الجزاء والحساب", "बदले और न्याय के")
            )
        ),
        WordByWordAyah(
            surahNumber = 1,
            ayahNumber = 5,
            fullArabic = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
            translationEn = "It is You we worship and You we ask for help.",
            translationUr = "ہم صرف تیری ہی عبادت کرتے ہیں اور صرف تجھ ہی سے مدد مانگتے ہیں۔",
            translationHi = "हम तेरी ही इबादत करते हैं और तुझ ही से मदद माँगते हैं।",
            words = listOf(
                WordPart("إِيَّاكَ", "Iyyaaka", "You alone", "صرف تیری ہی", "نخصك وحدك", "सिर्फ़ तेरी ही"),
                WordPart("نَعْبُدُ", "Na'budu", "we worship", "ہم بندگی کرتے ہیں", "نطيع ونخضع", "हम इबादत करते हैं"),
                WordPart("وَإِيَّاكَ", "Wa-Iyyaaka", "and You alone", "اور صرف تجھ ہی سے", "وإياك وحدك", "और सिर्फ़ तुझ ही से"),
                WordPart("نَسْتَعِينُ", "Nasta'een", "we ask for help", "ہم مدد طلب کرتے ہیں", "نطلب العون", "हम मदद माँगते हैं")
            )
        ),
        WordByWordAyah(
            surahNumber = 1,
            ayahNumber = 6,
            fullArabic = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
            translationEn = "Guide us to the straight path -",
            translationUr = "ہمیں سیدھے راستے کی ہدایت فرما،",
            translationHi = "हमें सीधे रास्ते की हिदायत दे,",
            words = listOf(
                WordPart("اهْدِنَا", "Ihdinaa", "Guide us", "ہمیں چلا / رہنمائی فرما", "وفقنا وأرشدنا", "हमें रास्ता दिखा"),
                WordPart("الصِّرَاطَ", "As-Siraata", "the path", "راستے پر", "الطريق والمنهج", "रास्ते पर"),
                WordPart("الْمُسْتَقِيمَ", "Al-Mustaqeema", "the straight", "جو بالکل سیدھا ہے", "القويم الذي لا اعوجاج فيه", "जो बिल्कुल सीधा है")
            )
        ),
        WordByWordAyah(
            surahNumber = 1,
            ayahNumber = 7,
            fullArabic = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
            translationEn = "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.",
            translationUr = "ان لوگوں کے راستے پر جن پر تو نے انعام فرمایا، جن پر نہ غضب ہوا اور نہ وہ گمراہ ہوئے۔",
            translationHi = "उन लोगों के रास्ते पर जिन पर तूने इनाम किया, जिन पर न ग़ज़ब हुआ और न वे भटके।",
            words = listOf(
                WordPart("صِرَاطَ", "Siraata", "Path of", "راستہ", "طريق", "रास्ता"),
                WordPart("الَّذِينَ", "Al-Ladheena", "those whom", "ان لوگوں کا", "الذين", "उन लोगों का"),
                WordPart("أَنْعَمْتَ", "An'amta", "You have favored", "تو نے انعام کیا", "أنعمت بالهداية", "तूने इनाम दिया"),
                WordPart("عَلَيْهِمْ", "'Alayhim", "upon them", "ان پر", "عليهم", "उन पर"),
                WordPart("غَيْرِ", "Ghayri", "not of", "نہ کہ", "سوى", "न कि"),
                WordPart("الْمَغْضُوبِ", "Al-Maghdoobi", "those who earned wrath", "جن پر غضب ہوا", "المغضوب عليهم", "जिन पर ग़ज़ब हुआ"),
                WordPart("عَلَيْهِمْ", "'Alayhim", "upon them", "ان پر", "عليهم", "उन पर"),
                WordPart("وَلَا", "Wa-Laa", "and nor of", "اور نہ", "ولا", "और न"),
                WordPart("الضَّالِّينَ", "Ad-Daalleen", "those who are astray", "جو بھٹک گئے", "التائهين عن الحق", "जो भटके हुए हैं")
            )
        )
    )

    // ==========================================
    // 5. SURAH INTRODUCTIONS & AUTHENTIC ASBAB-E-NUZOOL
    // ==========================================
    val surahIntroductions: List<SurahExegesis> = listOf(
        SurahExegesis(
            surahNumber = 1,
            nameArabic = "سُورَةُ الْفَاتِحَةِ",
            nameEnglish = "Surah Al-Fatihah (The Opening)",
            revelationPlace = "Makki (Early Makkah period)",
            asbabNuzoolEn = "Revealed as the complete opening of the Quran and the indispensable pillar of every Salaah. Hadith reports that Jibreel (AS) descended with an angel who had never visited Earth before, carrying the glad tidings of two lights: Al-Fatihah and the closing verses of Al-Baqarah (Sahih Muslim).",
            asbabNuzoolUr = "مکی دور کے بالکل آغاز میں نازل ہوئی۔ صحیح مسلم کی روایت کے مطابق حضرت جبرائیل علیہ السلام نے حضور ﷺ کو خوشخبری دی کہ آپ کو دو ایسے نور عطا ہوئے ہیں جو آپ سے پہلے کسی نبی کو نہیں ملے: سورۃ الفاتحہ اور سورۃ البقرہ کی آخری آیات۔",
            asbabNuzoolAr = "مكية من أوائل ما نزل بمكة، وهي أم الكتاب والسبع المثاني. روى مسلم في صحيحه نزول ملك لم ينزل قط مبشراً بنورين: فاتحة الكتاب وخواتيم سورة البقرة.",
            asbabNuzoolHi = "मक्की काल के प्रारंभ में अवतरित हुई। सहीह मुस्लिम के अनुसार यह दो महान प्रकाशों में से एक है जो नबी ﷺ को दिए गए।",
            mainThemesEn = listOf(
                "Tawheed (Oneness of Allah in Lordship, Worship, and Divine Attributes)",
                "The Day of Recompense and Accountability",
                "Sincere Supplication for Guidance to the Straight Path"
            ),
            mainThemesUr = listOf(
                "توحیدِ خالص اور اللہ تعالیٰ کی ربوبیت و رحمت کا اعتراف",
                "یومِ جزا پر پختہ ایمان اور جوابدہی کا احساس",
                "صراطِ مستقیم پر استقامت اور گمراہی سے بچاؤ کی دعا"
            ),
            mainThemesAr = listOf(
                "إخلاص التوحيد والعبودية لله رب العالمين",
                "إثبات يوم الدين والجزاء العادل",
                "الدعاء بالهداية إلى الصراط المستقيم"
            ),
            mainThemesHi = listOf(
                "तौहीद (अल्लाह का एक होना)",
                "क़यामत के दिन का यक़ीन",
                "सीधे रास्ते की हिदायत की दुआ"
            ),
            practicalLessonsEn = listOf(
                "Begin every action with Bismillah for Divine Barakah.",
                "Balance love, hope, and awe in every prayer.",
                "Seek help only from Allah through Isti'anah."
            ),
            practicalLessonsUr = listOf(
                "ہر اہم کام کا آغاز بسم اللہ سے کریں تاکہ برکت حاصل ہو۔",
                "دعا اور عبادت صرف اللہ کے لیے مخصوص کریں۔",
                "ہمیشہ صراطِ مستقیم پر قائم رہنے کی دعائیں مانگیں۔"
            ),
            practicalLessonsAr = listOf(
                "ابتداء الأعمال بالبسملة للبركة والتوفيق",
                "إفراد الله تعالى بالعبادة والاستعانة",
                "سؤال الثبات على الحق والاستقامة"
            ),
            practicalLessonsHi = listOf(
                "हर काम की शुरुआत बिस्मिल्लाह से करें।",
                "केवल अल्लाह से मदद माँगें।",
                "नेकी और सच्चाई के रास्ते पर चलें।"
            ),
            sourceAttribution = "Tafseer Ibn Kathir, Sahih Muslim (Book of Prayer), Ma'ariful Quran"
        ),
        SurahExegesis(
            surahNumber = 112,
            nameArabic = "سُورَةُ الإِخْلَاصِ",
            nameEnglish = "Surah Al-Ikhlas (The Sincerity)",
            revelationPlace = "Makki",
            asbabNuzoolEn = "Authentic Hadith recorded in Musnad Ahmad and Jami' at-Tirmidhi relates that the polytheists of Makkah said to the Prophet ﷺ: 'Describe your Lord's lineage to us!' Allah then revealed this pure declaration of Absolute Oneness.",
            asbabNuzoolUr = "مسند احمد اور ترمذی کی صحیح روایت کے مطابق قریشِ مکہ نے رسول اللہ ﷺ سے کہا: 'اپنے رب کا نسب بیان کیجیے!' تب اللہ تعالیٰ نے یہ سورۃ نازل فرما کر توحیدِ مطلق کی وضاحت فرمائی۔",
            asbabNuzoolAr = "نزلت رداً على مشركي مكة حين قالوا لرسول الله ﷺ: انسب لنا ربك! فأنزل الله تبارك وتعالى هذه السورة الخالصة في توحيد الصمدية.",
            asbabNuzoolHi = "मक्का के मुशरिकीन ने नबी ﷺ से पूछा कि अपने रब का वंश बताओ, तब अल्लाह ने यह सूरह नाज़िल फ़रमाई।",
            mainThemesEn = listOf(
                "Ahad: Absolute, Unrivaled Oneness",
                "As-Samad: Self-Sufficient upon whom all creation depends",
                "Transcending physical lineage, offspring, and resemblance"
            ),
            mainThemesUr = listOf(
                "اللہ ایک ہے، بے نیاز ہے، نہ کوئی اس کی اولاد ہے اور نہ وہ کسی کی اولاد ہے",
                "شرک اور تشبیہ کا مکمل رد",
                "قرآن کے ایک تہائی کے برابر ثواب کی حامل"
            ),
            mainThemesAr = listOf(
                "وحدانية الله المطلقة وأحديته الأزلية",
                "صمدية الحق سبحانه وافتقار جميع الخلائق إليه",
                "تنزيه الخالق عن الولد والوالد والكفء"
            ),
            mainThemesHi = listOf(
                "अल्लाह एक और अकेला है",
                "वह बेनियाज़ (स्वयं-निर्भर) है",
                "न उसका कोई वंशज है और न वह किसी की संतान है"
            ),
            practicalLessonsEn = listOf(
                "Purify intentions from hidden Shirk and hypocrisy.",
                "Rely solely on Allah (As-Samad) in every difficulty."
            ),
            practicalLessonsUr = listOf(
                "اپنے عقیدے اور نیت کو ہر قسم کے شرک اور دکھاوے سے پاک رکھیں۔",
                "تمام حاجات میں صرف الصمد پر بھروسہ رکھیں۔"
            ),
            practicalLessonsAr = listOf(
                "إخلاص التوحيد لله تعالى ونفي الشرك بجميع أنواعه",
                "اللجوء إلى الله الصمد في قضاء الحوائج كلها"
            ),
            practicalLessonsHi = listOf(
                "दिखावे और शिर्क से बचें।",
                "अपनी सभी ज़रूरतों के लिए केवल अल्लाह पर निर्भर रहें।"
            ),
            sourceAttribution = "Jami' at-Tirmidhi (Hadith 3364), Tafseer Ibn Kathir"
        )
    )

    // ==========================================
    // 6. ACADEMY ASSESSMENTS & MULTILINGUAL QUIZZES
    // ==========================================
    val academyQuizzes: List<AcademyQuizQuestion> = listOf(
        AcademyQuizQuestion(
            id = "quiz_1",
            category = "Alphabet & Makharij",
            questionEn = "Which part of the articulation area produces the letter Baa (ب)?",
            questionUr = "حرفِ با (ب) کس مخرج سے ادا ہوتا ہے؟",
            questionAr = "ما هو مخرج حرف الباء (ب) الصحيح؟",
            questionHi = "अक्षर बा (ب) किस मख़रज से निकलता है?",
            arabicPrompt = "ب",
            optionsEn = listOf("Both lips touching wet inner part firmly", "Middle of the throat", "Tip of the tongue", "Back of the palate"),
            optionsUr = listOf("دونوں ہونٹوں کے تری والے حصے کو ملا کر", "حلق کے درمیانی حصے سے", "زبان کی نوک سے", "تالو کے پچھلے حصے سے"),
            optionsAr = listOf("من باطن الشفتين بانطباق تام", "وسط الحلق", "طرف اللسان", "أقصى الحنك"),
            optionsHi = listOf("दोनों होंठों के भीतरी हिस्से को मिलाकर", "गले के बीच से", "ज़बान की नोक से", "तालू के पिछले भाग से"),
            correctIndex = 0,
            explanationEn = "The letter Baa is articulated by closing the wet inner part of both lips firmly together.",
            explanationUr = "حرفِ با دونوں ہونٹوں کے تری والے اندرونی حصے کو آپس میں ملانے سے ادا ہوتا ہے۔",
            explanationAr = "تخرج الباء بانطباق باطن الشفتين معاً انطباقاً تاماً رطباً.",
            explanationHi = "अक्षर बा दोनों होंठों के अंदरूनी भाग को मिलाकर अदा होता है।"
        ),
        AcademyQuizQuestion(
            id = "quiz_2",
            category = "Tajweed Rules",
            questionEn = "In the phrase 'مِن بَعْدِ', what rule applies to the Noon Sakinah?",
            questionUr = "'مِن بَعْدِ' میں نون ساکن پر تجوید کا کون سا قاعدہ لاگو ہوتا ہے؟",
            questionAr = "في قوله تعالى 'مِن بَعْدِ'، ما هو حكم النون الساكنة؟",
            questionHi = "'مِن بَعْدِ' में नून साकिना पर कौन सा नियम लागू होता है?",
            arabicPrompt = "مِن بَعْدِ",
            optionsEn = listOf("Iqlab (Noon converts to Meem)", "Izhar (Clearly pronounced)", "Idgham (Merged)", "Qalqalah"),
            optionsUr = listOf("اقلاب (نون کو میم میں بدلنا)", "اظہار (صاف پڑھنا)", "ادغام (ملانا)", "قلقلہ"),
            optionsAr = listOf("الإقلاب (قلب النون ميماً مخفاة)", "الإظهار الحلقي", "الإدغام", "القلقلة"),
            optionsHi = listOf("इक़्लाब (नून को मीम में बदलना)", "इज़हार (साफ़ पढ़ना)", "इदग़ाम (मिलाना)", "क़लक़ला"),
            correctIndex = 0,
            explanationEn = "When Noon Sakinah is followed by the letter Baa (ب), it converts to a Meem with Ghunnah (Iqlab).",
            explanationUr = "جب نون ساکن کے بعد حرفِ با آئے تو نون کو غنہ کے ساتھ میم سے بدل دیا جاتا ہے، جسے اقلاب کہتے ہیں۔",
            explanationAr = "حكم النون الساكنة هنا الإقلاب لوقوع حرف الباء بعدها، فتقلب ميماً مع الغنة.",
            explanationHi = "जब नून साकिना के बाद बा (ب) आए तो नून मीम में बदल जाता है (इक़्लाब)।"
        ),
        AcademyQuizQuestion(
            id = "quiz_3",
            category = "Quranic Vocabulary",
            questionEn = "What is the meaning of the frequent Quranic word 'رَبّ' (Rabb)?",
            questionUr = "قرآنی کلمہ 'رَبّ' کا مفہوم کیا ہے؟",
            questionAr = "ما معنى كلمة 'رَبّ' في الاستعمال القرآني؟",
            questionHi = "क़ुरआनी शब्द 'رَبّ' का अर्थ क्या है?",
            arabicPrompt = "رَبّ",
            optionsEn = listOf("Lord, Creator & Sustainer", "Servant", "Book", "Angel"),
            optionsUr = listOf("پالنے والا، مالک اور آقا", "بندہ", "کتاب", "فرشتہ"),
            optionsAr = listOf("السيد الخالق المربي المنعم", "العبد", "الكتاب", "الملك"),
            optionsHi = listOf("पालने वाला, स्वामी और रब", "दास", "किताब", "फ़रिश्ता"),
            correctIndex = 0,
            explanationEn = "Rabb denotes the One who creates, sustains, nurtures, and cherishes all creation toward perfection.",
            explanationUr = "رب کا معنی ہے پالنے والا، مالک اور تمام کائنات کو درجہ بدرجہ کمال تک پہنچانے والا۔",
            explanationAr = "الرب في لغة القرآن هو الخالق المالك المدبر لأمور خلقه بالتربية والإنعام.",
            explanationHi = "रब का अर्थ है पालने वाला, मालिक और सृजनहार।"
        ),
        AcademyQuizQuestion(
            id = "quiz_4",
            category = "Word-by-Word Translation",
            questionEn = "In Surah Al-Fatihah, what does 'إِيَّاكَ نَعْبُدُ' mean?",
            questionUr = "سورۃ الفاتحہ میں 'إِيَّاكَ نَعْبُدُ' کا ترجمہ کیا ہے؟",
            questionAr = "ما معنى قوله تعالى 'إِيَّاكَ نَعْبُدُ'؟",
            questionHi = "सूरह अल-फ़ातिहा में 'إِيَّاكَ نَعْبُدُ' का अनुवाद क्या है?",
            arabicPrompt = "إِيَّاكَ نَعْبُدُ",
            optionsEn = listOf("You alone we worship", "Guide us to the path", "All praise to Allah", "Master of Judgment Day"),
            optionsUr = listOf("ہم صرف تیری ہی عبادت کرتے ہیں", "ہمیں سیدھے راستے پر چلا", "سب تعریفیں اللہ کے لیے ہیں", "بدلے کے دن کا مالک"),
            optionsAr = listOf("نخصك وحدك بالعبادة والطاعة", "اهدنا الصراط", "الحمد لله", "مالك يوم الدين"),
            optionsHi = listOf("हम केवल तेरी ही इबादत करते हैं", "हमें सीधे रास्ते पर चला", "सब तारीफ़ें अल्लाह के लिए हैं", "न्याय के दिन का मालिक"),
            correctIndex = 0,
            explanationEn = "The phrasing 'Iyyaaka' before 'Na'budu' denotes exclusivity: We worship You and absolutely no one else.",
            explanationUr = "'ایاک' کا مقدم ہونا حصر کے لیے ہے: یعنی ہم صرف اور صرف تیری ہی بندگی کرتے ہیں اور کسی دوسرے کی نہیں۔",
            explanationAr = "تقديم المعمول 'إياك' يفيد الحصر والاختصاص، أي لا نعبد أحداً سواك.",
            explanationHi = "'इय्याक' पहले आने से यह अर्थ निकलता है कि हम केवल और केवल तेरी ही इबादत करते हैं।"
        )
    )

    // ==========================================
    // 7. ARABIC WRITING & TRACING EXERCISES
    // ==========================================
    val writingExercises = listOf(
        Pair("ا", "Alif - Straight vertical stroke from top to bottom"),
        Pair("ب", "Baa - Horizontal curved boat with single dot below"),
        Pair("ت", "Taa - Boat with two horizontal dots on top"),
        Pair("ث", "Thaa - Boat with triangular cluster of three dots on top"),
        Pair("ج", "Jeem - Angled head with deep rounded belly and center dot"),
        Pair("ح", "Haa - Angled head with deep rounded belly without dots"),
        Pair("خ", "Khaa - Angled head with single dot placed on top"),
        Pair("د", "Daal - Gentle 90-degree curve resting on baseline"),
        Pair("ر", "Raa - Smooth sweeping crescent curving below baseline"),
        Pair("س", "Seen - Three sharp teeth with deep descending tail"),
        Pair("ص", "Saad - Broad oval loop with sharp tooth and descending tail"),
        Pair("ط", "Taa - Broad oval loop with tall vertical mast"),
        Pair("ع", "Ayn - Small open crescent on top with deep circular belly"),
        Pair("ف", "Faa - Round loop with single dot on flat baseline boat"),
        Pair("ق", "Qaaf - Round loop with two dots and deep descending bowl"),
        Pair("ك", "Kaaf - Tall vertical stroke with flat base and mini S-mark"),
        Pair("ل", "Laam - Tall graceful vertical stroke with deep hooked tail"),
        Pair("م", "Meem - Small closed loop with straight descending tail"),
        Pair("ن", "Noon - Deep symmetrical bowl with single center dot"),
        Pair("و", "Waw - Round loop on baseline with sweeping crescent tail"),
        Pair("ه", "Haa - Double loop circle resting on baseline"),
        Pair("ي", "Yaa - Graceful swan neck with wide backwards bowl")
    )

    // ==========================================
    // 8. PROGRESS STORAGE & PERSISTENCE
    // ==========================================
    fun getProgress(context: Context): StudentAcademyProgress {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val completed = prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet()) ?: emptySet()
        val memorizedRaw = prefs.getStringSet(KEY_MEMORIZED_SURAHS, setOf("1", "112", "113", "114")) ?: setOf("1", "112", "113", "114")
        val memorized = memorizedRaw.mapNotNull { it.toIntOrNull() }.toSet()
        val learnedWords = prefs.getStringSet(KEY_LEARNED_WORDS, emptySet()) ?: emptySet()
        val passedQuizzes = prefs.getInt(KEY_QUIZZES_PASSED, 0)
        val lastModule = prefs.getString(KEY_LAST_MODULE, "qaida") ?: "qaida"
        val lastTitle = prefs.getString(KEY_LAST_LESSON_TITLE, "Noorani Qaida: Lesson 1 (Al-Mufradat)") ?: "Noorani Qaida: Lesson 1"
        val streak = prefs.getInt(KEY_STREAK_DAYS, 1)

        return StudentAcademyProgress(
            completedLessonIds = completed,
            memorizedSurahNumbers = memorized,
            learnedWordIds = learnedWords,
            totalQuizzesPassed = passedQuizzes,
            lastActiveModuleId = lastModule,
            lastActiveLessonTitle = lastTitle,
            currentStreakDays = streak
        )
    }

    fun markLessonCompleted(context: Context, lessonId: String, lessonTitle: String, moduleId: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_COMPLETED_LESSONS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(lessonId)
        prefs.edit()
            .putStringSet(KEY_COMPLETED_LESSONS, current)
            .putString(KEY_LAST_MODULE, moduleId)
            .putString(KEY_LAST_LESSON_TITLE, lessonTitle)
            .apply()
    }

    fun toggleMemorizedSurah(context: Context, surahNumber: Int): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_MEMORIZED_SURAHS, setOf("1", "112", "113", "114"))?.toMutableSet() ?: mutableSetOf()
        val key = surahNumber.toString()
        val isNowMemorized = if (current.contains(key)) {
            current.remove(key)
            false
        } else {
            current.add(key)
            true
        }
        prefs.edit().putStringSet(KEY_MEMORIZED_SURAHS, current).apply()
        return isNowMemorized
    }

    fun recordQuizPass(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_QUIZZES_PASSED, 0) + 1
        prefs.edit().putInt(KEY_QUIZZES_PASSED, count).apply()
    }

    fun toggleLearnedWord(context: Context, wordId: String): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_LEARNED_WORDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        val isLearned = if (current.contains(wordId)) {
            current.remove(wordId)
            false
        } else {
            current.add(wordId)
            true
        }
        prefs.edit().putStringSet(KEY_LEARNED_WORDS, current).apply()
        return isLearned
    }
}
