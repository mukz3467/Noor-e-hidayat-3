package com.example.data.repository

import com.example.data.model.PrayerSchedule
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

object PrayerCalculator {

    const val KAABA_LAT = 21.4225
    const val KAABA_LON = 39.8262

    data class PrayerAdjustments(
        val fajrMinutes: Int = 0,
        val sunriseMinutes: Int = 0,
        val dhuhrMinutes: Int = 0,
        val asrMinutes: Int = 0,
        val maghribMinutes: Int = 0,
        val ishaMinutes: Int = 0
    ) {
        val hasAdjustments: Boolean
            get() = fajrMinutes != 0 || sunriseMinutes != 0 || dhuhrMinutes != 0 ||
                    asrMinutes != 0 || maghribMinutes != 0 || ishaMinutes != 0
    }

    enum class CalculationMethod(
        val id: String,
        val displayName: String,
        val fajrAngle: Double,
        val ishaAngle: Double,
        val isIshaInterval: Boolean = false,
        val ishaIntervalMinutes: Int = 0,
        val defaultCountry: String = ""
    ) {
        KARACHI(
            id = "karachi",
            displayName = "University of Islamic Sciences, Karachi (Pakistan)",
            fajrAngle = 18.0,
            ishaAngle = 18.0,
            defaultCountry = "Pakistan"
        ),
        MWL(
            id = "mwl",
            displayName = "Muslim World League (MWL)",
            fajrAngle = 18.0,
            ishaAngle = 17.0,
            defaultCountry = "Europe"
        ),
        ISNA(
            id = "isna",
            displayName = "Islamic Society of North America (ISNA)",
            fajrAngle = 15.0,
            ishaAngle = 15.0,
            defaultCountry = "United States"
        ),
        EGYPTIAN(
            id = "egyptian",
            displayName = "Egyptian General Authority of Survey",
            fajrAngle = 19.5,
            ishaAngle = 17.5,
            defaultCountry = "Egypt"
        ),
        UMM_AL_QURA(
            id = "makkah",
            displayName = "Umm Al-Qura University, Makkah",
            fajrAngle = 18.5,
            ishaAngle = 0.0,
            isIshaInterval = true,
            ishaIntervalMinutes = 90,
            defaultCountry = "Saudi Arabia"
        ),
        DUBAI(
            id = "dubai",
            displayName = "Dubai / UAE General Authority of Islamic Affairs",
            fajrAngle = 18.2,
            ishaAngle = 18.2,
            defaultCountry = "UAE"
        ),
        QATAR(
            id = "qatar",
            displayName = "Qatar Ministry of Awqaf & Islamic Affairs",
            fajrAngle = 18.0,
            ishaAngle = 0.0,
            isIshaInterval = true,
            ishaIntervalMinutes = 90,
            defaultCountry = "Qatar"
        ),
        KUWAIT(
            id = "kuwait",
            displayName = "Kuwait Ministry of Awqaf",
            fajrAngle = 18.0,
            ishaAngle = 17.5,
            defaultCountry = "Kuwait"
        ),
        SINGAPORE(
            id = "singapore",
            displayName = "MUIS (Islamic Religious Council of Singapore)",
            fajrAngle = 20.0,
            ishaAngle = 18.0,
            defaultCountry = "Singapore"
        ),
        TURKEY(
            id = "turkey",
            displayName = "Diyanet İşleri Başkanlığı (Turkey)",
            fajrAngle = 18.0,
            ishaAngle = 17.0,
            defaultCountry = "Turkey"
        ),
        TEHRAN(
            id = "tehran",
            displayName = "Institute of Geophysics, University of Tehran",
            fajrAngle = 17.7,
            ishaAngle = 14.0,
            defaultCountry = "Iran"
        )
    }

    enum class AsrJuristic(val displayName: String, val shadowFactor: Double) {
        HANAFI("Hanafi (Shadow factor = 2)", 2.0),
        STANDARD("Standard / Shafi'i, Maliki, Hanbali (Shadow factor = 1)", 1.0)
    }

    enum class HighLatitudeRule(val displayName: String) {
        ANGLE_BASED("Angle-based Rule (1/60th of night per degree)"),
        MIDNIGHT("Midnight Rule (middle of the night)"),
        ONE_SEVENTH("One-Seventh Rule (1/7th of the night)")
    }

    /**
     * Recommends the officially and canonical calculation method for any country worldwide.
     */
    fun getRecommendedMethod(country: String, lat: Double = 0.0, lon: Double = 0.0): CalculationMethod {
        val c = country.trim().lowercase(Locale.ROOT)
        return when {
            c.contains("pakistan") || c.contains("india") || c.contains("bangladesh") || c.contains("afghanistan") -> CalculationMethod.KARACHI
            c.contains("saudi") || c.contains("yemen") || c.contains("oman") -> CalculationMethod.UMM_AL_QURA
            c.contains("emirates") || c.contains("uae") || c.contains("dubai") -> CalculationMethod.DUBAI
            c.contains("qatar") -> CalculationMethod.QATAR
            c.contains("kuwait") -> CalculationMethod.KUWAIT
            c.contains("egypt") || c.contains("libya") || c.contains("sudan") || c.contains("morocco") ||
                    c.contains("algeria") || c.contains("tunisia") || c.contains("syria") || c.contains("jordan") ||
                    c.contains("lebanon") || c.contains("palestine") -> CalculationMethod.EGYPTIAN
            c.contains("turkey") || c.contains("türkiye") -> CalculationMethod.TURKEY
            c.contains("united states") || c.contains("usa") || c.contains("canada") || c.contains("america") -> CalculationMethod.ISNA
            c.contains("singapore") || c.contains("malaysia") || c.contains("indonesia") || c.contains("brunei") -> CalculationMethod.SINGAPORE
            c.contains("iran") -> CalculationMethod.TEHRAN
            else -> CalculationMethod.MWL // Muslim World League as worldwide global standard for UK, Europe, Australia, etc.
        }
    }

    /**
     * Recommends Asr juristic calculation method based on country's predominant tradition.
     */
    fun getRecommendedAsr(country: String): AsrJuristic {
        val c = country.trim().lowercase(Locale.ROOT)
        return if (c.contains("pakistan") || c.contains("india") || c.contains("bangladesh") || c.contains("afghanistan") || c.contains("turkey") || c.contains("türkiye")) {
            AsrJuristic.HANAFI
        } else {
            AsrJuristic.STANDARD
        }
    }

    // Great circle calculation to Kaaba
    fun calculateQiblaBearing(userLat: Double, userLon: Double): Double {
        val phi1 = Math.toRadians(userLat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LON - userLon)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360.0) % 360.0
    }

    fun calculateDistanceToKaabaKm(userLat: Double, userLon: Double): Double {
        val r = 6371.0 // Earth's mean radius in km
        val dLat = Math.toRadians(KAABA_LAT - userLat)
        val dLon = Math.toRadians(KAABA_LON - userLon)
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(KAABA_LAT)
        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun calculatePrayerTimes(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance(),
        method: CalculationMethod = CalculationMethod.KARACHI,
        asrJuristic: AsrJuristic = AsrJuristic.HANAFI,
        adjustments: PrayerAdjustments = PrayerAdjustments(),
        highLatitudeRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
        cityName: String = "Karachi, Pakistan",
        timeZoneId: String? = null
    ): PrayerSchedule {
        val tz = if (!timeZoneId.isNullOrBlank()) TimeZone.getTimeZone(timeZoneId) else calendar.timeZone
        val localCal = (calendar.clone() as Calendar).apply { timeZone = tz }

        val year = localCal.get(Calendar.YEAR)
        val month = localCal.get(Calendar.MONTH) + 1
        val day = localCal.get(Calendar.DAY_OF_MONTH)
        val timeZoneOffsetHours = tz.getOffset(localCal.timeInMillis) / 3600000.0

        // Astronomical calculations
        val jd = computeJulianDay(year, month, day)
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val declination = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqTime = q / 15.0 - fixHour(ra)

        // Solar Noon (Dhuhr)
        val solarNoon = fixHour(12.0 + timeZoneOffsetHours - longitude / 15.0 - eqTime)

        // Sunrise & Sunset (accounting for atmospheric refraction 0.833 degrees)
        val sunAngle = 0.833
        val sunHourAngle = computeHourAngle(latitude, declination, sunAngle)
        val rawSunrise = solarNoon - sunHourAngle
        val rawSunset = solarNoon + sunHourAngle

        // Night length for high latitude adjustments
        val nightDurationHours = fixHour(rawSunrise - rawSunset + 24.0)

        // Fajr
        var fajrHourAngle = computeHourAngle(latitude, declination, method.fajrAngle)
        var rawFajr = solarNoon - fajrHourAngle
        if (rawFajr.isNaN() || fajrHourAngle.isNaN()) {
            // Apply high-latitude rule when sun does not reach twilight angle
            rawFajr = applyHighLatitudeFajr(rawSunrise, nightDurationHours, method.fajrAngle, highLatitudeRule)
        }

        // Asr
        val asrFactor = asrJuristic.shadowFactor
        val asrAngle = -Math.toDegrees(atan(1.0 / (asrFactor + tan(Math.toRadians(abs(latitude - declination))))))
        val asrHourAngle = computeHourAngle(latitude, declination, -asrAngle)
        val rawAsr = solarNoon + asrHourAngle

        // Maghrib
        val rawMaghrib = rawSunset + (2.0 / 60.0) // 2 minutes precautionary buffer

        // Isha
        var rawIsha: Double
        if (method.isIshaInterval) {
            val interval = method.ishaIntervalMinutes / 60.0
            rawIsha = rawMaghrib + interval
        } else {
            val ishaHourAngle = computeHourAngle(latitude, declination, method.ishaAngle)
            rawIsha = solarNoon + ishaHourAngle
            if (rawIsha.isNaN() || ishaHourAngle.isNaN()) {
                rawIsha = applyHighLatitudeIsha(rawSunset, nightDurationHours, method.ishaAngle, highLatitudeRule)
            }
        }

        // Apply minute adjustments
        val adjustedFajr = rawFajr + (adjustments.fajrMinutes / 60.0)
        val adjustedSunrise = rawSunrise + (adjustments.sunriseMinutes / 60.0)
        val adjustedDhuhr = solarNoon + (adjustments.dhuhrMinutes / 60.0)
        val adjustedAsr = rawAsr + (adjustments.asrMinutes / 60.0)
        val adjustedMaghrib = rawMaghrib + (adjustments.maghribMinutes / 60.0)
        val adjustedIsha = rawIsha + (adjustments.ishaMinutes / 60.0)

        val dateFormatted = String.format(Locale.US, "%02d/%02d/%d", day, month, year)
        val hijriDateStr = IslamicCalendarRepository.getFormattedHijriDate(localCal)

        return PrayerSchedule(
            dateStr = dateFormatted,
            hijriDate = hijriDateStr,
            fajr = formatPrayerTime("fajr", adjustedFajr),
            sunrise = formatPrayerTime("sunrise", adjustedSunrise),
            dhuhr = formatPrayerTime("dhuhr", adjustedDhuhr),
            asr = formatPrayerTime("asr", adjustedAsr),
            maghrib = formatPrayerTime("maghrib", adjustedMaghrib),
            isha = formatPrayerTime("isha", adjustedIsha),
            calculationMethod = method.displayName,
            cityName = cityName
        )
    }

    private fun computeJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun computeHourAngle(lat: Double, decl: Double, angle: Double): Double {
        val cosHA = (-sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))) /
                (cos(Math.toRadians(lat)) * cos(Math.toRadians(decl)))
        if (cosHA < -1.0 || cosHA > 1.0) {
            return Double.NaN
        }
        return Math.toDegrees(acos(cosHA)) / 15.0
    }

    private fun applyHighLatitudeFajr(
        sunrise: Double,
        nightHours: Double,
        fajrAngle: Double,
        rule: HighLatitudeRule
    ): Double {
        val portion = when (rule) {
            HighLatitudeRule.ANGLE_BASED -> (fajrAngle / 60.0) * nightHours
            HighLatitudeRule.MIDNIGHT -> 0.5 * nightHours
            HighLatitudeRule.ONE_SEVENTH -> (1.0 / 7.0) * nightHours
        }
        return sunrise - portion
    }

    private fun applyHighLatitudeIsha(
        sunset: Double,
        nightHours: Double,
        ishaAngle: Double,
        rule: HighLatitudeRule
    ): Double {
        val portion = when (rule) {
            HighLatitudeRule.ANGLE_BASED -> (ishaAngle / 60.0) * nightHours
            HighLatitudeRule.MIDNIGHT -> 0.5 * nightHours
            HighLatitudeRule.ONE_SEVENTH -> (1.0 / 7.0) * nightHours
        }
        return sunset + portion
    }

    fun fixHour(h: Double): Double = (h % 24.0 + 24.0) % 24.0
    fun fixAngle(a: Double): Double = (a % 360.0 + 360.0) % 360.0

    /**
     * Canonical formatter for Islamic prayer times.
     * Guarantees Fajr/Sunrise are strictly AM and Asr/Maghrib/Isha are strictly PM.
     */
    fun formatPrayerTime(prayerKey: String, timeHours: Double, is24Hour: Boolean = false): String {
        if (timeHours.isNaN()) return "--:--"
        var fixed = fixHour(timeHours)

        // Strict Canonical Islamic Time Constraints:
        // 1. Fajr and Sunrise MUST be AM (morning between 00:00 and 11:59)
        if (prayerKey == "fajr" || prayerKey == "sunrise") {
            if (fixed >= 12.0) {
                fixed -= 12.0
            }
        }
        // 2. Asr, Maghrib, Isha MUST be PM (afternoon/evening/night between 12:00 and 23:59)
        if (prayerKey == "asr" || prayerKey == "maghrib" || prayerKey == "isha") {
            if (fixed < 12.0) {
                fixed += 12.0
            }
        }

        val hours = fixed.toInt()
        val totalMinutes = ((fixed - hours) * 60).roundToInt()
        val finalHours = (hours + totalMinutes / 60) % 24
        val finalMinutes = totalMinutes % 60

        return if (is24Hour) {
            String.format(Locale.US, "%02d:%02d", finalHours, finalMinutes)
        } else {
            val h12 = if (finalHours == 0) 12 else if (finalHours > 12) finalHours - 12 else finalHours
            val amPm = when (prayerKey) {
                "fajr", "sunrise" -> "AM"
                "asr", "maghrib", "isha" -> "PM"
                "dhuhr" -> if (finalHours < 12) "AM" else "PM"
                else -> if (finalHours < 12) "AM" else "PM"
            }
            String.format(Locale.US, "%02d:%02d %s", h12, finalMinutes, amPm)
        }
    }

    fun formatTime(timeHours: Double, is24Hour: Boolean = false): String {
        if (timeHours.isNaN()) return "--:--"
        val fixed = fixHour(timeHours)
        val hours = fixed.toInt()
        val totalMinutes = ((fixed - hours) * 60).roundToInt()
        val finalHours = (hours + totalMinutes / 60) % 24
        val finalMinutes = totalMinutes % 60

        return if (is24Hour) {
            String.format(Locale.US, "%02d:%02d", finalHours, finalMinutes)
        } else {
            val h12 = if (finalHours == 0) 12 else if (finalHours > 12) finalHours - 12 else finalHours
            val amPm = if (finalHours < 12) "AM" else "PM"
            String.format(Locale.US, "%02d:%02d %s", h12, finalMinutes, amPm)
        }
    }

    data class LiveNextPrayerInfo(
        val prayerKey: String, // "fajr", "dhuhr", "asr", "maghrib", "isha", "sunrise"
        val prayerNameEnglish: String,
        val prayerNameUrdu: String,
        val prayerTimeFormatted: String,
        val remainingMillis: Long,
        val remainingHours: Int,
        val remainingMinutes: Int,
        val remainingSeconds: Int,
        val progressPercent: Float
    )

    fun calculateLiveNextPrayer(
        schedule: PrayerSchedule,
        timeZoneId: String? = null,
        nowCalendar: Calendar = Calendar.getInstance()
    ): LiveNextPrayerInfo {
        val tz = if (!timeZoneId.isNullOrBlank()) TimeZone.getTimeZone(timeZoneId) else nowCalendar.timeZone
        val localCal = (nowCalendar.clone() as Calendar).apply { timeZone = tz }
        val nowMillis = localCal.timeInMillis

        // Parse each prayer time for today
        val prayers = listOf(
            Triple("fajr", "Fajr", parsePrayerTimeToMillis(schedule.fajr, localCal)),
            Triple("sunrise", "Sunrise", parsePrayerTimeToMillis(schedule.sunrise, localCal)),
            Triple("dhuhr", "Dhuhr", parsePrayerTimeToMillis(schedule.dhuhr, localCal)),
            Triple("asr", "Asr", parsePrayerTimeToMillis(schedule.asr, localCal)),
            Triple("maghrib", "Maghrib", parsePrayerTimeToMillis(schedule.maghrib, localCal)),
            Triple("isha", "Isha", parsePrayerTimeToMillis(schedule.isha, localCal))
        )

        // Find the next upcoming prayer today
        for (i in prayers.indices) {
            val (key, name, timeMillis) = prayers[i]
            if (timeMillis > nowMillis) {
                val remaining = timeMillis - nowMillis
                val totalSeconds = (remaining / 1000).toInt()
                val hrs = totalSeconds / 3600
                val mins = (totalSeconds % 3600) / 60
                val secs = totalSeconds % 60

                val prevMillis = if (i > 0) prayers[i - 1].third else timeMillis - (4 * 3600 * 1000)
                val totalWindow = (timeMillis - prevMillis).toFloat().coerceAtLeast(1f)
                val elapsed = (nowMillis - prevMillis).toFloat().coerceAtLeast(0f)
                val progress = (elapsed / totalWindow).coerceIn(0f, 1f)

                val urduName = when (key) {
                    "fajr" -> "فجر"
                    "sunrise" -> "طلوعِ آفتاب"
                    "dhuhr" -> "ظہر"
                    "asr" -> "عصر"
                    "maghrib" -> "مغرب"
                    else -> "عشاء"
                }

                val timeStr = when (key) {
                    "fajr" -> schedule.fajr
                    "sunrise" -> schedule.sunrise
                    "dhuhr" -> schedule.dhuhr
                    "asr" -> schedule.asr
                    "maghrib" -> schedule.maghrib
                    else -> schedule.isha
                }

                return LiveNextPrayerInfo(
                    prayerKey = key,
                    prayerNameEnglish = name,
                    prayerNameUrdu = urduName,
                    prayerTimeFormatted = timeStr,
                    remainingMillis = remaining,
                    remainingHours = hrs,
                    remainingMinutes = mins,
                    remainingSeconds = secs,
                    progressPercent = progress
                )
            }
        }

        // If all prayers today have passed, next prayer is Tomorrow's Fajr
        val tomorrowFajrMillis = prayers[0].third + 24 * 3600 * 1000
        val remaining = (tomorrowFajrMillis - nowMillis).coerceAtLeast(0L)
        val totalSeconds = (remaining / 1000).toInt()
        val hrs = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60

        return LiveNextPrayerInfo(
            prayerKey = "fajr",
            prayerNameEnglish = "Fajr (Tomorrow)",
            prayerNameUrdu = "فجر (کل)",
            prayerTimeFormatted = schedule.fajr,
            remainingMillis = remaining,
            remainingHours = hrs,
            remainingMinutes = mins,
            remainingSeconds = secs,
            progressPercent = 0.95f
        )
    }

    private fun parsePrayerTimeToMillis(formattedTime: String, baseCal: Calendar): Long {
        return try {
            val trimmed = formattedTime.trim()
            val isPm = trimmed.endsWith("PM", ignoreCase = true)
            val isAm = trimmed.endsWith("AM", ignoreCase = true)
            val clean = trimmed.replace("AM", "").replace("PM", "").trim()
            val parts = clean.split(":")
            var hour = parts[0].toInt()
            val minute = parts[1].toInt()

            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0

            val cal = (baseCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } catch (_: Exception) {
            baseCal.timeInMillis
        }
    }

    fun generateMonthlyTimetable(
        year: Int,
        month: Int, // 1-12
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        method: CalculationMethod = CalculationMethod.KARACHI,
        asrJuristic: AsrJuristic = AsrJuristic.HANAFI,
        adjustments: PrayerAdjustments = PrayerAdjustments()
    ): List<PrayerSchedule> {
        val list = mutableListOf<PrayerSchedule>()
        val cal = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val schedule = calculatePrayerTimes(
                latitude = latitude,
                longitude = longitude,
                calendar = cal,
                method = method,
                asrJuristic = asrJuristic,
                adjustments = adjustments,
                timeZoneId = timeZoneId
            )
            list.add(schedule)
        }
        return list
    }
}
