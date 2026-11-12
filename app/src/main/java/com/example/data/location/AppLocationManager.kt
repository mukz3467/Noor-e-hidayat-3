package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val countryName: String,
    val timeZoneId: String,
    val accuracyMeters: Float? = null,
    val isGpsFix: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CityPreset(
    val nameEn: String,
    val nameUr: String,
    val nameAr: String,
    val nameHi: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

object AppLocationManager {

    private const val PREFS_NAME = "location_prefs"
    private const val KEY_LAT = "cached_latitude"
    private const val KEY_LON = "cached_longitude"
    private const val KEY_CITY = "cached_city"
    private const val KEY_COUNTRY = "cached_country"
    private const val KEY_TZ = "cached_timezone"
    private const val KEY_ACCURACY = "cached_accuracy"
    private const val KEY_IS_GPS = "cached_is_gps"
    private const val KEY_TIMESTAMP = "cached_timestamp"

    // Default: Karachi, Pakistan (Islamic hub)
    val DEFAULT_LOCATION = UserLocation(
        latitude = 24.8607,
        longitude = 67.0011,
        cityName = "Karachi",
        countryName = "Pakistan",
        timeZoneId = "Asia/Karachi",
        accuracyMeters = null,
        isGpsFix = false
    )

    val PRESET_CITIES: List<CityPreset> = listOf(
        // Pakistan Major Cities
        CityPreset("Karachi", "کراچی", "كراتشي", "कराची", "Pakistan", 24.8607, 67.0011, "Asia/Karachi"),
        CityPreset("Lahore", "لاہور", "لاهور", "लाहौर", "Pakistan", 31.5204, 74.3587, "Asia/Karachi"),
        CityPreset("Faisalabad", "فیصل آباد", "فيصل آباد", "फैसलाबाद", "Pakistan", 31.4504, 73.1350, "Asia/Karachi"),
        CityPreset("Rawalpindi", "راولپنڈی", "راولبندي", "रावलपिंडी", "Pakistan", 33.5651, 73.0169, "Asia/Karachi"),
        CityPreset("Islamabad", "اسلام آباد", "إسلام آباد", "इस्लामाबाद", "Pakistan", 33.6844, 73.0479, "Asia/Karachi"),
        CityPreset("Multan", "ملتان", "ملتان", "मुल्तान", "Pakistan", 30.1575, 71.5249, "Asia/Karachi"),
        CityPreset("Peshawar", "پشاور", "بيشاور", "पेशावर", "Pakistan", 34.0151, 71.5249, "Asia/Karachi"),
        CityPreset("Quetta", "کوئٹہ", "كويته", "क्वेटा", "Pakistan", 30.1798, 66.9750, "Asia/Karachi"),
        CityPreset("Gujranwala", "گوجرانوالہ", "غوجرانوالا", "गुजरांवाला", "Pakistan", 32.1877, 74.1945, "Asia/Karachi"),
        CityPreset("Sialkot", "سیالکوٹ", "سيالكوت", "सियालकोट", "Pakistan", 32.4945, 74.5229, "Asia/Karachi"),
        CityPreset("Hyderabad", "حیدرآباد", "حيدر آباد", "हैदराबाद", "Pakistan", 25.3960, 68.3578, "Asia/Karachi"),
        CityPreset("Bahawalpur", "بہاولپور", "بهاولبور", "बहावलपुर", "Pakistan", 29.3544, 71.6911, "Asia/Karachi"),
        CityPreset("Sargodha", "سرگودھا", "سرغودها", "सरगोधा", "Pakistan", 32.0836, 72.6711, "Asia/Karachi"),
        CityPreset("Abbottabad", "ایبٹ آباد", "أبوت آباد", "एबटाबाद", "Pakistan", 34.1688, 73.2215, "Asia/Karachi"),
        CityPreset("Sukkur", "سکھر", "سکھر", "सक्खर", "Pakistan", 27.7052, 68.8574, "Asia/Karachi"),
        CityPreset("Muzaffarabad", "مظفر آباد", "مظفر آباد", "मुज़फ़्फ़राबाद", "Pakistan", 34.3700, 73.4711, "Asia/Karachi"),
        CityPreset("Gilgit", "گلگت", "غلغت", "गिलगित", "Pakistan", 35.9208, 74.3144, "Asia/Karachi"),
        CityPreset("Mirpur", "میرپور", "ميربور", "मीरपुर", "Pakistan", 33.1484, 73.7519, "Asia/Karachi"),

        // Holy Cities & Middle East
        CityPreset("Makkah", "مکہ مکرمہ", "مكة المكرمة", "मक्का", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh"),
        CityPreset("Madinah", "مدینہ منورہ", "المدينة المنورة", "मदीना", "Saudi Arabia", 24.5247, 39.5692, "Asia/Riyadh"),
        CityPreset("Riyadh", "ریاض", "الرياض", "रियाद", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh"),
        CityPreset("Dubai", "دبئی", "دبي", "दुबई", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
        CityPreset("Abu Dhabi", "ابوظہبی", "أبو ظبي", "अबू धाबी", "UAE", 24.4539, 54.3773, "Asia/Dubai"),
        CityPreset("Doha", "دوحہ", "الدوحة", "दोहा", "Qatar", 25.2854, 51.5310, "Asia/Qatar"),
        CityPreset("Kuwait City", "کویت سٹی", "مدينة الكويت", "कुवैत सिटी", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait"),
        CityPreset("Cairo", "قاہرہ", "القاهرة", "काहिरा", "Egypt", 30.0444, 31.2357, "Africa/Cairo"),
        CityPreset("Istanbul", "استنبول", "إسطنبول", "इस्तांबुल", "Turkey", 41.0082, 28.9784, "Europe/Istanbul"),
        CityPreset("Amman", "عمان", "عمان", "अम्मान", "Jordan", 31.9454, 35.9284, "Asia/Amman"),
        CityPreset("Baghdad", "بغداد", "بغداد", "बगदाद", "Iraq", 33.3152, 44.3661, "Asia/Baghdad"),

        // Global Cities - Middle East & Holy Sites
        CityPreset("Jerusalem", "القدس الشریف", "القدس الشريف", "यरूशलम", "Palestine", 31.7683, 35.2137, "Asia/Jerusalem"),
        CityPreset("Jeddah", "جدہ", "جدة", "जेद्दा", "Saudi Arabia", 21.4858, 39.1925, "Asia/Riyadh"),
        CityPreset("Dammam", "دمام", "الدمام", "दम्माम", "Saudi Arabia", 26.4207, 50.0888, "Asia/Riyadh"),
        CityPreset("Manama", "منامہ", "المنامة", "मनामा", "Bahrain", 26.2285, 50.5860, "Asia/Bahrain"),
        CityPreset("Muscat", "مسقط", "مسقط", "मस्कट", "Oman", 23.5880, 58.3829, "Asia/Muscat"),
        CityPreset("Beirut", "بیروت", "بيروت", "बेयरूत", "Lebanon", 33.8938, 35.5018, "Asia/Beirut"),

        // Global Cities - Europe
        CityPreset("London", "لندن", "لندن", "लंदन", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
        CityPreset("Birmingham", "برمنگھم", "برمنغهام", "बर्मिंघम", "United Kingdom", 52.4862, -1.8904, "Europe/London"),
        CityPreset("Manchester", "مانچسٹر", "مانشستر", "मैनचेस्टर", "United Kingdom", 53.4808, -2.2426, "Europe/London"),
        CityPreset("Paris", "پیرس", "باريس", "पेरिस", "France", 48.8566, 2.3522, "Europe/Paris"),
        CityPreset("Berlin", "برلن", "برلين", "बर्लिन", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
        CityPreset("Frankfurt", "فرینکفرٹ", "فرانكفورت", "फ्रैंकफर्ट", "Germany", 50.1109, 8.6821, "Europe/Berlin"),
        CityPreset("Amsterdam", "ایمسٹرڈیم", "أمستردام", "एम्स्टर्डम", "Netherlands", 52.3676, 4.9041, "Europe/Amsterdam"),
        CityPreset("Brussels", "برسلز", "بروكسل", "ब्रुसेल्स", "Belgium", 50.8503, 4.3517, "Europe/Brussels"),
        CityPreset("Rome", "روم", "روما", "रोम", "Italy", 41.9028, 12.4964, "Europe/Rome"),
        CityPreset("Madrid", "میڈرڈ", "مدريد", "मैड्रिड", "Spain", 40.4168, -3.7038, "Europe/Madrid"),
        CityPreset("Vienna", "ویانا", "فيينا", "वियना", "Austria", 48.2082, 16.3738, "Europe/Vienna"),
        CityPreset("Stockholm", "اسٹاک ہوم", "ستوكهولم", "स्टॉकहोम", "Sweden", 59.3293, 18.0686, "Europe/Stockholm"),
        CityPreset("Dublin", "ڈبلن", "دبلن", "डबलिन", "Ireland", 53.3498, -6.2603, "Europe/Dublin"),

        // Global Cities - North America
        CityPreset("New York", "نیویارک", "نيويورك", "न्यूयॉर्क", "United States", 40.7128, -74.0060, "America/New_York"),
        CityPreset("Chicago", "شکاگو", "شيكاغو", "शिकागो", "United States", 41.8781, -87.6298, "America/Chicago"),
        CityPreset("Los Angeles", "لاس اینجلس", "لوس أنجلوس", "लॉस एंजिल्स", "United States", 34.0522, -118.2437, "America/Los_Angeles"),
        CityPreset("Houston", "ہیوسٹن", "هيوستن", "ह्यूस्टन", "United States", 29.7604, -95.3698, "America/Chicago"),
        CityPreset("Dallas", "ڈیلاس", "دالاس", "डलास", "United States", 32.7767, -96.7970, "America/Chicago"),
        CityPreset("Washington DC", "واشنگٹن ڈی سی", "واشنطن العاصمة", "वाशिंगटन डीसी", "United States", 38.9072, -77.0369, "America/New_York"),
        CityPreset("Toronto", "ٹورنٹو", "تورونتو", "टोरंटो", "Canada", 43.6532, -79.3832, "America/Toronto"),
        CityPreset("Montreal", "مانٹریال", "مونتريال", "मॉन्ट्रियल", "Canada", 45.5017, -73.5673, "America/Toronto"),
        CityPreset("Vancouver", "وینکوور", "فانكوفر", "वैंकूवर", "Canada", 49.2827, -123.1207, "America/Vancouver"),

        // Global Cities - South Asia & Central Asia
        CityPreset("Delhi", "نئی دہلی", "دلهي", "नई दिल्ली", "India", 28.6139, 77.2090, "Asia/Kolkata"),
        CityPreset("Mumbai", "ممبئی", "مومباي", "मुंबई", "India", 19.0760, 72.8777, "Asia/Kolkata"),
        CityPreset("Kolkata", "کولکتہ", "كولكاتا", "कोलकाता", "India", 22.5726, 88.3639, "Asia/Kolkata"),
        CityPreset("Bengaluru", "بنگلور", "بنغالور", "बेंगलुरु", "India", 12.9716, 77.5946, "Asia/Kolkata"),
        CityPreset("Hyderabad (Deccan)", "حیدرآباد دکن", "حيدر آباد الدكن", "हैदराबाद दक्कन", "India", 17.3850, 78.4867, "Asia/Kolkata"),
        CityPreset("Dhaka", "ڈھاکہ", "دكا", "ढाका", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka"),
        CityPreset("Chittagong", "چٹاگانگ", "شيتاغونغ", "चटगांव", "Bangladesh", 22.3569, 91.7832, "Asia/Dhaka"),
        CityPreset("Kabul", "کابل", "كابول", "काबुल", "Afghanistan", 34.5553, 69.2075, "Asia/Kabul"),
        CityPreset("Tashkent", "تاشقند", "طشقند", "ताशकंद", "Uzbekistan", 41.2995, 69.2401, "Asia/Tashkent"),
        CityPreset("Samarkand", "سمرقند", "سمرقند", "समरकंद", "Uzbekistan", 39.6270, 66.9750, "Asia/Samarkand"),
        CityPreset("Baku", "باکو", "باكو", "बाकू", "Azerbaijan", 40.4093, 49.8671, "Asia/Baku"),
        CityPreset("Almaty", "الماتی", "ألماتي", "अल्माटी", "Kazakhstan", 43.2220, 76.8512, "Asia/Almaty"),

        // Global Cities - Southeast & East Asia
        CityPreset("Kuala Lumpur", "کوالالمپور", "كوالالمبور", "कुआलालंपुर", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur"),
        CityPreset("Jakarta", "جکارتہ", "جاكرتا", "जकार्ता", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta"),
        CityPreset("Surabaya", "سورابایا", "سورابايا", "सुरबाया", "Indonesia", -7.2575, 112.7521, "Asia/Jakarta"),
        CityPreset("Singapore", "سنگاپور", "سنغافورة", "सिंगापुर", "Singapore", 1.3521, 103.8198, "Asia/Singapore"),
        CityPreset("Bangkok", "بینکاک", "بانكوك", "बैंकॉक", "Thailand", 13.7563, 100.5018, "Asia/Bangkok"),
        CityPreset("Tokyo", "ٹوکیو", "طوكيو", "टोक्यो", "Japan", 35.6762, 139.6503, "Asia/Tokyo"),
        CityPreset("Seoul", "سیول", "سيول", "सियोल", "South Korea", 37.5665, 126.9780, "Asia/Seoul"),
        CityPreset("Beijing", "بیجنگ", "بكين", "बीजिंग", "China", 39.9042, 116.4074, "Asia/Shanghai"),

        // Global Cities - Africa
        CityPreset("Alexandria", "اسکندریہ", "الإسكندرية", "सिकंदरिया", "Egypt", 31.2001, 29.9187, "Africa/Cairo"),
        CityPreset("Casablanca", "کاسابلانکا", "الدار البيضاء", "कासाब्लांका", "Morocco", 33.5731, -7.5898, "Africa/Casablanca"),
        CityPreset("Tunis", "تیونس", "تونس", "ट्यूनिस", "Tunisia", 36.8065, 10.1815, "Africa/Tunis"),
        CityPreset("Algiers", "الجزائر", "الجزائر", "अल्जीयर्स", "Algeria", 36.7538, 3.0588, "Africa/Algiers"),
        CityPreset("Khartoum", "خرطوم", "الخرطوم", "खारतूम", "Sudan", 15.5007, 32.5599, "Africa/Khartoum"),
        CityPreset("Nairobi", "نیروبی", "نيروبي", "नैरोबी", "Kenya", -1.2921, 36.8219, "Africa/Nairobi"),
        CityPreset("Johannesburg", "جوہانسبرگ", "جوهانسبرغ", "जोहान्सबर्ग", "South Africa", -26.2041, 28.0473, "Africa/Johannesburg"),
        CityPreset("Lagos", "لاگوس", "لاغوس", "लागोस", "Nigeria", 6.5244, 3.3792, "Africa/Lagos"),

        // Global Cities - Australia & Oceania
        CityPreset("Sydney", "سڈنی", "سيدني", "सिडनी", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
        CityPreset("Melbourne", "میلبورن", "ملبورن", "मेलबर्न", "Australia", -37.8136, 144.9631, "Australia/Melbourne"),
        CityPreset("Auckland", "آکلینڈ", "أوكلاند", "ऑकलैंड", "New Zealand", -36.8485, 174.7633, "Pacific/Auckland")
    )

    private val _currentLocation = MutableStateFlow(DEFAULT_LOCATION)
    val currentLocation: StateFlow<UserLocation> = _currentLocation.asStateFlow()

    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(KEY_LAT) && prefs.contains(KEY_LON)) {
            val lat = prefs.getFloat(KEY_LAT, 24.8607f).toDouble()
            val lon = prefs.getFloat(KEY_LON, 67.0011f).toDouble()
            val city = prefs.getString(KEY_CITY, "Karachi") ?: "Karachi"
            val country = prefs.getString(KEY_COUNTRY, "Pakistan") ?: "Pakistan"
            val tz = prefs.getString(KEY_TZ, "Asia/Karachi") ?: "Asia/Karachi"
            val acc = if (prefs.contains(KEY_ACCURACY)) prefs.getFloat(KEY_ACCURACY, 0f) else null
            val isGps = prefs.getBoolean(KEY_IS_GPS, false)
            val time = prefs.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

            _currentLocation.value = UserLocation(
                latitude = lat,
                longitude = lon,
                cityName = city,
                countryName = country,
                timeZoneId = tz,
                accuracyMeters = acc,
                isGpsFix = isGps,
                timestamp = time
            )
        } else {
            // Auto-detect based on device TimeZone if no cached fix yet
            val deviceTz = TimeZone.getDefault().id
            val matchedPreset = PRESET_CITIES.firstOrNull { it.timeZoneId == deviceTz }
            if (matchedPreset != null) {
                _currentLocation.value = UserLocation(
                    latitude = matchedPreset.latitude,
                    longitude = matchedPreset.longitude,
                    cityName = matchedPreset.nameEn,
                    countryName = matchedPreset.country,
                    timeZoneId = matchedPreset.timeZoneId,
                    accuracyMeters = null,
                    isGpsFix = false
                )
            }
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isGpsEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {
            false
        }
    }

    fun openLocationSettings(context: Context) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdate(
        context: Context,
        forceFresh: Boolean = false,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        init(context)
        if (!hasLocationPermission(context)) {
            _locationError.value = "Location permission not granted. Please allow location access or choose your city manually."
            onComplete?.invoke(false, "Location permission not granted.")
            return
        }

        if (!isGpsEnabled(context)) {
            _locationError.value = "GPS is disabled on device. Using last saved location."
            onComplete?.invoke(false, "GPS is disabled.")
            return
        }

        // Avoid spamming fresh GPS request if we have an accurate fix < 15 mins old
        val current = _currentLocation.value
        val ageMinutes = (System.currentTimeMillis() - current.timestamp) / 60000
        if (!forceFresh && current.isGpsFix && ageMinutes < 15) {
            _locationError.value = null
            onComplete?.invoke(true, "Using recent location fix (${ageMinutes}m old).")
            return
        }

        _isLocating.value = true
        _locationError.value = null

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()

                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc: Location? ->
                        if (loc != null) {
                            processNewLocation(context, loc)
                            _isLocating.value = false
                            _locationError.value = null
                            onComplete?.invoke(true, "Location acquired successfully.")
                        } else {
                            // Try fallback to last known location
                            fusedClient.lastLocation.addOnSuccessListener { fallbackLoc: Location? ->
                                if (fallbackLoc != null) {
                                    processNewLocation(context, fallbackLoc)
                                    _isLocating.value = false
                                    _locationError.value = null
                                    onComplete?.invoke(true, "Using last known location fix.")
                                } else {
                                    useLocationManagerFallback(context, onComplete)
                                }
                            }.addOnFailureListener {
                                useLocationManagerFallback(context, onComplete)
                            }
                        }
                    }
                    .addOnFailureListener {
                        useLocationManagerFallback(context, onComplete)
                    }
            } catch (e: Exception) {
                useLocationManagerFallback(context, onComplete)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun useLocationManagerFallback(context: Context, onComplete: ((Boolean, String) -> Unit)?) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLoc = when {
                gpsLoc != null && netLoc != null -> if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
                gpsLoc != null -> gpsLoc
                netLoc != null -> netLoc
                else -> null
            }

            if (bestLoc != null) {
                processNewLocation(context, bestLoc)
                _isLocating.value = false
                _locationError.value = null
                onComplete?.invoke(true, "Location acquired via device sensor fallback.")
            } else {
                _isLocating.value = false
                _locationError.value = "GPS fix timed out. Using saved location."
                onComplete?.invoke(false, "GPS fix timed out.")
            }
        } catch (e: Exception) {
            _isLocating.value = false
            _locationError.value = "Could not obtain GPS fix: ${e.message}"
            onComplete?.invoke(false, "Could not obtain GPS fix.")
        }
    }

    private fun processNewLocation(context: Context, location: Location) {
        val lat = location.latitude
        val lon = location.longitude
        val accuracy = if (location.hasAccuracy()) location.accuracy else null

        var cityName = "Detected Location"
        var countryName = ""
        var tzId = TimeZone.getDefault().id

        // Attempt Geocoding
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Detected Location"
                countryName = addr.countryName ?: ""
            }
        } catch (_: Exception) {}

        // Match closest preset city if name is unknown
        if (cityName == "Detected Location") {
            val closest = findClosestPreset(lat, lon)
            if (closest != null) {
                cityName = closest.nameEn
                countryName = closest.country
                tzId = closest.timeZoneId
            }
        }

        val updated = UserLocation(
            latitude = lat,
            longitude = lon,
            cityName = cityName,
            countryName = countryName,
            timeZoneId = tzId,
            accuracyMeters = accuracy,
            isGpsFix = true,
            timestamp = System.currentTimeMillis()
        )

        _currentLocation.value = updated
        persistLocation(context, updated)
    }

    fun selectPresetCity(context: Context, preset: CityPreset) {
        val updated = UserLocation(
            latitude = preset.latitude,
            longitude = preset.longitude,
            cityName = preset.nameEn,
            countryName = preset.country,
            timeZoneId = preset.timeZoneId,
            accuracyMeters = null,
            isGpsFix = false,
            timestamp = System.currentTimeMillis()
        )
        _currentLocation.value = updated
        _locationError.value = null
        persistLocation(context, updated)
    }

    fun setManualCoordinates(
        context: Context,
        lat: Double,
        lon: Double,
        cityName: String,
        countryName: String,
        timeZoneId: String
    ): Boolean {
        if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            return false
        }
        val validTz = if (TimeZone.getAvailableIDs().contains(timeZoneId)) timeZoneId else TimeZone.getDefault().id
        val updated = UserLocation(
            latitude = lat,
            longitude = lon,
            cityName = cityName.ifBlank { "Custom Location" },
            countryName = countryName,
            timeZoneId = validTz,
            accuracyMeters = null,
            isGpsFix = false,
            timestamp = System.currentTimeMillis()
        )
        _currentLocation.value = updated
        _locationError.value = null
        persistLocation(context, updated)
        return true
    }

    private fun persistLocation(context: Context, loc: UserLocation) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat(KEY_LAT, loc.latitude.toFloat())
            putFloat(KEY_LON, loc.longitude.toFloat())
            putString(KEY_CITY, loc.cityName)
            putString(KEY_COUNTRY, loc.countryName)
            putString(KEY_TZ, loc.timeZoneId)
            if (loc.accuracyMeters != null) {
                putFloat(KEY_ACCURACY, loc.accuracyMeters)
            } else {
                remove(KEY_ACCURACY)
            }
            putBoolean(KEY_IS_GPS, loc.isGpsFix)
            putLong(KEY_TIMESTAMP, loc.timestamp)
            apply()
        }
    }

    fun findClosestPreset(lat: Double, lon: Double): CityPreset? {
        return PRESET_CITIES.minByOrNull { city ->
            val dLat = Math.toRadians(city.latitude - lat)
            val dLon = Math.toRadians(city.longitude - lon)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(city.latitude)) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2)
            2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        }
    }
}
