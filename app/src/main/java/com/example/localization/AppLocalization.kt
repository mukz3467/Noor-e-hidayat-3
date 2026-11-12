package com.example.localization

import com.example.data.model.AppLanguage

object AppLocalization {

    fun get(key: String, language: AppLanguage): String {
        return strings[key]?.get(language) ?: strings[key]?.get(AppLanguage.ENGLISH) ?: key
    }

    private val strings: Map<String, Map<AppLanguage, String>> = mapOf(
        "app_name" to mapOf(
            AppLanguage.ENGLISH to "Noor-e-Hidayat",
            AppLanguage.URDU to "نورِ ہدایت",
            AppLanguage.ARABIC to "نور الهداية",
            AppLanguage.HINDI to "नूर-ए-हिदायत"
        ),
        "app_tagline" to mapOf(
            AppLanguage.ENGLISH to "Your Authentic Islamic Companion",
            AppLanguage.URDU to "آپ کا مستند اور بااعتماد اسلامی رفیق",
            AppLanguage.ARABIC to "رفيقك الإسلامي الموثوق",
            AppLanguage.HINDI to "आपका प्रामाणिक इस्लामिक साथी"
        ),
        "home" to mapOf(
            AppLanguage.ENGLISH to "Home",
            AppLanguage.URDU to "صفحہ اول",
            AppLanguage.ARABIC to "الرئيسية",
            AppLanguage.HINDI to "होम"
        ),
        "quran" to mapOf(
            AppLanguage.ENGLISH to "Al-Quran",
            AppLanguage.URDU to "القرآن الکریم",
            AppLanguage.ARABIC to "القرآن الكريم",
            AppLanguage.HINDI to "अल-क़ुरआन"
        ),
        "live_stream" to mapOf(
            AppLanguage.ENGLISH to "Live Makkah & Madinah",
            AppLanguage.URDU to "لائیو مکہ و مدینہ",
            AppLanguage.ARABIC to "بث مباشر مكة والمدينة",
            AppLanguage.HINDI to "लाइव मक्का और मदीना"
        ),
        "lectures" to mapOf(
            AppLanguage.ENGLISH to "Islamic Lectures",
            AppLanguage.URDU to "اسلامی بیانات و دروس",
            AppLanguage.ARABIC to "المحاضرات والدروس الإسلامية",
            AppLanguage.HINDI to "इस्लामिक व्याख्यान"
        ),
        "learning" to mapOf(
            AppLanguage.ENGLISH to "Quran Learning",
            AppLanguage.URDU to "تعلیم القرآن و تجوید",
            AppLanguage.ARABIC to "تعليم القرآن والتجويد",
            AppLanguage.HINDI to "क़ुरआन शिक्षण"
        ),
        "more_options" to mapOf(
            AppLanguage.ENGLISH to "More Options",
            AppLanguage.URDU to "مزید سہولیات و خصوصیات",
            AppLanguage.ARABIC to "المزيد من الخدمات",
            AppLanguage.HINDI to "अधिक विकल्प"
        ),
        "hadith" to mapOf(
            AppLanguage.ENGLISH to "Hadith Collection",
            AppLanguage.URDU to "کتبِ احادیث مبارکہ",
            AppLanguage.ARABIC to "موسوعة الحديث الشريف",
            AppLanguage.HINDI to "हदीस संग्रह"
        ),
        "tafseer" to mapOf(
            AppLanguage.ENGLISH to "Tafseer Library",
            AppLanguage.URDU to "مکتبۂ تفاسیر",
            AppLanguage.ARABIC to "مكتبة التفاسير",
            AppLanguage.HINDI to "तफ़सीर लाइब्रेरी"
        ),
        "duas" to mapOf(
            AppLanguage.ENGLISH to "Islamic Duas",
            AppLanguage.URDU to "مسنون و قرآنی دعائیں",
            AppLanguage.ARABIC to "الأدعية والأذكار",
            AppLanguage.HINDI to "इस्लामिक दुआएं"
        ),
        "tasbeeh" to mapOf(
            AppLanguage.ENGLISH to "Tasbeeh Counter",
            AppLanguage.URDU to "ڈیجیٹل تسبیح کاؤنٹر",
            AppLanguage.ARABIC to "المسبحة الإلكترونية",
            AppLanguage.HINDI to "तस्बीह काउंटर"
        ),
        "prayer_times" to mapOf(
            AppLanguage.ENGLISH to "Prayer Times",
            AppLanguage.URDU to "اوقاتِ نماز",
            AppLanguage.ARABIC to "مواقيت الصلاة",
            AppLanguage.HINDI to "नमाज़ के औक़ात"
        ),
        "azan_notifications" to mapOf(
            AppLanguage.ENGLISH to "Azan Notifications",
            AppLanguage.URDU to "اذان الرٹس و اطلاعات",
            AppLanguage.ARABIC to "تنبيهات الأذان",
            AppLanguage.HINDI to "अज़ान सूचनाएं"
        ),
        "qibla" to mapOf(
            AppLanguage.ENGLISH to "Qibla Finder",
            AppLanguage.URDU to "قبلہ نما (قطب نما)",
            AppLanguage.ARABIC to "بوصلة القبلة",
            AppLanguage.HINDI to "क़िबला कम्पास"
        ),
        "zakat" to mapOf(
            AppLanguage.ENGLISH to "Zakat Calculator",
            AppLanguage.URDU to "زکوٰۃ کیلکولیٹر",
            AppLanguage.ARABIC to "حاسبة الزكاة",
            AppLanguage.HINDI to "ज़कात कैलकुलेटर"
        ),
        "calendar" to mapOf(
            AppLanguage.ENGLISH to "Hijri Calendar",
            AppLanguage.URDU to "ہجری تقویم",
            AppLanguage.ARABIC to "التقويم الهجري",
            AppLanguage.HINDI to "हिजरी कैलेंडर"
        ),
        "podcasts" to mapOf(
            AppLanguage.ENGLISH to "Islamic Podcasts",
            AppLanguage.URDU to "اسلامی پوڈکاسٹس",
            AppLanguage.ARABIC to "بودكاست إسلامي",
            AppLanguage.HINDI to "इस्लामिक पॉडकास्ट"
        ),
        "bookmarks" to mapOf(
            AppLanguage.ENGLISH to "Bookmarks",
            AppLanguage.URDU to "محفوظ شدہ (بک مارکس)",
            AppLanguage.ARABIC to "الإشارات المرجعية",
            AppLanguage.HINDI to "बुकमार्क"
        ),
        "downloads" to mapOf(
            AppLanguage.ENGLISH to "Downloaded Content",
            AppLanguage.URDU to "ڈاؤنلوڈ شدہ مواد",
            AppLanguage.ARABIC to "التنزيلات",
            AppLanguage.HINDI to "डाउनलोड किया गया"
        ),
        "settings" to mapOf(
            AppLanguage.ENGLISH to "App Settings",
            AppLanguage.URDU to "ترتیبات",
            AppLanguage.ARABIC to "الإعدادات",
            AppLanguage.HINDI to "सेटिंग्स"
        ),
        "next_prayer" to mapOf(
            AppLanguage.ENGLISH to "Next Prayer",
            AppLanguage.URDU to "اگلی نماز",
            AppLanguage.ARABIC to "الصلاة القادمة",
            AppLanguage.HINDI to "अगली नमाज़"
        ),
        "fajr" to mapOf(
            AppLanguage.ENGLISH to "Fajr",
            AppLanguage.URDU to "فجر",
            AppLanguage.ARABIC to "الفجر",
            AppLanguage.HINDI to "फ़ज्र"
        ),
        "sunrise" to mapOf(
            AppLanguage.ENGLISH to "Sunrise",
            AppLanguage.URDU to "طلوعِ آفتاب",
            AppLanguage.ARABIC to "الشروق",
            AppLanguage.HINDI to "शुरूक़ (सूर्योदय)"
        ),
        "dhuhr" to mapOf(
            AppLanguage.ENGLISH to "Dhuhr",
            AppLanguage.URDU to "ظہر",
            AppLanguage.ARABIC to "الظهر",
            AppLanguage.HINDI to "ज़ुहर"
        ),
        "asr" to mapOf(
            AppLanguage.ENGLISH to "Asr",
            AppLanguage.URDU to "عصر",
            AppLanguage.ARABIC to "العصر",
            AppLanguage.HINDI to "अस्र"
        ),
        "maghrib" to mapOf(
            AppLanguage.ENGLISH to "Maghrib",
            AppLanguage.URDU to "مغرب",
            AppLanguage.ARABIC to "المغرب",
            AppLanguage.HINDI to "मग़रिब"
        ),
        "isha" to mapOf(
            AppLanguage.ENGLISH to "Isha",
            AppLanguage.URDU to "عشاء",
            AppLanguage.ARABIC to "العشاء",
            AppLanguage.HINDI to "इशा"
        ),
        "surah" to mapOf(
            AppLanguage.ENGLISH to "Surah",
            AppLanguage.URDU to "سورۃ",
            AppLanguage.ARABIC to "سورة",
            AppLanguage.HINDI to "सूरह"
        ),
        "para" to mapOf(
            AppLanguage.ENGLISH to "Para (Juz)",
            AppLanguage.URDU to "پارہ (جزء)",
            AppLanguage.ARABIC to "جزء",
            AppLanguage.HINDI to "पारा (जुज़)"
        ),
        "makki" to mapOf(
            AppLanguage.ENGLISH to "Makki",
            AppLanguage.URDU to "مکی",
            AppLanguage.ARABIC to "مكية",
            AppLanguage.HINDI to "मक्की"
        ),
        "madani" to mapOf(
            AppLanguage.ENGLISH to "Madani",
            AppLanguage.URDU to "مدنی",
            AppLanguage.ARABIC to "مدنية",
            AppLanguage.HINDI to "मदनी"
        ),
        "ayah_count" to mapOf(
            AppLanguage.ENGLISH to "Verses",
            AppLanguage.URDU to "آیات",
            AppLanguage.ARABIC to "آيات",
            AppLanguage.HINDI to "आयात"
        ),
        "translation_btn" to mapOf(
            AppLanguage.ENGLISH to "Translation",
            AppLanguage.URDU to "ترجمہ",
            AppLanguage.ARABIC to "الترجمة",
            AppLanguage.HINDI to "अनुवाद (तर्जुमा)"
        ),
        "tafseer_btn" to mapOf(
            AppLanguage.ENGLISH to "Tafseer",
            AppLanguage.URDU to "تفسیر",
            AppLanguage.ARABIC to "التفسير",
            AppLanguage.HINDI to "तफ़सीर (विस्तार)"
        ),
        "reciter" to mapOf(
            AppLanguage.ENGLISH to "Reciter",
            AppLanguage.URDU to "قاری",
            AppLanguage.ARABIC to "القارئ",
            AppLanguage.HINDI to "क़ारी"
        ),
        "audio_download" to mapOf(
            AppLanguage.ENGLISH to "Download Audio",
            AppLanguage.URDU to "آڈیو ڈاؤنلوڈ کریں",
            AppLanguage.ARABIC to "تحميل التلاوة",
            AppLanguage.HINDI to "ऑडियो डाउनलोड करें"
        ),
        "copied" to mapOf(
            AppLanguage.ENGLISH to "Ayah copied to clipboard",
            AppLanguage.URDU to "آیت کاپی کر لی گئی",
            AppLanguage.ARABIC to "تم نسخ الآية",
            AppLanguage.HINDI to "आयत कॉपी कर ली गई"
        ),
        "bookmarked" to mapOf(
            AppLanguage.ENGLISH to "Saved to bookmarks",
            AppLanguage.URDU to "بک مارک میں شامل ہو گیا",
            AppLanguage.ARABIC to "تمت الإضافة للمفضلة",
            AppLanguage.HINDI to "बुकमार्क में सहेजा गया"
        ),
        "search_hint" to mapOf(
            AppLanguage.ENGLISH to "Search by name or number...",
            AppLanguage.URDU to "نام یا نمبر کے ذریعے تلاش کریں...",
            AppLanguage.ARABIC to "ابحث بالاسم أو الرقم...",
            AppLanguage.HINDI to "नाम या संख्या द्वारा खोजें..."
        ),
        "theme_selector" to mapOf(
            AppLanguage.ENGLISH to "Select Theme",
            AppLanguage.URDU to "تھیم منتخب کریں",
            AppLanguage.ARABIC to "اختر المظهر",
            AppLanguage.HINDI to "थीम चुनें"
        ),
        "language_selector" to mapOf(
            AppLanguage.ENGLISH to "Select Language",
            AppLanguage.URDU to "زبان کا انتخاب",
            AppLanguage.ARABIC to "اختر اللغة",
            AppLanguage.HINDI to "भाषा चुनें"
        ),
        "font_size" to mapOf(
            AppLanguage.ENGLISH to "Arabic Font Size",
            AppLanguage.URDU to "عربی فونٹ سائز",
            AppLanguage.ARABIC to "حجم الخط القرآني",
            AppLanguage.HINDI to "अरबी फ़ॉन्ट आकार"
        ),
        "live_makkah_title" to mapOf(
            AppLanguage.ENGLISH to "Makkah Live (Masjid al-Haram)",
            AppLanguage.URDU to "مکہ مکرمہ لائیو (مسجد الحرام)",
            AppLanguage.ARABIC to "بث مباشر مكة المكرمة (المسجد الحرام)",
            AppLanguage.HINDI to "मक्का लाइव (मस्जिद अल-हरम)"
        ),
        "live_madinah_title" to mapOf(
            AppLanguage.ENGLISH to "Madinah Live (Masjid an-Nabawi)",
            AppLanguage.URDU to "مدینہ منورہ لائیو (مسجد نبوی ﷺ)",
            AppLanguage.ARABIC to "بث مباشر المدينة المنورة (المسجد النبوي)",
            AppLanguage.HINDI to "मदीना लाइव (मस्जिद अन-नबवी)"
        ),
        "official_source" to mapOf(
            AppLanguage.ENGLISH to "Official Saudi Broadcast Channel",
            AppLanguage.URDU to "سرکاری سعودی نشریاتی چینل",
            AppLanguage.ARABIC to "القناة الرسمية لهيئة البث والإذاعة السعودية",
            AppLanguage.HINDI to "आधिकारिक सऊदी प्रसारण चैनल"
        ),
        "bukhari" to mapOf(
            AppLanguage.ENGLISH to "Sahih al-Bukhari",
            AppLanguage.URDU to "صحیح البخاری",
            AppLanguage.ARABIC to "صحيح البخاري",
            AppLanguage.HINDI to "सहीह अल-बुख़ारी"
        ),
        "muslim" to mapOf(
            AppLanguage.ENGLISH to "Sahih Muslim",
            AppLanguage.URDU to "صحیح مسلم",
            AppLanguage.ARABIC to "صحيح مسلم",
            AppLanguage.HINDI to "सहीह मुस्लिम"
        ),
        "qibla_compass_bearing" to mapOf(
            AppLanguage.ENGLISH to "Qibla Bearing",
            AppLanguage.URDU to "قبلہ زاویہ",
            AppLanguage.ARABIC to "زاوية القبلة",
            AppLanguage.HINDI to "क़िबला कोण"
        ),
        "zakat_disclaimer" to mapOf(
            AppLanguage.ENGLISH to "Note: This calculator assists in computing your estimated Zakat (standard 2.5% on qualifying wealth above Nisab). For complex business structures or personalized rulings, consult a qualified Islamic scholar.",
            AppLanguage.URDU to "وضاحت: یہ کیلکولیٹر نصاب سے زائد قابل زکوٰۃ مال پر مروجہ 2.5 فیصد کے مطابق تخمینہ لگاتا ہے۔ تفصیلی اور پیچیدہ مالی مسائل میں مستند علمائے کرام سے رہنمائی لیں۔",
            AppLanguage.ARABIC to "ملاحظة: تساعد هذه الحاسبة في تقدير زكاتك (2.5٪ من المال البالغ للنصاب). للحالات المعقدة، يُرجى استشارة عالم شرعي معتمد.",
            AppLanguage.HINDI to "नोट: यह कैलकुलेटर निसाब से अधिक पात्र संपत्ति पर 2.5% की दर से आपकी अनुमानित ज़कात की गणना करता है। व्यक्तिगत मसलों के लिए किसी योग्य विद्वान से परामर्श करें।"
        ),
        "calculate" to mapOf(
            AppLanguage.ENGLISH to "Calculate Zakat",
            AppLanguage.URDU to "زکوٰۃ کا حساب لگائیں",
            AppLanguage.ARABIC to "احسب الزكاة",
            AppLanguage.HINDI to "ज़कात की गणना करें"
        ),
        "offline_ready" to mapOf(
            AppLanguage.ENGLISH to "Available Offline",
            AppLanguage.URDU to "آف لائن دستیاب",
            AppLanguage.ARABIC to "متاح دون اتصال",
            AppLanguage.HINDI to "ऑफ़लाइन उपलब्ध"
        ),
        "tap_to_count" to mapOf(
            AppLanguage.ENGLISH to "TAP TO COUNT",
            AppLanguage.URDU to "شمار کرنے کے لیے چھوئیں",
            AppLanguage.ARABIC to "اضغط للتسبيح",
            AppLanguage.HINDI to "गिनने के लिए टैप करें"
        ),
        "target" to mapOf(
            AppLanguage.ENGLISH to "Target",
            AppLanguage.URDU to "ہدف",
            AppLanguage.ARABIC to "الهدف",
            AppLanguage.HINDI to "लक्ष्य"
        ),
        "target_completed" to mapOf(
            AppLanguage.ENGLISH to "Target Completed!",
            AppLanguage.URDU to "ماشاء اللہ! ہدف مکمل ہوا",
            AppLanguage.ARABIC to "ما شاء الله! اكتمل الهدف",
            AppLanguage.HINDI to "माशाअल्लाह! लक्ष्य पूरा हुआ"
        ),
        "rounds" to mapOf(
            AppLanguage.ENGLISH to "Round",
            AppLanguage.URDU to "چکر",
            AppLanguage.ARABIC to "الدورة",
            AppLanguage.HINDI to "राउंड"
        ),
        "undo" to mapOf(
            AppLanguage.ENGLISH to "Undo",
            AppLanguage.URDU to "ایک عدد واپس",
            AppLanguage.ARABIC to "تراجع",
            AppLanguage.HINDI to "वापस लें"
        ),
        "reset" to mapOf(
            AppLanguage.ENGLISH to "Reset",
            AppLanguage.URDU to "دوبارہ شروع",
            AppLanguage.ARABIC to "إعادة ضبط",
            AppLanguage.HINDI to "रीसेट"
        ),
        "sound" to mapOf(
            AppLanguage.ENGLISH to "Sound",
            AppLanguage.URDU to "آواز",
            AppLanguage.ARABIC to "الصوت",
            AppLanguage.HINDI to "ध्वनि"
        ),
        "vibration" to mapOf(
            AppLanguage.ENGLISH to "Vibration",
            AppLanguage.URDU to "ارتعاش",
            AppLanguage.ARABIC to "الاهتزاز",
            AppLanguage.HINDI to "कंपन"
        ),
        "screen_awake" to mapOf(
            AppLanguage.ENGLISH to "Screen Awake",
            AppLanguage.URDU to "اسکرین آن رکھیں",
            AppLanguage.ARABIC to "إبقاء الشاشة مفعلة",
            AppLanguage.HINDI to "स्क्रीन चालू रखें"
        ),
        "history" to mapOf(
            AppLanguage.ENGLISH to "History",
            AppLanguage.URDU to "تاریخچہ",
            AppLanguage.ARABIC to "السجل",
            AppLanguage.HINDI to "इतिहास"
        ),
        "statistics" to mapOf(
            AppLanguage.ENGLISH to "Statistics",
            AppLanguage.URDU to "اعداد و شمار",
            AppLanguage.ARABIC to "الإحصائيات",
            AppLanguage.HINDI to "सांख्यिकी"
        ),
        "today_total" to mapOf(
            AppLanguage.ENGLISH to "Today's Total",
            AppLanguage.URDU to "آج کا کل شمار",
            AppLanguage.ARABIC to "مجموع اليوم",
            AppLanguage.HINDI to "आज का कुल"
        ),
        "all_time_total" to mapOf(
            AppLanguage.ENGLISH to "All-Time Total",
            AppLanguage.URDU to "ہمیشہ کا کل شمار",
            AppLanguage.ARABIC to "الإجمالي الكلي",
            AppLanguage.HINDI to "कुल पाठ"
        ),
        "custom_target" to mapOf(
            AppLanguage.ENGLISH to "Custom Target",
            AppLanguage.URDU to "اپنی مرضی کا ہدف",
            AppLanguage.ARABIC to "هدف مخصص",
            AppLanguage.HINDI to "कस्टम लक्ष्य"
        ),
        "add_custom_dhikr" to mapOf(
            AppLanguage.ENGLISH to "Add Custom Zikr",
            AppLanguage.URDU to "نیا ذکر شامل کریں",
            AppLanguage.ARABIC to "إضافة ذكر مخصص",
            AppLanguage.HINDI to "नया ज़िक्र जोड़ें"
        ),
        "change_dhikr" to mapOf(
            AppLanguage.ENGLISH to "Change Zikr",
            AppLanguage.URDU to "ذکر تبدیل کریں",
            AppLanguage.ARABIC to "تغيير الذكر",
            AppLanguage.HINDI to "ज़िक्र बदलें"
        ),
        "save_session" to mapOf(
            AppLanguage.ENGLISH to "Save Session",
            AppLanguage.URDU to "شمار محفوظ کریں",
            AppLanguage.ARABIC to "حفظ الجلسة",
            AppLanguage.HINDI to "सत्र सहेजें"
        ),
        "search_duas_hint" to mapOf(
            AppLanguage.ENGLISH to "Search authentic duas, topics or Arabic...",
            AppLanguage.URDU to "مسنون دعائیں، الفاظ یا عربی تلاش کریں...",
            AppLanguage.ARABIC to "ابحث في الأدعية المأثورة أو الكلمات...",
            AppLanguage.HINDI to "प्रमाणिक दुआएं, विषय या अरबी खोजें..."
        ),
        "favorite_duas" to mapOf(
            AppLanguage.ENGLISH to "Favorites",
            AppLanguage.URDU to "پسندیدہ دعائیں",
            AppLanguage.ARABIC to "الأدعية المفضلة",
            AppLanguage.HINDI to "पसंदीदा दुआएं"
        ),
        "recent_duas" to mapOf(
            AppLanguage.ENGLISH to "Recently Viewed",
            AppLanguage.URDU to "حالیہ دیکھی گئی دعائیں",
            AppLanguage.ARABIC to "شوهدت مؤخراً",
            AppLanguage.HINDI to "हाल ही में देखी गई"
        ),
        "daily_featured_dua" to mapOf(
            AppLanguage.ENGLISH to "Daily Authentic Dua",
            AppLanguage.URDU to "آج کی منتخب مسنون دعا",
            AppLanguage.ARABIC to "دعاء اليوم المأثور",
            AppLanguage.HINDI to "आज की चयनित मसनून दुआ"
        ),
        "all_duas" to mapOf(
            AppLanguage.ENGLISH to "All Duas",
            AppLanguage.URDU to "تمام دعائیں",
            AppLanguage.ARABIC to "جميع الأدعية",
            AppLanguage.HINDI to "सभी दुआएं"
        ),
        "arabic_font_size" to mapOf(
            AppLanguage.ENGLISH to "Arabic Font Size",
            AppLanguage.URDU to "عربی فونٹ کا سائز",
            AppLanguage.ARABIC to "حجم الخط العربي",
            AppLanguage.HINDI to "अरबी फ़ॉन्ट का आकार"
        ),
        "play_recitation" to mapOf(
            AppLanguage.ENGLISH to "Play Audio",
            AppLanguage.URDU to "تلاوت سنیں",
            AppLanguage.ARABIC to "استماع للتلاوة",
            AppLanguage.HINDI to "ऑडियो सुनें"
        ),
        "pause_recitation" to mapOf(
            AppLanguage.ENGLISH to "Pause",
            AppLanguage.URDU to "وقفہ کریں",
            AppLanguage.ARABIC to "إيقاف مؤقت",
            AppLanguage.HINDI to "रोकें"
        ),
        "repeat_playback" to mapOf(
            AppLanguage.ENGLISH to "Repeat",
            AppLanguage.URDU to "تکرار",
            AppLanguage.ARABIC to "تكرار",
            AppLanguage.HINDI to "दोहराएं"
        ),
        "hadith_authenticity" to mapOf(
            AppLanguage.ENGLISH to "Authenticity Grade",
            AppLanguage.URDU to "درجۂ صحت",
            AppLanguage.ARABIC to "درجة الحديث",
            AppLanguage.HINDI to "प्रमाणिकता श्रेणी"
        ),
        "reference_source" to mapOf(
            AppLanguage.ENGLISH to "Source Reference",
            AppLanguage.URDU to "مستند حوالہ",
            AppLanguage.ARABIC to "المصدر والتخريج",
            AppLanguage.HINDI to "प्रमाणिक संदर्भ"
        ),
        "recommended_time" to mapOf(
            AppLanguage.ENGLISH to "Occasion & Time",
            AppLanguage.URDU to "موقع و مناسب وقت",
            AppLanguage.ARABIC to "المناسبة والوقت",
            AppLanguage.HINDI to "अवसर और समय"
        ),
        "virtues_benefits" to mapOf(
            AppLanguage.ENGLISH to "Virtues & Benefits",
            AppLanguage.URDU to "فضائل و برکات",
            AppLanguage.ARABIC to "الفضائل والأجر",
            AppLanguage.HINDI to "फ़ज़ीलत और लाभ"
        ),
        "copy_dua" to mapOf(
            AppLanguage.ENGLISH to "Copy Dua",
            AppLanguage.URDU to "دعا کاپی کریں",
            AppLanguage.ARABIC to "نسخ الدعاء",
            AppLanguage.HINDI to "दुआ कॉपी करें"
        ),
        "share_dua" to mapOf(
            AppLanguage.ENGLISH to "Share Dua",
            AppLanguage.URDU to "دعا شیئر کریں",
            AppLanguage.ARABIC to "مشاركة الدعاء",
            AppLanguage.HINDI to "दुआ शेयर करें"
        ),
        "dua_added_fav" to mapOf(
            AppLanguage.ENGLISH to "Added to Favorites",
            AppLanguage.URDU to "پسندیدہ میں شامل کر دیا گیا",
            AppLanguage.ARABIC to "تمت الإضافة إلى المفضلة",
            AppLanguage.HINDI to "पसंदीदा में जोड़ा गया"
        ),
        "dua_removed_fav" to mapOf(
            AppLanguage.ENGLISH to "Removed from Favorites",
            AppLanguage.URDU to "پسندیدہ سے ہٹا دیا گیا",
            AppLanguage.ARABIC to "تم الحذف من المفضلة",
            AppLanguage.HINDI to "पसंदीदा से हटाया गया"
        ),
        "dua_copied" to mapOf(
            AppLanguage.ENGLISH to "Dua copied to clipboard",
            AppLanguage.URDU to "دعا کلپ بورڈ پر کاپی ہو گئی",
            AppLanguage.ARABIC to "تم نسخ الدعاء إلى الحافظة",
            AppLanguage.HINDI to "दुआ क्लिपबोर्ड पर कॉपी हो गई"
        )
    )
}
