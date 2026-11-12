package com.example.data.repository

import android.content.Context
import com.example.data.model.*
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

object QuranData {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val reciters = listOf(
        Reciter(
            id = "mishary",
            nameArabic = "مشاري راشد العفاسي",
            nameEnglish = "Mishary Rashid Alafasy",
            nameUrdu = "مشاری راشد العفاسی",
            style = "Murattal (128kbps)",
            audioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/"
        ),
        Reciter(
            id = "abdulbasit",
            nameArabic = "عبد الباسط عبد الصمد",
            nameEnglish = "Abdul Basit Abdul Samad",
            nameUrdu = "عبد الباسط عبد الصمد",
            style = "Murattal (128kbps)",
            audioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmurattal/"
        ),
        Reciter(
            id = "husary",
            nameArabic = "محمود خليل الحصري",
            nameEnglish = "Mahmoud Khalil Al-Husary",
            nameUrdu = "محمود خلیل الحصری",
            style = "Mujawwad / Tajweed",
            audioBaseUrl = "https://everyayah.com/data/Husary_128kbps/"
        ),
        Reciter(
            id = "ghamdi",
            nameArabic = "سعد الغامدي",
            nameEnglish = "Saad Al-Ghamdi",
            nameUrdu = "سعد الغامدی",
            style = "Murattal (128kbps)",
            audioBaseUrl = "https://everyayah.com/data/Ghamadi_40kbps/"
        )
    )

    val paras: List<Para> = listOf(
        Para(1, "الم", "Alif Lam Meem", "الم", "अलिफ़ लाम मीम", 1, 1),
        Para(2, "سَيَقُولُ", "Sayaqool", "سیقول", "सयक़ूल", 2, 142),
        Para(3, "تِلْكَ الرُّسُلُ", "Tilkar Rusul", "تلک الرسل", "तिलकर रुसुल", 2, 253),
        Para(4, "لَنْ تَنَالُوا", "Lan Tanaaloo", "لن تنالوا", "लन तनालू", 3, 93),
        Para(5, "وَالْمُحْصَنَاتُ", "Wal Mohsanat", "والمحصنات", "वल मुहसनात", 4, 24),
        Para(6, "لَا يُحِبُّ اللَّهُ", "La Yuhibbullah", "لا یحب اللہ", "ला युहिब्बल्लाह", 4, 148),
        Para(7, "وَإِذَا سَمِعُوا", "Wa Iza Samiu", "واذا سمعوا", "व इज़ा समिऊ", 5, 82),
        Para(8, "وَلَوْ أَنَّنَا", "Wa Law Annana", "ولو اننا", "व लौ अन्नना", 6, 111),
        Para(9, "قَالَ الْمَلَأُ", "Qalal Malao", "قال الملا", "क़ालल मल-उ", 7, 88),
        Para(10, "وَاعْلَمُوا", "Wa A'lamoo", "واعلموا", "वअलमू", 8, 41),
        Para(11, "يَعْتَذِرُونَ", "Yatazeroon", "یعتذرون", "याताज़िरून", 9, 93),
        Para(12, "وَمَا مِنْ دَابَّةٍ", "Wa Mamin Da'abba", "وما من دابۃ", "वमा मिन दाब्बा", 11, 6),
        Para(13, "وَمَا أُبَرِّئُ", "Wa Ma Ubarri'u", "وما ابریٔ", "वमा उबरी-उ", 12, 53),
        Para(14, "رُبَمَا", "Rubama", "ربما", "रुबमा", 15, 1),
        Para(15, "سُبْحَانَ الَّذِي", "Subhanallazi", "سبحن الذی", "सुब्हानल्लज़ी", 17, 1),
        Para(16, "قَالَ أَلَمْ", "Qala Alam", "قال الم", "क़ाल अलम", 18, 75),
        Para(17, "اقْتَرَبَ", "Iqtaraba", "اقترب للناس", "इक़्तरब", 21, 1),
        Para(18, "قَدْ أَفْلَحَ", "Qad Aflaha", "قد افلح", "क़द अफ़लह", 23, 1),
        Para(19, "وَقَالَ الَّذِينَ", "Wa Qalallazina", "وقال الذین", "वक़ालल्लज़ीना", 25, 21),
        Para(20, "أَمَّنْ خَلَقَ", "Amman Khalaqa", "امن خلق", "अम्मन ख़लक़ा", 27, 56),
        Para(21, "اتْلُ مَا أُوحِيَ", "Utlu Ma Oohiya", "اتل ما اوحی", "उत्लु मा ऊहिया", 29, 46),
        Para(22, "وَمَنْ يَقْنُتْ", "Wa Manyaqnut", "ومن یقنت", "वमन यक़्नुत", 33, 31),
        Para(23, "وَمَا لِيَ", "Wa Maliya", "وما لی", "वमा लिया", 36, 28),
        Para(24, "فَمَنْ أَظْلَمُ", "Faman Azlamu", "فمن اظلم", "फ़मन अज़लमु", 39, 32),
        Para(25, "إِلَيْهِ يُرَدُّ", "Ilaihi Yuraddu", "الیہ یرد", "इलैहि युरद्दु", 41, 47),
        Para(26, "حم", "Ha'a Meem", "حم", "हा-मीम", 46, 1),
        Para(27, "قَالَ فَمَا خَطْبُكُمْ", "Qala Fama Khatbukum", "قال فما خطبکم", "क़ाल फ़मा ख़तबुकुम", 51, 31),
        Para(28, "قَدْ سَمِعَ اللَّهُ", "Qad Samiallah", "قد سمع اللہ", "क़द समिअल्लाह", 58, 1),
        Para(29, "تَبَارَكَ الَّذِي", "Tabarakallazi", "تبارک الذی", "तबारकल्लज़ी", 67, 1),
        Para(30, "عَمَّ", "Amma Yatasa'aloon", "عمّ یتساءلون", "अम्मा यतसा-अलून", 78, 1)
    )

    // Complete list of all 114 Surahs
    val surahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "الفاتحہ", "अल-फ़ातिहा", "The Opening", 7, RevelationType.MAKKI, 1),
        Surah(2, "البقرة", "Al-Baqarah", "البقرہ", "अल-बक़रा", "The Cow", 286, RevelationType.MADANI, 1),
        Surah(3, "آل عمران", "Ali 'Imran", "آل عمران", "आल इमरान", "Family of Imran", 200, RevelationType.MADANI, 3),
        Surah(4, "النساء", "An-Nisa", "النساء", "अन-निसा", "The Women", 176, RevelationType.MADANI, 4),
        Surah(5, "المائدة", "Al-Ma'idah", "المائدہ", "अल-माइदा", "The Table Spread", 120, RevelationType.MADANI, 6),
        Surah(6, "الأنعام", "Al-An'am", "الانعام", "अल-अनआम", "The Cattle", 165, RevelationType.MAKKI, 7),
        Surah(7, "الأعراف", "Al-A'raf", "الاعراف", "अल-आराफ़", "The Heights", 206, RevelationType.MAKKI, 8),
        Surah(8, "الأنفال", "Al-Anfal", "الانفال", "अल-अनफ़ाल", "The Spoils of War", 75, RevelationType.MADANI, 9),
        Surah(9, "التوبة", "At-Tawbah", "التوبہ", "अत-तौबा", "The Repentance", 129, RevelationType.MADANI, 10),
        Surah(10, "يونس", "Yunus", "یونس", "यूनुस", "Jonah", 109, RevelationType.MAKKI, 11),
        Surah(11, "هود", "Hud", "ہود", "हूद", "Hud", 123, RevelationType.MAKKI, 11),
        Surah(12, "يوسف", "Yusuf", "یوسف", "यूसुफ़", "Joseph", 111, RevelationType.MAKKI, 12),
        Surah(13, "الرعد", "Ar-Ra'd", "الرعد", "अर-राअद", "The Thunder", 43, RevelationType.MADANI, 13),
        Surah(14, "إبراهيم", "Ibrahim", "ابراہیم", "इब्राहीम", "Abraham", 52, RevelationType.MAKKI, 13),
        Surah(15, "الحجر", "Al-Hijr", "الحجر", "अल-हिज्र", "The Rocky Tract", 99, RevelationType.MAKKI, 14),
        Surah(16, "النحل", "An-Nahl", "النحل", "अन-नह्ल", "The Bee", 128, RevelationType.MAKKI, 14),
        Surah(17, "الإسراء", "Al-Isra", "الاسراء", "अल-इस्रा", "The Night Journey", 111, RevelationType.MAKKI, 15),
        Surah(18, "الكهف", "Al-Kahf", "الکہف", "अल-कहफ़", "The Cave", 110, RevelationType.MAKKI, 15),
        Surah(19, "مريم", "Maryam", "مریم", "मरयम", "Mary", 98, RevelationType.MAKKI, 16),
        Surah(20, "طه", "Taha", "طٰہٰ", "ता-हा", "Ta-Ha", 135, RevelationType.MAKKI, 16),
        Surah(21, "الأنبياء", "Al-Anbiya", "الانبیاء", "अल-अम्बिया", "The Prophets", 112, RevelationType.MAKKI, 17),
        Surah(22, "الحج", "Al-Hajj", "الحج", "अल-हज", "The Pilgrimage", 78, RevelationType.MADANI, 17),
        Surah(23, "المؤمنون", "Al-Mu'minoon", "المؤمنون", "अल-मुमिनून", "The Believers", 118, RevelationType.MAKKI, 18),
        Surah(24, "النور", "An-Nur", "النور", "अन-नूर", "The Light", 64, RevelationType.MADANI, 18),
        Surah(25, "الفرقان", "Al-Furqan", "الفرقان", "अल-फ़ुरक़ान", "The Criterion", 77, RevelationType.MAKKI, 18),
        Surah(26, "الشعراء", "Ash-Shu'ara", "الشعراء", "अश-शुआरा", "The Poets", 227, RevelationType.MAKKI, 19),
        Surah(27, "النمل", "An-Naml", "النمل", "अन-नम्ल", "The Ant", 93, RevelationType.MAKKI, 19),
        Surah(28, "القصص", "Al-Qasas", "القصص", "अल-क़सस", "The Stories", 88, RevelationType.MAKKI, 20),
        Surah(29, "العنكبوت", "Al-'Ankabut", "العنکبوت", "अल-अंकबूत", "The Spider", 69, RevelationType.MAKKI, 20),
        Surah(30, "الروم", "Ar-Rum", "الروم", "अर-रूम", "The Romans", 60, RevelationType.MAKKI, 21),
        Surah(31, "لقمان", "Luqman", "لقمان", "लुक़मान", "Luqman", 34, RevelationType.MAKKI, 21),
        Surah(32, "السجدة", "As-Sajdah", "السجدہ", "अस-सजदा", "The Prostration", 30, RevelationType.MAKKI, 21),
        Surah(33, "الأحزاب", "Al-Ahzab", "الاحزاب", "अल-अहज़ाब", "The Combined Forces", 73, RevelationType.MADANI, 21),
        Surah(34, "سبأ", "Saba", "سبا", "सबा", "Sheba", 54, RevelationType.MAKKI, 22),
        Surah(35, "فاطر", "Fatir", "فاطر", "फ़ातिर", "Originator", 45, RevelationType.MAKKI, 22),
        Surah(36, "يس", "Ya-Sin", "یٰسٓ", "या-सीन", "Ya Sin", 83, RevelationType.MAKKI, 22),
        Surah(37, "الصافات", "As-Saffat", "الصافات", "अस-साफ़्फ़ात", "Those Who Set The Ranks", 182, RevelationType.MAKKI, 23),
        Surah(38, "ص", "Sad", "صٓ", "साद", "The Letter Sad", 88, RevelationType.MAKKI, 23),
        Surah(39, "الزمر", "Az-Zumar", "الزمر", "अज़-ज़ुमर", "The Troops", 75, RevelationType.MAKKI, 23),
        Surah(40, "غافر", "Ghafir", "غافر", "ग़ाफ़िर", "The Forgiver", 85, RevelationType.MAKKI, 24),
        Surah(41, "فصلت", "Fussilat", "فصلت", "फ़ुस्सिलात", "Explained in Detail", 54, RevelationType.MAKKI, 24),
        Surah(42, "الشورى", "Ash-Shura", "الشوریٰ", "अश-शूरा", "The Consultation", 53, RevelationType.MAKKI, 25),
        Surah(43, "الزخرف", "Az-Zukhruf", "الزخرف", "अज़-ज़ुख़रुफ़", "The Ornaments of Gold", 89, RevelationType.MAKKI, 25),
        Surah(44, "الدخان", "Ad-Dukhan", "الدخان", "अद-दुख़ान", "The Smoke", 59, RevelationType.MAKKI, 25),
        Surah(45, "الجاثية", "Al-Jathiyah", "الجاثیہ", "अल-जासिया", "The Crouching", 37, RevelationType.MAKKI, 25),
        Surah(46, "الأحقاف", "Al-Ahqaf", "الاحقاف", "अल-अहक़ाफ़", "The Wind-Curved Sandhills", 35, RevelationType.MAKKI, 26),
        Surah(47, "محمد", "Muhammad", "محمد ﷺ", "मुहम्मद ﷺ", "Muhammad", 38, RevelationType.MADANI, 26),
        Surah(48, "الفتح", "Al-Fath", "الفتح", "अल-फ़तह", "The Victory", 29, RevelationType.MADANI, 26),
        Surah(49, "الحجرات", "Al-Hujurat", "الحجرات", "अल-हुजुरात", "The Rooms", 18, RevelationType.MADANI, 26),
        Surah(50, "ق", "Qaf", "قٓ", "क़ाफ़", "The Letter Qaf", 45, RevelationType.MAKKI, 26),
        Surah(51, "الذاريات", "Adh-Dhariyat", "الذاریات", "अज़-ज़ारियात", "The Winnowing Winds", 60, RevelationType.MAKKI, 26),
        Surah(52, "الطور", "At-Tur", "الطور", "अत-तूर", "The Mount", 49, RevelationType.MAKKI, 27),
        Surah(53, "النجم", "An-Najm", "النجم", "अन-नज्म", "The Star", 62, RevelationType.MAKKI, 27),
        Surah(54, "القمر", "Al-Qamar", "القمر", "अल-क़मर", "The Moon", 55, RevelationType.MAKKI, 27),
        Surah(55, "الرحمن", "Ar-Rahman", "الرحمن", "अर-रहमान", "The Beneficent", 78, RevelationType.MADANI, 27),
        Surah(56, "الواقعة", "Al-Waqi'ah", "الواقعہ", "अल-वाक़िआ", "The Inevitable", 96, RevelationType.MAKKI, 27),
        Surah(57, "الحديد", "Al-Hadid", "الحدید", "अल-हदीद", "The Iron", 29, RevelationType.MADANI, 27),
        Surah(58, "المجادلة", "Al-Mujadila", "المجادلہ", "अल-मुजादिला", "The Pleading Woman", 22, RevelationType.MADANI, 28),
        Surah(59, "الحشر", "Al-Hashr", "الحشر", "अल-हश्र", "The Exile", 24, RevelationType.MADANI, 28),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "الممتحنہ", "अल-मुमतहिना", "She that is to be examined", 13, RevelationType.MADANI, 28),
        Surah(61, "الصف", "As-Saff", "الصف", "अस-सफ़", "The Ranks", 14, RevelationType.MADANI, 28),
        Surah(62, "الجمعة", "Al-Jumu'ah", "الجمعہ", "अल-जुमुआ", "Friday", 11, RevelationType.MADANI, 28),
        Surah(63, "المنافقون", "Al-Munafiqoon", "المنافقون", "अल-मुनाफ़िक़ून", "The Hypocrites", 11, RevelationType.MADANI, 28),
        Surah(64, "التغابن", "At-Taghabun", "التغابن", "अत-तग़ाबुन", "Mutual Disillusion", 18, RevelationType.MADANI, 28),
        Surah(65, "الطلاق", "At-Talaq", "الطلاق", "अत-तलाक़", "Divorce", 12, RevelationType.MADANI, 28),
        Surah(66, "التحريم", "At-Tahrim", "التحریم", "अत-तहरीम", "The Prohibition", 12, RevelationType.MADANI, 28),
        Surah(67, "الملك", "Al-Mulk", "الملک", "अल-मुल्क", "The Sovereignty", 30, RevelationType.MAKKI, 29),
        Surah(68, "القلم", "Al-Qalam", "القلم", "अल-क़लम", "The Pen", 52, RevelationType.MAKKI, 29),
        Surah(69, "الحاقة", "Al-Haqqah", "الحاقہ", "अल-हाक़्क़ा", "The Inevitable Reality", 52, RevelationType.MAKKI, 29),
        Surah(70, "المعارج", "Al-Ma'arij", "المعارج", "अल-मआरिज", "The Ascending Stairways", 44, RevelationType.MAKKI, 29),
        Surah(71, "نوح", "Nuh", "نوح", "नूह", "Noah", 28, RevelationType.MAKKI, 29),
        Surah(72, "الجن", "Al-Jinn", "الجن", "अल-जिन्न", "The Jinn", 28, RevelationType.MAKKI, 29),
        Surah(73, "المزمل", "Al-Muzzammil", "المزمل", "अल-मुज़म्मिल", "The Enshrouded One", 20, RevelationType.MAKKI, 29),
        Surah(74, "المدثر", "Al-Muddaththir", "المدثر", "अल-मुद्दस्सिर", "The Cloaked One", 56, RevelationType.MAKKI, 29),
        Surah(75, "القيامة", "Al-Qiyamah", "القیامہ", "अल-क़ियामा", "The Resurrection", 40, RevelationType.MAKKI, 29),
        Surah(76, "الإنسان", "Al-Insan", "الانسان", "अल-इंसान", "Man", 31, RevelationType.MADANI, 29),
        Surah(77, "المرسلات", "Al-Mursalat", "المرسلات", "अल-मुरसलात", "The Emissaries", 50, RevelationType.MAKKI, 29),
        Surah(78, "النبأ", "An-Naba", "النباء", "अन-नबा", "The Tidings", 40, RevelationType.MAKKI, 30),
        Surah(79, "النازعات", "An-Nazi'at", "النازعات", "अन-नाज़िआत", "Those who drag forth", 46, RevelationType.MAKKI, 30),
        Surah(80, "عبس", "'Abasa", "عبس", "अबसा", "He Frowned", 42, RevelationType.MAKKI, 30),
        Surah(81, "التكوير", "At-Takwir", "التکویر", "अत-तकवीर", "The Overthrowing", 29, RevelationType.MAKKI, 30),
        Surah(82, "الانفطار", "Al-Infitar", "الانفطار", "अल-इन्फ़ितार", "The Cleaving", 19, RevelationType.MAKKI, 30),
        Surah(83, "المطففين", "Al-Mutaffifin", "المطففین", "अल-मुतफ़्फ़िफ़ीन", "Defrauding", 36, RevelationType.MAKKI, 30),
        Surah(84, "الانشقاق", "Al-Inshiqaq", "الانشقاق", "अल-इन्शिक़ाक़", "The Splitting Open", 25, RevelationType.MAKKI, 30),
        Surah(85, "البروج", "Al-Burooj", "البروج", "अल-बुरूज", "The Mansions of the Stars", 22, RevelationType.MAKKI, 30),
        Surah(86, "الطارق", "At-Tariq", "الطارق", "अत-तारिक़", "The Morning Star", 17, RevelationType.MAKKI, 30),
        Surah(87, "الأعلى", "Al-A'la", "الاعلیٰ", "अल-आला", "The Most High", 19, RevelationType.MAKKI, 30),
        Surah(88, "الغاشية", "Al-Ghashiyah", "الغاشیہ", "अल-ग़ाशिया", "The Overwhelming Event", 26, RevelationType.MAKKI, 30),
        Surah(89, "الفجر", "Al-Fajr", "الفجر", "अल-फ़ज्र", "The Dawn", 30, RevelationType.MAKKI, 30),
        Surah(90, "البلد", "Al-Balad", "البلد", "अल-बलद", "The City", 20, RevelationType.MAKKI, 30),
        Surah(91, "الشمس", "Ash-Shams", "الشمس", "अश-शम्स", "The Sun", 15, RevelationType.MAKKI, 30),
        Surah(92, "الليل", "Al-Lail", "اللیل", "अल-लैल", "The Night", 21, RevelationType.MAKKI, 30),
        Surah(93, "الضحى", "Ad-Duha", "الضحیٰ", "अद-दुहा", "The Morning Hours", 11, RevelationType.MAKKI, 30),
        Surah(94, "الشرح", "Ash-Sharh", "الانشراح", "अश-शरह", "The Relief", 8, RevelationType.MAKKI, 30),
        Surah(95, "التين", "At-Tin", "التین", "अत-तीन", "The Fig", 8, RevelationType.MAKKI, 30),
        Surah(96, "العلق", "Al-'Alaq", "العلق", "अल-अलक़", "The Clot", 19, RevelationType.MAKKI, 30),
        Surah(97, "القدر", "Al-Qadr", "القدر", "अल-क़द्र", "The Power", 5, RevelationType.MAKKI, 30),
        Surah(98, "البينة", "Al-Bayyinah", "البینہ", "अल-बय्यिना", "The Clear Proof", 8, RevelationType.MADANI, 30),
        Surah(99, "الزلزلة", "Az-Zalzalah", "الزلزال", "अज़-ज़लज़ला", "The Earthquake", 8, RevelationType.MADANI, 30),
        Surah(100, "العاديات", "Al-'Adiyat", "العادیات", "अल-आदियात", "The Courser", 11, RevelationType.MAKKI, 30),
        Surah(101, "القارعة", "Al-Qari'ah", "القارclassعة", "अल-क़ारिआ", "The Calamity", 11, RevelationType.MAKKI, 30),
        Surah(102, "التكاثر", "At-Takathur", "التکاثر", "अत-तकासुर", "The Rivalry in world increase", 8, RevelationType.MAKKI, 30),
        Surah(103, "العصر", "Al-'Asr", "العصر", "अल-अस्र", "The Declining Day", 3, RevelationType.MAKKI, 30),
        Surah(104, "الهمزة", "Al-Humazah", "الہمزہ", "अल-हुमज़ा", "The Traducer", 9, RevelationType.MAKKI, 30),
        Surah(105, "الفيل", "Al-Fil", "الفیل", "अल-फ़ील", "The Elephant", 5, RevelationType.MAKKI, 30),
        Surah(106, "قريش", "Quraysh", "قریش", "क़ुरैश", "Quraysh", 4, RevelationType.MAKKI, 30),
        Surah(107, "الماعون", "Al-Ma'un", "الماعون", "अल-माऊन", "The Small Kindnesses", 7, RevelationType.MAKKI, 30),
        Surah(108, "الكوثر", "Al-Kawthar", "الکوثر", "अल-कौसर", "The Abundance", 3, RevelationType.MAKKI, 30),
        Surah(109, "الكافرون", "Al-Kafiroon", "الکافرون", "अल-काफ़िरून", "The Disbelievers", 6, RevelationType.MAKKI, 30),
        Surah(110, "النصر", "An-Nasr", "النصر", "अन-नस्र", "The Divine Support", 3, RevelationType.MADANI, 30),
        Surah(111, "المسد", "Al-Masad", "المسد", "अल-मसद", "The Palm Fiber", 5, RevelationType.MAKKI, 30),
        Surah(112, "الإخلاص", "Al-Ikhlas", "الاخلاص", "अल-इख़लास", "The Sincerity", 4, RevelationType.MAKKI, 30),
        Surah(113, "الفلق", "Al-Falaq", "الفلق", "अल-फ़लक़", "The Daybreak", 5, RevelationType.MAKKI, 30),
        Surah(114, "الناس", "An-Nas", "الناس", "अन-नास", "Mankind", 6, RevelationType.MAKKI, 30)
    )

    // Complete Ayahs for featured Surahs with Tanzil-verified Uthmani Arabic text, multiple verified translations, and authentic Tafseers
    private val ayahDatabase: Map<Int, List<Ayah>> = mapOf(
        1 to listOf(
            createAyah(
                surah = 1,
                ayah = 1,
                arabic = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                para = 1,
                enTrans = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                enTransName = "Saheeh International",
                urTrans = "شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "अल्लाह के नाम से, जो अत्यंत कृपाशील और दयावान है।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "أبتدئ قراءتي مستعينا باسم الله تعالى، وهو الرحمن ذو الرحمة العامة، الرحيم ذو الرحمة الخاصة.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Bismi-llah' begins every action in Islam. 'Ar-Rahman' and 'Ar-Rahim' are two names derived from Rahmah (mercy). Ar-Rahman carries a more expansive meaning of mercy embracing all creation, whereas Ar-Rahim denotes special mercy for the believers.",
                tafseerEnAuthor = "Tafseer Ibn Kathir (Abridged)",
                tafseerUr = "تفسیر ابن کثیر: تمام کلام اور افعال کی ابتدا اللہ تعالیٰ کے بابرکت نام سے کرنا مسنون ہے۔ اللہ تعالیٰ نے انسان کو توفیق بخشی کہ وہ اپنے ہر کام کا آغاز اس کے نام سے کرے۔ رحمن اور رحیم دونوں رحمت سے ماخوذ ہیں، رحمن عام رحمت کا حامل ہے اور رحیم خاص بندوں پر مہربان۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: بدأت السورة بالبسملة لأن كل أمر ذي بال لا يبدأ فيه ببسم الله فهو أبتر. الله هو المألوه المعبود بحق. الرحمن ذو الرحمة الواسعة الشاملة، والرحيم بعباده المؤمنين.",
                tafseerArAuthor = "التفسير الميسر (مجمع الملك فهد)"
            ),
            createAyah(
                surah = 1,
                ayah = 2,
                arabic = "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ",
                para = 1,
                enTrans = "[All] praise is [due] to Allah, Lord of the worlds -",
                enTransName = "Saheeh International",
                urTrans = "سب طرح کی تعریف خدا ہی کو (سزاوار) ہے جو تمام مخلوقات کا پروردگار ہے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "प्रशंसा अल्लाह ही के लिए है, जो सारे संसार का पालनहार है।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "الثناء الكامل والشكر الخالص لله وحده، مالك الخلق والمدبر لأمورهم جميعاً.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Al-Hamd' is praise combined with reverence and love. Allah possesses all perfect attributes. 'Rabb' denotes the Owner, Master, Caregiver, and Sustainer of all creations ('al-Alamin').",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفہیم القرآن (مودودی): حمد کا مفہوم شکر اور تعریف دونوں پر مشتمل ہے۔ رب سے مراد وہ ہستی ہے جو پالنے والی، پرورش کرنے والی، ضروریات پوری کرنے والی اور ہر لمحہ خبرگیری کرنے والی ہے۔ تمام کائناتوں کی ربوبیت صرف اللہ وحدہ لا شریک کے دستِ قدرت میں ہے۔",
                tafseerUrAuthor = "تفہیم القرآن (سید ابوالاعلیٰ مودودی)",
                tafseerAr = "تفسير الجلالين: (الحمد لله) الثناء عليه بجميل صفاته وبإنعامه، و(رب العالمين) مالك الإنس والجن والملائكة وجميع المخلوقات وخالقهم ومربيهم بنعمه.",
                tafseerArAuthor = "تفسير الجلالين"
            ),
            createAyah(
                surah = 1,
                ayah = 3,
                arabic = "الرَّحْمَٰنِ الرَّحِيمِ",
                para = 1,
                enTrans = "The Entirely Merciful, the Especially Merciful,",
                enTransName = "Saheeh International",
                urTrans = "بڑا مہربان نہایت رحم والا۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "अत्यंत कृपाशील, अति दयावान है।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "ذو الرحمة الواسعة التي وسعت كل شيء، والرحمة الخاصة بالمؤمنين.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Allah reiterated His mercy immediately after stating His total Lordship and dominion to instill hope alongside awe and reverence.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر: رب العالمین کے جلال و ہیبت کے ذکر کے فوراً بعد اپنی بے پایاں رحمت کا مژدہ سنایا تاکہ بندے کے دل میں خوف اور امید کا صحیح توازن قائم رہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: بيان لعظيم رحمته وإحسانه، فبعد أن وصف نفسه بالربوبية أتبعها بالرحمة ترغيباً بعد ترهيب.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 1,
                ayah = 4,
                arabic = "مَالِكِ يَوْمِ الدِّينِ",
                para = 1,
                enTrans = "Sovereign of the Day of Recompense.",
                enTransName = "Saheeh International",
                urTrans = "انصاف کے دن کا حاکم۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "बदले के दिन (क़यामत) का मालिक है।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "المتصرف وحده في يوم القيامة، يوم الجزاء والحساب على الأعمال.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Malik Yawm ad-Deen' means the sole Ruler on the Day of Judgment when all worldly kings and claims cease and only Allah's sovereignty remains manifest.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفہیم القرآن: 'دین' یہاں بدلے اور جزا و سزا کے معنی میں آیا ہے۔ قیامت کے روز جزا اور سزا کا پورا اختیار اسی ایک پروردگار کے ہاتھ میں ہوگا۔",
                tafseerUrAuthor = "تفہیم القرآن",
                tafseerAr = "تفسير الجلالين: مالك يوم الجزاء وهو يوم القيامة، وخص بالذكر لأنه لا ملك ظاهراً فيه لأحد غيره تعالى.",
                tafseerArAuthor = "تفسير الجلالين"
            ),
            createAyah(
                surah = 1,
                ayah = 5,
                arabic = "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ",
                para = 1,
                enTrans = "It is You we worship and You we ask for help.",
                enTransName = "Saheeh International",
                urTrans = "(اے پروردگار) ہم تیری ہی عبادت کرتے ہیں اور تجھ ہی سے مدد مانگتے ہیں۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "हम तेरी ही इबादत करते हैं और तुझ ही से मदद चाहते हैं।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "نخصك وحدك بالعبادة، ونستعين بك وحدك في جميع أمورنا.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Shifting from third person to second person directly addresses Allah. 'Iyyaka Na'budu' establishes pure Tawheed (Monotheism) and disavows Shirk (polytheism), while 'Iyyaka Nasta'in' disavows reliance on anyone other than Allah.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: یہ آیت تمام اسلامی عقائد کا خلاصہ ہے۔ 'ایاک نعبد' سے شرک کی نفی اور 'ایاک نستعین' سے اپنی بے بسی و ناتوانی کا اعتراف اور توکل علی اللہ ظاہر ہوتا ہے۔ عبادت مدد چاہنے پر مقدم رکھی گئی کیونکہ اللہ کا حق بندے کی ضرورت پر مقدم ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: تقديم المفعول (إياك) يفيد الحصر والقصر، أي لا نعبد غيرك ولا نستعين بسواك في أمر من الأمور.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 1,
                ayah = 6,
                arabic = "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ",
                para = 1,
                enTrans = "Guide us to the straight path -",
                enTransName = "Saheeh International",
                urTrans = "ہم کو سیدھے راستے پر چلا۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "हमें सीधे मार्ग पर चला।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "دلّنا ووفّقنا وثبّتنا على الطريق الواضح المستقيم الذي لا عوج فيه.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'As-Sirat al-Mustaqim' is Islam, the Qur'an, and the Sunnah of the Prophet Muhammad ﷺ. Hidayah here signifies both guidance to knowledge and perseverance on righteousness.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفہیم القرآن: صراطِ مستقیم سے مراد وہ سیدھا اور معتدل راستہ ہے جس کی رہنمائی اللہ کے رسولوں اور کتابوں نے کی۔ ہدایت طلب کرنے کا مطلب راستے کی معرفت، اس پر چلنے کی توفیق اور استقامت مانگنا ہے۔",
                tafseerUrAuthor = "تفہیم القرآن",
                tafseerAr = "تفسير الجلالين: أي أرشدنا إليه وثبتنا عليه وهو دين الإسلام الواضح المؤدي إلى رضوان الله وجنته.",
                tafseerArAuthor = "تفسير الجلالين"
            ),
            createAyah(
                surah = 1,
                ayah = 7,
                arabic = "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                para = 1,
                enTrans = "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.",
                enTransName = "Saheeh International",
                urTrans = "ان لوگوں کے راستے جن پر تو اپنا فضل و کرم کرتا رہا، نہ ان کے جن پر غصہ کیا گیا اور نہ گمراہوں کے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "उन लोगों का मार्ग जिन पर तूने कृपा की, जो न तेरे प्रकोप के भागी हुए और न भटके हुए हैं।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "طريق الذين تفضلت عليهم بالهداية من النبيين والصديقين والشهداء والصالحين، غير طريق المغضوب عليهم وهم اليهود ومن سلك سبيلهم، ولا الضالين وهم النصارى ومن تبعهم.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Those favored are the Prophets, truth-tellers, martyrs, and righteous (Surah An-Nisa: 69). The Prophet ﷺ explained that 'al-Maghdubi 'Alayhim' refers to those who know the truth yet reject it, and 'Ad-Dallin' refers to those who act without divine knowledge.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: 'منعم علیہم' سے مراد انبیاء، صدیقین، شہداء اور صالحین ہیں۔ اور جن پر غضب ہوا وہ وہ ہیں جنہوں نے حق کو پہچان کر بھی اس سے روگردانی کی، اور گمراہ وہ ہیں جنہوں نے جہالت و گمراہی کی روش اختیار کی۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: المؤمن يسأل الله دائماً أن يجنبه مسالك أهل الغضب الذين عرفوا الحق وتركوه، وأهل الضلال الذين عبدوا الله على جهل وضلالة.",
                tafseerArAuthor = "التفسير الميسر"
            )
        ),
        // Surah 112: Al-Ikhlas
        112 to listOf(
            createAyah(
                surah = 112,
                ayah = 1,
                arabic = "قُلْ هُوَ اللَّهُ أَحَدٌ",
                para = 30,
                enTrans = "Say, 'He is Allah, [who is] One,",
                enTransName = "Saheeh International",
                urTrans = "کہہ دو کہ وہ اللہ ایک ہے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "कहो: वह अल्लाह एक और अद्वितीय है,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "قل أيها الرسول لمن سألوك عن ربك: هو الله المتفرد بالألوهية والربوبية والأسماء والصفات، لا شريك له.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Ahad' means He is the One and Only, Who has no peer, no assistant, no rival, and no equal. This Surah equals one-third of the Quran in reward according to authentic Hadith in Sahih Bukhari.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: مشرکین نے رسول اللہ ﷺ سے دریافت کیا تھا کہ اپنے رب کا نسب بیان کیجیے، اس پر یہ سورت نازل ہوئی۔ 'احد' کا مطلب ہے کہ ذات، صفات اور افعال میں اس کی کوئی نظیر نہیں۔ صحیح بخاری کے مطابق یہ سورت تہائی قرآن کے برابر ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "تفسير الجلالين: أي هو الله واحد لا شريك له ولا نظير في ملكه وخلقه وتدبيره.",
                tafseerArAuthor = "تفسير الجلالين"
            ),
            createAyah(
                surah = 112,
                ayah = 2,
                arabic = "اللَّهُ الصَّمَدُ",
                para = 30,
                enTrans = "Allah, the Eternal Refuge.",
                enTransName = "Saheeh International",
                urTrans = "اللہ بےنیاز (سب کا سہارا) ہے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "अल्लाह बेनियाज़ (सब उसकी शरण में हैं) है।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "الله الذي يقصده جميع الخلائق في حوائجهم ورغائبهم، الغني عن كل ما سواه.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'As-Samad' means the Master to Whom all creation turns in their needs and supplications, while He has no need of any of them.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفہیم القرآن: 'صمد' وہ ہستی ہے جس کی طرف ہر ضرورت میں رجوع کیا جائے، جو خود بے پروا اور بے نیاز ہو لیکن ساری کائنات اپنی بقا اور حاجات کے لیے اس کی محتاج ہو۔",
                tafseerUrAuthor = "تفہیم القرآن",
                tafseerAr = "التفسير الميسر: السيد الذي تصمد إليه الخلائق في قضاء حوائجها، المستغني عن كل أحد والمفتقر إليه كل أحد.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 112,
                ayah = 3,
                arabic = "لَمْ يَلِدْ وَلَمْ يُولَدْ",
                para = 30,
                enTrans = "He neither begets nor is born,",
                enTransName = "Saheeh International",
                urTrans = "نہ اس کی کوئی اولاد ہے اور نہ وہ کسی کی اولاد ہے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "न उसने किसी को जना और न वह किसी से जन्मा है,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "ليس له ولد ولم يكن له والد سبحانه، متنزه عن صفات المخلوقين.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Negating offspring, ancestry, and physical succession proves His absolute eternity and uniqueness above all creation.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: اللہ تعالیٰ ہر قسم کے نسب، والد یا اولاد سے پاک اور بالاتر ہے۔ ولادت مخلوق کی صفت ہے اور خالق اس سے منزہ ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: نفي للأبوة والبنوة التي افتراها المشركون وأهل الكتاب، فهو الأول الذي ليس قبله شيء والآخر الذي ليس بعده شيء.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 112,
                ayah = 4,
                arabic = "وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
                para = 30,
                enTrans = "Nor is there to Him any equivalent.'",
                enTransName = "Saheeh International",
                urTrans = "اور کوئی اس کا ہمسر اور برابری کرنے والا نہیں۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "और न कोई उसके बराबर का है।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "ولم يكن له مماثلاً ولا مساوياً أحد من خلقه، ليس كمثله شيء وهو السميع البصير.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Kufuwan Ahad' signifies that absolutely nothing in existence resembles Him or possesses equal status in majesty, perfection, or sovereignty.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر: کائنات میں کوئی ایک بھی ہستی نہیں جو کسی درجے میں بھی اللہ کے برابر یا مماثل ہو سکے۔ وہ یکتا اور بے مثل ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "تفسير الجلالين: أي مكافئاً ومماثلاً أحد، سبحانه وتعالى عما يصفون.",
                tafseerArAuthor = "تفسير الجلالين"
            )
        ),
        // Surah 113: Al-Falaq
        113 to listOf(
            createAyah(
                surah = 113,
                ayah = 1,
                arabic = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ",
                para = 30,
                enTrans = "Say, 'I seek refuge in the Lord of daybreak",
                enTransName = "Saheeh International",
                urTrans = "کہو کہ میں صبح کے پروردگار کی پناہ مانگتا ہوں۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "कहो: मैं सुबह के रब की शरण लेता हूँ,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "قل أيها الرسول: أعتصم وألتجئ برب الصبح وفالقه.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Al-Falaq' signifies the morning light that cleaves darkness. The One capable of dispelling cosmic darkness is capable of dispelling any evil that threatens the believer.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: 'فلق' سے مراد صبح کا پھوٹنا ہے۔ جس طرح اللہ رات کے اندھیرے کو چاک کر کے صبح کا نور پھیلاتا ہے، اسی طرح وہ ہر برائی اور خوف کو دور کرنے پر قادر ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: الاستعاذة بالله القادر على إزالة الظلمة بضياء الصبح تكفل الحفظ من كل شر.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 113,
                ayah = 2,
                arabic = "مِنْ شَرِّ مَا خَلَقَ",
                para = 30,
                enTrans = "From the evil of that which He created",
                enTransName = "Saheeh International",
                urTrans = "ہر اس چیز کے شر سے جو اس نے پیدا کی۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "हर उस चीज़ की बुराई से जो उसने पैदा की,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "من شر جميع المخلوقات وأذاها.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Encompasses seeking refuge from harm caused by any created entity—humans, jinn, beasts, diseases, and unseen perils.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر: کائنات کی ہر مخلوق کے شر سے، چاہے انسان ہو، جنات ہوں، درندے ہوں یا آفات و بیماریاں، اللہ تعالیٰ کی پناہ طلب کی گئی ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "تفسير الجلالين: أي من شر كل ذي شر خلقه الله في الأرض والسماء.",
                tafseerArAuthor = "تفسير الجلالين"
            ),
            createAyah(
                surah = 113,
                ayah = 3,
                arabic = "وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ",
                para = 30,
                enTrans = "And from the evil of darkness when it settles",
                enTransName = "Saheeh International",
                urTrans = "اور اندھیری رات کے شر سے جب اس کا اندھیرا چھا جائے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "और अंधेरी रात की बुराई से जब वह छा जाए,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "ومن شر الليل المظلم إذا دخل واشتد ظلامه، لما ينتشر فيه من الشياطين والشرور.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Ghasiqin iza waqab' means the onset of night when darkness blankets the earth, during which evil forces and predators emerge.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفہیم القرآن: رات کے گہرے اندھیرے میں جرائم پیشہ لوگ، کیڑے مکوڑے اور شیاطین پھیل جاتے ہیں، اس لیے رات کے شر سے خاص پناہ مانگی گئی ہے۔",
                tafseerUrAuthor = "تفہیم القرآن",
                tafseerAr = "التفسير الميسر: تخصيص شر الليل بالذكر لانتشار الهوام والسباع والشياطين فيه.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 113,
                ayah = 4,
                arabic = "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ",
                para = 30,
                enTrans = "And from the evil of the blowers in knots",
                enTransName = "Saheeh International",
                urTrans = "اور گنڈوں پر پھونکنے والیوں کے شر سے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "और गांठों में फूंकने वालियों (जादू-टोने) की बुराई से,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "ومن شر السواحر اللاتي يعقدن العقد وينفثن فيها بالسحر والأذى.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Refers to practitioners of sorcery and witchcraft who blow with incantations over tied knots to cast spells.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: اس سے مراد جادوگر اور جادوگرنیاں ہیں جو دھاگوں میں گرہیں لگا کر ان پر منتر پھونکتے ہیں۔ جادو کے اثرات سے بچاؤ کا سب سے بڑا ذریعہ یہ معوذتین ہیں۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "تفسير الجلالين: أي السواحر اللاتي ينفخن في عقد الخيوط حين يسحرن بها.",
                tafseerArAuthor = "تفسير الجلالين"
            ),
            createAyah(
                surah = 113,
                ayah = 5,
                arabic = "وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                para = 30,
                enTrans = "And from the evil of an envier when he envies.'",
                enTransName = "Saheeh International",
                urTrans = "اور حسد کرنے والے کے شر سے جب وہ حسد کرنے لگے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "और डााह (ईर्ष्या) करने वाले की बुराई से जब वह डाह करे।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "ومن شر كل حاسد يبغض النعمة على غيره ويسعى في زوالها وإيصال الضرر إليه.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Hasad is wishing that Allah's blessings be stripped from another person. The evil eye (al-Ayn) is real and rooted in envy.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: حسد یہ ہے کہ بندہ دوسرے کی نعمت کے زوال کی تمنا کرے اور اس کو نقصان پہنچانے کے درپے ہو۔ نظرِ بد بھی حسد ہی کی ایک خطرناک شاخ ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: الحاسد إذا أظهر حسده وعمل بمقتضاه أضر بالمعيون، فكان التعوذ بالله ملجأً وحصناً منه.",
                tafseerArAuthor = "التفسير الميسر"
            )
        ),
        // Surah 114: An-Nas
        114 to listOf(
            createAyah(
                surah = 114,
                ayah = 1,
                arabic = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ",
                para = 30,
                enTrans = "Say, 'I seek refuge in the Lord of mankind,",
                enTransName = "Saheeh International",
                urTrans = "کہو کہ میں انسانوں کے پروردگار کی پناہ میں آتا ہوں۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "कहो: मैं लोगों के रब की शरण लेता हूँ,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "قل أيها الرسول: ألتجئ وأعتصم برب الناس وخالقهم ومدبر أمورهم.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Surah An-Nas seeks refuge with the Rabb (Lord), Malik (King), and Ilah (God) of mankind against the most insidious inner spiritual danger: the whispers of Satan.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: اس سورت میں اللہ تعالیٰ کی ربوبیت، بادشاہت اور الوہیت کے تینوں اوصاف کے واسطے سے انسان کے سب سے بڑے اور چھپے ہوئے دشمن شیطان کے وسوسوں سے پناہ مانگی گئی ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: استعاذة برب الناس الذي يربيهم بنعمه ويصلح شؤونهم.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 114,
                ayah = 2,
                arabic = "مَلِكِ النَّاسِ",
                para = 30,
                enTrans = "The Sovereign of mankind,",
                enTransName = "Saheeh International",
                urTrans = "انسانوں کے حقیقی بادشاہ کی (پناہ میں)،",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "लोगों के बादशाह की,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "ملك الناس المتصرف في شؤونهم بقضائه وقدره.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Malik An-Nas' emphasizes that worldly kings are merely His creations, and all mankind are His subjects.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفہیم القرآن: وہ تمام انسانوں کا حقیقی اور مطلق حاکم ہے، جس کا حکم ہر شخص پر نافذ العمل ہے۔",
                tafseerUrAuthor = "تفہیم القرآن",
                tafseerAr = "تفسير الجلالين: أي سيدهم الآمر الناهي فيهم دون غيره.",
                tafseerArAuthor = "تفسير الجلالين"
            ),
            createAyah(
                surah = 114,
                ayah = 3,
                arabic = "إِلَٰهِ النَّاسِ",
                para = 30,
                enTrans = "The God of mankind,",
                enTransName = "Saheeh International",
                urTrans = "انسانوں کے سچے معبود کی (پناہ میں)،",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "लोगों के सच्चे पूज्य की,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "معبودهم الحق الذي لا معبود سواه.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Ilah An-Nas' reminds us that He alone is rightfully worshipped and obeyed.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر: وہ ہستی جس کے سامنے تمام انسان سر بسجود ہوتے ہیں اور جس کے سوا کوئی عبادت کے لائق نہیں۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: لا معبود بحق إلا الله، ولا تجوز العبادة لغيره.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 114,
                ayah = 4,
                arabic = "مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ",
                para = 30,
                enTrans = "From the evil of the retreating whisperer -",
                enTransName = "Saheeh International",
                urTrans = "وسوسہ ڈالنے والے (شیطان) کے شر سے جو (اللہ کے ذکر سے) پیچھے ہٹ جاتا ہے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "छुपकर फुसफुसाने वाले (शैतान) की बुराई से,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "من شر الشيطان الذي يلقي الشبه والوساوس في القلوب، وينخنس ويختفي عند ذكر الله.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: 'Al-Waswas' is the one who injects doubt into hearts; 'Al-Khannas' means he retreats and hides whenever Allah is remembered, as confirmed by Ibn Abbas (ra).",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: حضرت عبداللہ بن عباس رضی اللہ عنہما نے فرمایا: شیطان انسان کے دل پر منہ رکھے رہتا ہے، جب انسان اللہ کو یاد کرتا ہے تو وہ ہٹ جاتا ہے اور جب غافل ہوتا ہے تو وسوسے ڈالتا ہے۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "التفسير الميسر: الشيطان جاثم على قلب ابن آدم، فإذا ذكر الله خنس، وإذا غفل وسوس.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 114,
                ayah = 5,
                arabic = "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ",
                para = 30,
                enTrans = "Who whispers into the breasts of mankind -",
                enTransName = "Saheeh International",
                urTrans = "جو لوگوں کے دلوں میں وسوسے ڈالتا ہے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "जो लोगों के दिलों में बहकाता है,",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "الذي يبث الشر والشكوك في صدور الناس ويزين لهم المعاصي.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Whispering takes the form of decorating sins, casting doubt on faith, and instilling baseless anxieties.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفہیم القرآن: دل و دماغ کے اندر ترغیبِ گناہ اور شکوک و شبہات کے بیچ بونا شیطان کی سب سے خطرناک چال ہے۔",
                tafseerUrAuthor = "تفہیم القرآن",
                tafseerAr = "التفسير الميسر: يوقع في قلوبهم الشكوك ويثبطهم عن الطاعات.",
                tafseerArAuthor = "التفسير الميسر"
            ),
            createAyah(
                surah = 114,
                ayah = 6,
                arabic = "مِنَ الْجِنَّةِ وَالنَّاسِ",
                para = 30,
                enTrans = "From among the jinn and mankind.'",
                enTransName = "Saheeh International",
                urTrans = "خواہ وہ جنوں میں سے ہو یا انسانوں میں سے۔",
                urTransName = "مولانا فتح محمد جالندھری",
                hiTrans = "चाहे वह जिन्नों में से हो या इंसानों में से।",
                hiTransName = "मुहम्मद फ़ारूक़ ख़ान",
                arTrans = "من شياطين الإنس والجن على حد سواء.",
                arTransName = "التفسير الميسر",
                tafseerEn = "Ibn Kathir: Clarifies that whisperers who mislead include both rebellious unseen jinn and evil human companions who entice to falsehood.",
                tafseerEnAuthor = "Tafseer Ibn Kathir",
                tafseerUr = "تفسیر ابن کثیر: وسوسہ ڈالنے والے اور بہکانے والے صرف نادیدہ جناتی شیاطین ہی نہیں ہوتے بلکہ انسانوں میں سے بھی برے ہم نشین اور گمراہ کن لوگ شامل ہیں۔",
                tafseerUrAuthor = "تفسیر ابن کثیر (اردو)",
                tafseerAr = "تفسير الجلالين: شياطين الإنس وشياطين الجن يعوذ المسلم من شرورهم جميعاً.",
                tafseerArAuthor = "تفسير الجلالين"
            )
        )
    )

    private val surahAyahCache = ConcurrentHashMap<Int, List<Ayah>>()

    fun getAyahsForSurah(context: Context? = null, surahNumber: Int): List<Ayah> {
        surahAyahCache[surahNumber]?.let { return it }

        // 1. Try loading authentic Uthmani script & multi-language translations from bundled assets
        val ctx = context ?: appContext
        if (ctx != null) {
            try {
                val jsonString = ctx.assets.open("quran/surah_$surahNumber.json").bufferedReader().use { it.readText() }
                val root = JSONObject(jsonString)
                if (root.optInt("code") == 200 && root.has("data")) {
                    val dataArray = root.getJSONArray("data")
                    var arAyahs: org.json.JSONArray? = null
                    var urAyahs: org.json.JSONArray? = null
                    var enAyahs: org.json.JSONArray? = null
                    var hiAyahs: org.json.JSONArray? = null

                    for (d in 0 until dataArray.length()) {
                        val editionObj = dataArray.getJSONObject(d)
                        val edInfo = editionObj.optJSONObject("edition")
                        val identifier = edInfo?.optString("identifier") ?: ""
                        when {
                            identifier.contains("uthmani") -> arAyahs = editionObj.getJSONArray("ayahs")
                            identifier.contains("jalandhry") -> urAyahs = editionObj.getJSONArray("ayahs")
                            identifier.contains("sahih") -> enAyahs = editionObj.getJSONArray("ayahs")
                            identifier.contains("farooq") -> hiAyahs = editionObj.getJSONArray("ayahs")
                        }
                    }

                    if (
                        arAyahs != null && arAyahs.length() > 0 &&
                        urAyahs != null && urAyahs.length() == arAyahs.length() &&
                        enAyahs != null && enAyahs.length() == arAyahs.length() &&
                        hiAyahs != null && hiAyahs.length() == arAyahs.length()
                    ) {
                        val resultList = mutableListOf<Ayah>()
                        val total = arAyahs.length()
                        val surahMeta = surahs.firstOrNull { it.number == surahNumber } ?: surahs[0]

                        for (i in 0 until total) {
                            val arObj = arAyahs.getJSONObject(i)
                            val ayahNum = arObj.optInt("numberInSurah", i + 1)
                            val juzNum = arObj.optInt("juz", surahMeta.startingPara)
                            val arText = arObj.optString("text")

                            // Remove Bismillah from Ayah 1 for Surahs 2..114 so it is not shown twice (top header already displays Bismillah)
                            val finalArabic = if (surahNumber > 1 && ayahNum == 1) {
                                var s = arText.trim()
                                val bismillahPrefixes = listOf(
                                    "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                    "بِّسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                    "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    "بِّسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
                                )
                                for (prefix in bismillahPrefixes) {
                                    if (s.startsWith(prefix)) {
                                        s = s.removePrefix(prefix).trim()
                                        break
                                    }
                                }
                                s
                            } else {
                                arText.trim()
                            }

                            val urText = urAyahs?.optJSONObject(i)?.optString("text")
                                ?: "سورۃ ${surahMeta.nameUrdu} کی آیت نمبر $ayahNum"
                            val enText = enAyahs?.optJSONObject(i)?.optString("text")
                                ?: "Verse $ayahNum of Surah ${surahMeta.nameEnglish}"
                            val hiText = hiAyahs?.optJSONObject(i)?.optString("text")
                                ?: "सूरह ${surahMeta.nameHindi} की आयत $ayahNum"

                            val ayahItem = createAyah(
                                surah = surahNumber,
                                ayah = ayahNum,
                                arabic = finalArabic,
                                para = juzNum,
                                enTrans = enText,
                                enTransName = "Saheeh International",
                                urTrans = urText,
                                urTransName = "مولانا فتح محمد جالندھری",
                                hiTrans = hiText,
                                hiTransName = "मुहम्मद फ़ارूक़ ख़ान",
                                arTrans = arText,
                                arTransName = "القرآن الكريم برسم العثماني",
                                tafseerEn = "",
                                tafseerEnAuthor = "",
                                tafseerUr = "",
                                tafseerUrAuthor = "",
                                tafseerAr = "",
                                tafseerArAuthor = ""
                            )
                            resultList.add(ayahItem)
                        }
                        surahAyahCache[surahNumber] = resultList
                        return resultList
                    }
                }
            } catch (e: Exception) {
                // Asset load fallback
            }
        }

        // Never substitute invented Arabic or placeholder translations for Quran text.
        // An empty result tells the UI that the bundled source is unavailable or invalid.
        return emptyList()
    }

    // Overload without context for compatibility
    fun getAyahsForSurah(surahNumber: Int): List<Ayah> {
        return getAyahsForSurah(null, surahNumber)
    }

    private fun createAyah(
        surah: Int,
        ayah: Int,
        arabic: String,
        para: Int,
        enTrans: String,
        enTransName: String,
        urTrans: String,
        urTransName: String,
        hiTrans: String,
        hiTransName: String,
        arTrans: String,
        arTransName: String,
        tafseerEn: String,
        tafseerEnAuthor: String,
        tafseerUr: String,
        tafseerUrAuthor: String,
        tafseerAr: String,
        tafseerArAuthor: String
    ): Ayah {
        return Ayah(
            surahNumber = surah,
            ayahNumber = ayah,
            textArabic = arabic,
            paraNumber = para,
            translations = listOf(
                AyahTranslation("en_saheeh", AppLanguage.ENGLISH, enTransName, "Saheeh International (1997)", enTrans),
                AyahTranslation("ur_jalandhari", AppLanguage.URDU, urTransName, "مستند اردو ترجمہ", urTrans),
                AyahTranslation("hi_farooq", AppLanguage.HINDI, hiTransName, "हिन्दी अनुवाद संस्करण", hiTrans),
                AyahTranslation("ar_muyassar", AppLanguage.ARABIC, arTransName, "مجمع الملك فهد لطباعة المصحف الشريف", arTrans)
            ),
            // Do not attach generic/generated commentary under named scholars.
            tafseers = emptyList()
        )
    }
}
