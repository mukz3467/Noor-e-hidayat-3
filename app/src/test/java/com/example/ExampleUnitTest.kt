package com.example

import com.example.data.model.NisabType
import com.example.data.model.ZakatCalculationInput
import com.example.data.repository.HadithData
import com.example.data.repository.MediaAndLearningData
import com.example.data.repository.PrayerCalculator
import com.example.data.repository.QuranData
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testQuranSurahIntegrity() {
        // Must contain all 114 Surahs
        assertEquals(114, QuranData.surahs.size)
        // First is Al-Fatiha
        assertEquals("Al-Fatihah", QuranData.surahs[0].nameEnglish)
        assertEquals(1, QuranData.surahs[0].number)
        assertEquals(7, QuranData.surahs[0].ayahCount)

        // Last is An-Nas
        assertEquals("An-Nas", QuranData.surahs[113].nameEnglish)
        assertEquals(114, QuranData.surahs[113].number)
        assertEquals(6, QuranData.surahs[113].ayahCount)

        // Verify ordering
        for (i in 0 until 114) {
            assertEquals(i + 1, QuranData.surahs[i].number)
        }
    }

    @Test
    fun testQuranParaIntegrity() {
        // Must contain all 30 Paras (Juz)
        assertEquals(30, QuranData.paras.size)
        assertEquals(1, QuranData.paras[0].number)
        assertEquals(30, QuranData.paras[29].number)
    }

    @Test
    fun testAyahTranslationAndTafseerSystem() {
        val fatihaAyahs = QuranData.getAyahsForSurah(1)
        assertEquals(7, fatihaAyahs.size)

        fatihaAyahs.forEach { ayah ->
            // Must have separate translations
            assertTrue("Translations must not be empty", ayah.translations.isNotEmpty())
            // Must have separate verified Tafseers
            assertTrue("Tafseers must not be empty", ayah.tafseers.isNotEmpty())

            // Verify author and translator metadata
            val firstTrans = ayah.translations[0]
            assertNotNull(firstTrans.translatorName)
            assertNotNull(firstTrans.editionName)

            val firstTafseer = ayah.tafseers[0]
            assertNotNull(firstTafseer.authorName)
            assertNotNull(firstTafseer.bookTitle)
        }
    }

    @Test
    fun testHadithCollections() {
        assertTrue(HadithData.hadiths.isNotEmpty())
        val bukhariHadiths = HadithData.hadiths.filter { it.collection == "Sahih al-Bukhari" }
        val muslimHadiths = HadithData.hadiths.filter { it.collection == "Sahih Muslim" }

        assertTrue("Must include Sahih al-Bukhari", bukhariHadiths.isNotEmpty())
        assertTrue("Must include Sahih Muslim", muslimHadiths.isNotEmpty())

        bukhariHadiths.forEach { h ->
            assertNotNull(h.textArabic)
            assertNotNull(h.textEnglish)
            assertNotNull(h.textUrdu)
            assertEquals("Sahih", h.grade.take(5))
        }
    }

    @Test
    fun testPrayerCalculationEngine() {
        val schedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 24.8607,
            longitude = 67.0011,
            method = PrayerCalculator.CalculationMethod.KARACHI,
            cityName = "Karachi, Pakistan"
        )
        assertNotNull(schedule.fajr)
        assertNotNull(schedule.sunrise)
        assertNotNull(schedule.dhuhr)
        assertNotNull(schedule.asr)
        assertNotNull(schedule.maghrib)
        assertNotNull(schedule.isha)

        assertTrue(schedule.fajr.contains("AM"))
        assertTrue(schedule.maghrib.contains("PM"))
    }

    @Test
    fun testQiblaBearingCalculation() {
        // Karachi to Kaaba is approx 258 degrees
        val bearing = PrayerCalculator.calculateQiblaBearing(24.8607, 67.0011)
        assertTrue("Karachi Qibla bearing should be between 250 and 270", bearing in 250.0..270.0)
    }

    @Test
    fun testZakatCalculation() {
        // Test case 1: Wealth below Nisab
        val belowNisabInput = ZakatCalculationInput(
            cashOnHand = 100.0,
            goldGramPrice = 75.0,
            silverGramPrice = 0.95,
            selectedNisabType = NisabType.SILVER
        )
        val resultBelow = MediaAndLearningData.calculateZakat(belowNisabInput)
        assertFalse(resultBelow.isEligible)
        assertEquals(0.0, resultBelow.zakatPayable, 0.001)

        // Test case 2: Wealth above Nisab (e.g. $10,000 cash)
        val aboveNisabInput = ZakatCalculationInput(
            cashOnHand = 10000.0,
            goldGramPrice = 75.0,
            silverGramPrice = 0.95,
            selectedNisabType = NisabType.SILVER
        )
        val resultAbove = MediaAndLearningData.calculateZakat(aboveNisabInput)
        assertTrue(resultAbove.isEligible)
        // 2.5% of 10,000 = 250.0
        assertEquals(250.0, resultAbove.zakatPayable, 0.001)
    }

    @Test
    fun testYouTubeRepositoryCuratedContent() {
        val curated = com.example.data.repository.YouTubeRepository.CURATED_SCHOLAR_VIDEOS
        assertTrue("Must have Israr Ahmed videos", curated.containsKey("israr"))
        assertTrue("Must have Tariq Masood videos", curated.containsKey("tariq_masood"))
        assertTrue("Must have Tariq Jameel videos", curated.containsKey("tariq_jameel"))
        assertTrue("Must have Mufti Menk videos", curated.containsKey("menk"))

        val liveStreams = com.example.data.repository.YouTubeRepository.HARAMEYN_LIVE_STREAMS
        assertTrue("Must have Makkah live streams", liveStreams.containsKey("makkah"))
        assertTrue("Must have Madinah live streams", liveStreams.containsKey("madinah"))

        assertTrue("All curated lectures must be populated", com.example.data.repository.YouTubeRepository.ALL_CURATED_LECTURES.isNotEmpty())
    }

    @Test
    fun testTop5ScholarsInAllLanguages() {
        val languages = listOf(
            com.example.data.model.AppLanguage.URDU,
            com.example.data.model.AppLanguage.ENGLISH,
            com.example.data.model.AppLanguage.HINDI,
            com.example.data.model.AppLanguage.ARABIC
        )

        languages.forEach { lang ->
            val scholars = com.example.data.repository.QuranTafseerRepository.getTop5Scholars(lang)
            assertEquals("Must have exactly 5 scholars in ${lang.displayName}", 5, scholars.size)
            val keys = scholars.map { it.key }
            assertTrue(keys.contains("ibn_kathir"))
            assertTrue(keys.contains("maududi"))
            assertTrue(keys.contains("maariful_quran"))
            assertTrue(keys.contains("jalalayn"))
            assertTrue(keys.contains("saadi"))
        }
    }

    @Test
    fun testDetailedScholarTafseerGeneration() {
        val tafseer = com.example.data.repository.QuranTafseerRepository.getScholarTafseer(
            surahNumber = 3,
            ayahNumber = 1,
            scholarKey = "ibn_kathir",
            language = com.example.data.model.AppLanguage.URDU
        )
        assertNotNull(tafseer.authorName)
        assertNotNull(tafseer.bookTitle)
        assertTrue("Tafseer commentary must be comprehensive", tafseer.tafseerText.length > 100)
        assertTrue("Must include key takeaways", tafseer.keyPoints.isNotEmpty())
    }

    @Test
    fun testRevelationContext4Points() {
        val ctx = com.example.data.repository.QuranTafseerRepository.getRevelationContext(
            surahNumber = 1,
            ayahNumber = 1,
            language = com.example.data.model.AppLanguage.URDU
        )
        assertNotNull(ctx.sababNuzool)
        assertNotNull(ctx.zamanaNuzool)
        assertNotNull(ctx.maqsadHikmat)
        assertNotNull(ctx.fazaailBenefits)
        assertTrue("Must explain why it was revealed", ctx.sababNuzool.isNotBlank())
        assertTrue("Must explain when it was revealed", ctx.zamanaNuzool.isNotBlank())
        assertTrue("Must explain divine purpose", ctx.maqsadHikmat.isNotBlank())
        assertTrue("Must explain recitation virtues & benefits", ctx.fazaailBenefits.isNotBlank())
    }

    @Test
    fun testDistinctTafseerForDifferentAyahs() {
        val fatihaAyahs = com.example.data.repository.QuranData.getAyahsForSurah(1)
        val fatihaAyah1 = fatihaAyahs[0]
        val fatihaAyah2 = fatihaAyahs[1]

        val tafseer1 = com.example.data.repository.QuranTafseerRepository.getScholarTafseer(
            ayah = fatihaAyah1,
            scholarKey = "ibn_kathir",
            language = com.example.data.model.AppLanguage.URDU
        )
        val tafseer2 = com.example.data.repository.QuranTafseerRepository.getScholarTafseer(
            ayah = fatihaAyah2,
            scholarKey = "ibn_kathir",
            language = com.example.data.model.AppLanguage.URDU
        )

        assertNotEquals("Tafseer for different ayahs must not be identical", tafseer1.tafseerText, tafseer2.tafseerText)
    }

    @Test
    fun testDua12CategoriesIntegrity() {
        val categories = com.example.data.repository.DuaData.categories
        assertEquals("Must have exactly 12 authentic Dua categories", 12, categories.size)

        val requiredCategoryIds = listOf(
            "morning_evening",
            "namaz_worship",
            "forgiveness_repentance",
            "quranic_duas",
            "family_personal",
            "health_protection",
            "special_situations",
            "ramadan_fasting",
            "hajj_umrah",
            "travel_duas",
            "food_daily",
            "hardship_anxiety"
        )

        val categoryIds = categories.map { it.id }
        requiredCategoryIds.forEach { reqId ->
            assertTrue("Must include category $reqId", categoryIds.contains(reqId))
        }

        // Verify that every category has authentic duas assigned
        val allDuas = com.example.data.repository.DuaData.duas
        assertTrue("Duas collection must not be empty", allDuas.isNotEmpty())

        requiredCategoryIds.forEach { reqId ->
            val countInCat = allDuas.count { it.categoryId == reqId }
            assertTrue("Category $reqId must have authentic duas (found $countInCat)", countInCat > 0)
        }
    }

    @Test
    fun testDuaAuthenticSourcesAndGrading() {
        val allDuas = com.example.data.repository.DuaData.duas

        allDuas.forEach { dua ->
            assertFalse("Arabic text must not be blank", dua.arabic.isBlank())
            assertFalse("Transliteration must not be blank", dua.transliteration.isBlank())
            assertFalse("Reference citation must not be blank", dua.reference.isBlank())
            assertFalse("Hadith authenticity grade must not be blank", dua.grade.isBlank())

            // Authenticity check: grade must mention Sahih, Hasan, Muttafaqun, or Quranic
            val isAuthenticGrade = dua.grade.contains("Sahih", ignoreCase = true) ||
                    dua.grade.contains("Hasan", ignoreCase = true) ||
                    dua.grade.contains("Muttafaq", ignoreCase = true) ||
                    dua.grade.contains("Quranic", ignoreCase = true) ||
                    dua.grade.contains("صحيح") ||
                    dua.grade.contains("آية")
            assertTrue("Dua ${dua.id} must have verified authentic grading (found: ${dua.grade})", isAuthenticGrade)
        }
    }

    @Test
    fun testDua4LanguagesTranslations() {
        val allDuas = com.example.data.repository.DuaData.duas

        allDuas.forEach { dua ->
            // Titles in 4 languages
            assertTrue("English title in ${dua.id} must not be blank", dua.titleEn.isNotBlank())
            assertTrue("Urdu title in ${dua.id} must not be blank", dua.titleUr.isNotBlank())
            assertTrue("Arabic title in ${dua.id} must not be blank", dua.titleAr.isNotBlank())
            assertTrue("Hindi title in ${dua.id} must not be blank", dua.titleHi.isNotBlank())

            // Translations in Urdu, English, Hindi
            assertTrue("English translation in ${dua.id} must not be blank", dua.translationEn.isNotBlank())
            assertTrue("Urdu translation in ${dua.id} must not be blank", dua.translationUr.isNotBlank())
            assertTrue("Hindi translation in ${dua.id} must not be blank", dua.translationHi.isNotBlank())
        }
    }

    @Test
    fun testDailyFeaturedDua() {
        val featured = com.example.data.repository.DuaData.getDailyFeaturedDua()
        assertNotNull("Daily featured dua must not be null", featured)
        assertTrue("Daily featured dua must have valid ID", featured.id.isNotBlank())
        assertTrue("Daily featured dua must have Arabic", featured.arabic.isNotBlank())
    }

    @Test
    fun testPrayerCalculationMethodsAndCoordinates() {
        // Test Faisalabad, Pakistan (31.4504, 73.1350) - Karachi Method, Hanafi Asr
        val faisalabadHanafi = PrayerCalculator.calculatePrayerTimes(
            latitude = 31.4504,
            longitude = 73.1350,
            method = PrayerCalculator.CalculationMethod.KARACHI,
            asrJuristic = PrayerCalculator.AsrJuristic.HANAFI,
            cityName = "Faisalabad, Pakistan"
        )
        assertNotNull(faisalabadHanafi.fajr)
        assertNotNull(faisalabadHanafi.sunrise)
        assertNotNull(faisalabadHanafi.dhuhr)
        assertNotNull(faisalabadHanafi.asr)
        assertNotNull(faisalabadHanafi.maghrib)
        assertNotNull(faisalabadHanafi.isha)

        // Standard Asr for comparison: Hanafi Asr must be distinct from Standard Asr
        val faisalabadStandard = PrayerCalculator.calculatePrayerTimes(
            latitude = 31.4504,
            longitude = 73.1350,
            method = PrayerCalculator.CalculationMethod.KARACHI,
            asrJuristic = PrayerCalculator.AsrJuristic.STANDARD,
            cityName = "Faisalabad, Pakistan"
        )
        assertNotNull(faisalabadStandard.asr)
        // In afternoon, shadow factor 2 (Hanafi) occurs later than shadow factor 1 (Standard)
        assertNotEquals("Hanafi and Standard Asr times should differ", faisalabadHanafi.asr, faisalabadStandard.asr)

        // Test Makkah, Saudi Arabia (21.4225, 39.8262) - Umm Al-Qura Method
        val makkahSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            method = PrayerCalculator.CalculationMethod.UMM_AL_QURA,
            asrJuristic = PrayerCalculator.AsrJuristic.STANDARD,
            cityName = "Makkah, Saudi Arabia"
        )
        assertNotNull(makkahSchedule.fajr)
        assertNotNull(makkahSchedule.maghrib)

        // Test London, UK (51.5074, -0.1278) - MWL Method
        val londonSchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 51.5074,
            longitude = -0.1278,
            method = PrayerCalculator.CalculationMethod.MWL,
            asrJuristic = PrayerCalculator.AsrJuristic.STANDARD,
            cityName = "London, UK"
        )
        assertNotNull(londonSchedule.fajr)
        assertNotNull(londonSchedule.isha)

        // Test New York, USA (40.7128, -74.0060) - ISNA Method
        val nySchedule = PrayerCalculator.calculatePrayerTimes(
            latitude = 40.7128,
            longitude = -74.0060,
            method = PrayerCalculator.CalculationMethod.ISNA,
            asrJuristic = PrayerCalculator.AsrJuristic.STANDARD,
            cityName = "New York, USA"
        )
        assertNotNull(nySchedule.fajr)
        assertNotNull(nySchedule.isha)

        // All 11 calculation methods must execute without exception
        PrayerCalculator.CalculationMethod.entries.forEach { method ->
            val result = PrayerCalculator.calculatePrayerTimes(
                latitude = 24.8607,
                longitude = 67.0011,
                method = method
            )
            assertNotNull(result.fajr)
            assertNotNull(result.dhuhr)
            assertNotNull(result.maghrib)
        }
    }

    @Test
    fun testPrayerTimeAdjustments() {
        val base = PrayerCalculator.calculatePrayerTimes(
            latitude = 24.8607,
            longitude = 67.0011,
            method = PrayerCalculator.CalculationMethod.KARACHI
        )
        val adjusted = PrayerCalculator.calculatePrayerTimes(
            latitude = 24.8607,
            longitude = 67.0011,
            method = PrayerCalculator.CalculationMethod.KARACHI,
            adjustments = PrayerCalculator.PrayerAdjustments(fajrMinutes = 5, maghribMinutes = -3)
        )
        assertNotEquals(base.fajr, adjusted.fajr)
        assertNotEquals(base.maghrib, adjusted.maghrib)
        assertEquals(base.dhuhr, adjusted.dhuhr)
    }

    @Test
    fun testMonthlyTimetableGeneration() {
        val days = PrayerCalculator.generateMonthlyTimetable(
            year = 2026,
            month = 4,
            latitude = 31.4504,
            longitude = 73.1350,
            timeZoneId = "Asia/Karachi",
            method = PrayerCalculator.CalculationMethod.KARACHI,
            asrJuristic = PrayerCalculator.AsrJuristic.HANAFI
        )
        assertTrue("Monthly timetable must have at least 28 days", days.size >= 28)
        assertTrue("Days must have valid prayer strings", days[0].fajr.isNotBlank())
        assertTrue("Days must have valid date strings", days[0].dateStr.isNotBlank())
    }

    @Test
    fun testQiblaBearingsWorldwide() {
        // Karachi, Pakistan (~267.7 degrees)
        val karachiQibla = PrayerCalculator.calculateQiblaBearing(24.8607, 67.0011)
        assertTrue("Karachi Qibla should be approx 267.7° (found $karachiQibla)", karachiQibla in 265.0..270.0)

        // Faisalabad, Pakistan (~260 degrees)
        val faisalabadQibla = PrayerCalculator.calculateQiblaBearing(31.4504, 73.1350)
        assertTrue("Faisalabad Qibla should be approx 260° (found $faisalabadQibla)", faisalabadQibla in 255.0..268.0)

        // London, UK (~118 degrees - East-Southeast)
        val londonQibla = PrayerCalculator.calculateQiblaBearing(51.5074, -0.1278)
        assertTrue("London Qibla should be approx 118° (found $londonQibla)", londonQibla in 110.0..125.0)

        // New York, USA (~58 degrees - East-Northeast)
        val nyQibla = PrayerCalculator.calculateQiblaBearing(40.7128, -74.0060)
        assertTrue("New York Qibla should be approx 58° (found $nyQibla)", nyQibla in 50.0..65.0)

        // Tokyo, Japan (~293 degrees - West-Northwest)
        val tokyoQibla = PrayerCalculator.calculateQiblaBearing(35.6762, 139.6503)
        assertTrue("Tokyo Qibla should be approx 293° (found $tokyoQibla)", tokyoQibla in 285.0..300.0)

        // Distance from Karachi to Makkah (approx 2700-2900 km)
        val distKm = PrayerCalculator.calculateDistanceToKaabaKm(24.8607, 67.0011)
        assertTrue("Distance to Kaaba from Karachi should be around 2800 km (found $distKm)", distKm in 2600.0..3000.0)
    }

    @Test
    fun testIslamicCalendarAndRuetHilalAdjustment() {
        val today = java.util.Calendar.getInstance()
        val standardHijri = com.example.data.repository.IslamicCalendarRepository.getHijriDate(
            calendar = today,
            manualOffsetDays = 0
        )
        assertNotNull(standardHijri.monthNameEn)
        assertTrue("Hijri year should be around 1445-1450", standardHijri.year in 1445..1450)
        assertTrue("Hijri month should be 1..12", standardHijri.month in 1..12)
        assertTrue("Hijri day should be 1..30", standardHijri.day in 1..30)

        // Formatted Hijri date string
        val formatted = com.example.data.repository.IslamicCalendarRepository.getFormattedHijriDate(today)
        assertTrue("Formatted date must contain AH", formatted.contains("AH"))

        // Test manual adjustment (+2 days)
        val plus2Hijri = com.example.data.repository.IslamicCalendarRepository.getHijriDate(
            calendar = today,
            manualOffsetDays = 2
        )
        assertNotNull(plus2Hijri.monthNameEn)

        // Verified Islamic events check
        val events = com.example.data.repository.IslamicCalendarRepository.ISLAMIC_EVENTS
        assertEquals("Must contain verified major Islamic events", 10, events.size)

        val ramadanEvent = events.find { it.id == "ramadan_start" }
        assertNotNull(ramadanEvent)
        assertTrue("Ramadan event must have moon sighting advisory badge", ramadanEvent!!.isMoonSightingDependent)

        val eidFitrEvent = events.find { it.id == "eid_al_fitr" }
        assertNotNull(eidFitrEvent)
        assertTrue(eidFitrEvent!!.isMoonSightingDependent)

        // Ramadan 30-day schedule generation check
        val ramadanDays = com.example.data.repository.IslamicCalendarRepository.generateRamadanSchedule(
            latitude = 31.4504,
            longitude = 73.1350,
            timeZoneId = "Asia/Karachi",
            gregorianStartCal = today
        )
        assertEquals("Ramadan schedule must have 30 days", 30, ramadanDays.size)
        assertTrue("Sehri time must be valid", ramadanDays[0].sehriTime.contains("AM") || ramadanDays[0].sehriTime.contains("PM"))
        assertTrue("Iftar time must be valid", ramadanDays[0].iftarTime.contains("PM"))
    }

    @Test
    fun testLocationPresetsIntegrity() {
        val pakCities = com.example.data.location.AppLocationManager.PRESET_CITIES.filter { it.country == "Pakistan" }
        assertTrue("Must have at least 10 major Pakistani cities", pakCities.size >= 10)

        val requiredPak = listOf("Karachi", "Lahore", "Faisalabad", "Rawalpindi", "Islamabad", "Multan", "Peshawar", "Quetta")
        requiredPak.forEach { name ->
            assertTrue("Must contain city $name", pakCities.any { it.nameEn.contains(name) })
        }

        val globalCities = com.example.data.location.AppLocationManager.PRESET_CITIES.filter { it.country != "Pakistan" }
        assertTrue("Must contain Makkah and Madinah", globalCities.any { it.nameEn.contains("Makkah") })
        assertTrue("Must contain Madinah", globalCities.any { it.nameEn.contains("Madinah") })
    }

    @Test
    fun testWorldwideMethodAutoRecommendation() {
        // North America -> ISNA
        assertEquals(
            PrayerCalculator.CalculationMethod.ISNA,
            PrayerCalculator.getRecommendedMethod("United States")
        )
        assertEquals(
            PrayerCalculator.CalculationMethod.ISNA,
            PrayerCalculator.getRecommendedMethod("Canada")
        )

        // Europe / UK -> MWL
        assertEquals(
            PrayerCalculator.CalculationMethod.MWL,
            PrayerCalculator.getRecommendedMethod("United Kingdom")
        )
        assertEquals(
            PrayerCalculator.CalculationMethod.MWL,
            PrayerCalculator.getRecommendedMethod("Germany")
        )

        // Saudi Arabia / Gulf -> Umm Al-Qura / Dubai / Qatar
        assertEquals(
            PrayerCalculator.CalculationMethod.UMM_AL_QURA,
            PrayerCalculator.getRecommendedMethod("Saudi Arabia")
        )
        assertEquals(
            PrayerCalculator.CalculationMethod.DUBAI,
            PrayerCalculator.getRecommendedMethod("United Arab Emirates")
        )
        assertEquals(
            PrayerCalculator.CalculationMethod.QATAR,
            PrayerCalculator.getRecommendedMethod("Qatar")
        )

        // Egypt & North Africa -> Egyptian General Authority
        assertEquals(
            PrayerCalculator.CalculationMethod.EGYPTIAN,
            PrayerCalculator.getRecommendedMethod("Egypt")
        )

        // Turkey -> Diyanet
        assertEquals(
            PrayerCalculator.CalculationMethod.TURKEY,
            PrayerCalculator.getRecommendedMethod("Turkey")
        )

        // Singapore & SE Asia -> MUIS
        assertEquals(
            PrayerCalculator.CalculationMethod.SINGAPORE,
            PrayerCalculator.getRecommendedMethod("Singapore")
        )

        // South Asia -> Karachi & Hanafi
        assertEquals(
            PrayerCalculator.CalculationMethod.KARACHI,
            PrayerCalculator.getRecommendedMethod("Pakistan")
        )
        assertEquals(
            PrayerCalculator.AsrJuristic.HANAFI,
            PrayerCalculator.getRecommendedAsr("Pakistan")
        )
        assertEquals(
            PrayerCalculator.AsrJuristic.STANDARD,
            PrayerCalculator.getRecommendedAsr("Saudi Arabia")
        )
    }
}

