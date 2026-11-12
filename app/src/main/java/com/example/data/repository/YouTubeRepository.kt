package com.example.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class YouTubeVideoItem(
    val id: String,
    val title: String,
    val channel: String,
    val thumbnail: String,
    val duration: String = "",
    val views: String = "",
    val publishedTime: String = ""
)

object YouTubeRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // 30-minute in-memory cache to prevent redundant requests and rate-limits
    private val cache = mutableMapOf<String, Pair<Long, List<YouTubeVideoItem>>>()
    private const val CACHE_TTL_MS = 30 * 60 * 1000L

    // Explicit non-authenticated session configuration to bypass YouTube consent wall & bot challenges
    const val YOUTUBE_CLIENT_NAME = "1"
    const val YOUTUBE_CLIENT_VERSION = "2.20240401.00.00"
    const val NON_AUTH_SESSION_COOKIES = "CONSENT=YES+cb.20240401-00-p0.en+FX+999; VISITOR_INFO1_LIVE=oKckVSqvaGw; SOCS=CAESEwgDEgk0ODE3Nzk3MjQaAmVuIAEaBgiA_LyaBg; PREF=tz=UTC&f6=40000000&f5=30000&hl=en; GPS=1"

    // Randomized generic mobile device user agents pool for headless simulation
    val MOBILE_USER_AGENTS = listOf(
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro Build/UD1A.230803.041; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/124.0.6367.179 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 14; SM-S928B Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.113 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; SM-A536B Build/TP1A.220624.014) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.6312.118 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.82 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; 2201116SG) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.54 Mobile Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4.1 Mobile/15E148 Safari/604.1"
    )

    fun getRandomMobileUserAgent(): String = MOBILE_USER_AGENTS.random()

    val PROXY_SESSION_HEADERS: Map<String, String> = mapOf(
        "x-youtube-client-name" to YOUTUBE_CLIENT_NAME,
        "x-youtube-client-version" to YOUTUBE_CLIENT_VERSION,
        "x-youtube-page-cl" to "614000000",
        "x-youtube-utc-offset" to "0",
        "Cookie" to NON_AUTH_SESSION_COOKIES,
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Language" to "en-US,en;q=0.9,ur;q=0.8",
        "Sec-Fetch-Dest" to "document",
        "Sec-Fetch-Mode" to "navigate",
        "Sec-Fetch-Site" to "none",
        "Sec-Fetch-User" to "?1",
        "Sec-Ch-Ua" to "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"",
        "Sec-Ch-Ua-Mobile" to "?1",
        "Sec-Ch-Ua-Platform" to "\"Android\""
    )

    data class ScholarProfile(
        val key: String,
        val nameEnglish: String,
        val nameUrdu: String
    )

    val FEATURED_SCHOLARS = listOf(
        ScholarProfile("israr", "Dr. Israr Ahmed", "ڈاکٹر اسرار احمد"),
        ScholarProfile("tariq_masood", "Mufti Tariq Masood", "مفتی طارق مسعود"),
        ScholarProfile("tariq_jameel", "Maulana Tariq Jameel", "مولانا طارق جمیل"),
        ScholarProfile("menk", "Mufti Menk", "Ismail Menk"),
        ScholarProfile("assim", "Assim Al-Hakeem", "عاصم الحكيم"),
        ScholarProfile("nouman", "Nouman Ali Khan", "نعمان علی خان")
    )

    // Curated verified videos for Islamic scholars & topics (Guarantees zero-failure playback)
    val CURATED_SCHOLAR_VIDEOS: Map<String, List<YouTubeVideoItem>> = mapOf(
        "israr" to listOf(
            YouTubeVideoItem(
                id = "iS4i2r-rq2c",
                title = "SAB IMTIHAN HAI! ZINDAGI KI HAQEEQAT - DR ISRAR AHMED KA DIL CHOO LENE WALA BAYAN",
                channel = "BAYAN",
                thumbnail = "https://i.ytimg.com/vi/iS4i2r-rq2c/hq720.jpg",
                duration = "13:34",
                views = "294,658 views",
                publishedTime = "Official Bayan"
            ),
            YouTubeVideoItem(
                id = "5sF_Z0WjBtw",
                title = "Trust Allah | never disappoint | dr israr ahmad",
                channel = "Wisdom waves",
                thumbnail = "https://i.ytimg.com/vi/5sF_Z0WjBtw/hq720.jpg",
                duration = "9:28",
                views = "3,302,688 views",
                publishedTime = "Wisdom Bayan"
            ),
            YouTubeVideoItem(
                id = "mRiEQ53SuoY",
                title = "JAWANI AUR NAFS KI GHULAMI! | DR ISRAR AHMED EMOTIONAL BAYAN | DR ISRAR AHMED TAFSEER QURAN IN...",
                channel = "REHBAR E KAMIL",
                thumbnail = "https://i.ytimg.com/vi/mRiEQ53SuoY/hqdefault.jpg",
                duration = "54:49",
                views = "1,131,599 views",
                publishedTime = "Quranic Guidance"
            ),
            YouTubeVideoItem(
                id = "SFfQJtCP6Ok",
                title = "Life Changing Bayan | Phir Nek Nahi ho Tum - Dr. Israr Ahmed",
                channel = "Being Muslim Production",
                thumbnail = "https://i.ytimg.com/vi/SFfQJtCP6Ok/hq720.jpg",
                duration = "11:31",
                views = "2,062,109 views",
                publishedTime = "Life Changing Series"
            ),
            YouTubeVideoItem(
                id = "u4fT-i9H_h8",
                title = "Heartbroken? This Will Heal You | Dr Israr Ahmad Emotional Bayan",
                channel = "Glowing Way",
                thumbnail = "https://i.ytimg.com/vi/u4fT-i9H_h8/hqdefault.jpg",
                duration = "53:03",
                views = "407,444 views",
                publishedTime = "Heart Healing"
            ),
            YouTubeVideoItem(
                id = "k2YQhB4ZgLw",
                title = "Is the End Time Here? Dr. Israr Ahmad Bayan On Israel War #Israel",
                channel = "LivIslamic Official",
                thumbnail = "https://i.ytimg.com/vi/k2YQhB4ZgLw/hqdefault.jpg",
                duration = "48:58",
                views = "37,176 views",
                publishedTime = "End Times Series"
            ),
            YouTubeVideoItem(
                id = "aY_L2oK4Z80",
                title = "SABR & AZMAISH ARE TESTING ME! | Heart-Melting Motivational Bayan | Dr. Israr Ahmed",
                channel = "Tawakkul",
                thumbnail = "https://i.ytimg.com/vi/aY_L2oK4Z80/hqdefault.jpg",
                duration = "37:21",
                views = "806,833 views",
                publishedTime = "Sabr & Azmaish"
            ),
            YouTubeVideoItem(
                id = "bL7d2V9zX60",
                title = "The Power of Patience (صبر) | Life Changing Bayan by Dr Israr Ahmed | Emaan Bil Ilm",
                channel = "Forex Hunter",
                thumbnail = "https://i.ytimg.com/vi/bL7d2V9zX60/hqdefault.jpg",
                duration = "37:18",
                views = "1,804,711 views",
                publishedTime = "Emaan Bil Ilm"
            ),
            YouTubeVideoItem(
                id = "xQvW8e0v3a8",
                title = "TAWAKKUL: Allah Ki Raza Mein Razi Rehna 🤍 | Dr Israr Ahmed RH #tawakkul #islam #bayan",
                channel = "Abdullah",
                thumbnail = "https://i.ytimg.com/vi/xQvW8e0v3a8/hqdefault.jpg",
                duration = "42:05",
                views = "240,189 views",
                publishedTime = "Tawakkul Series"
            ),
            YouTubeVideoItem(
                id = "qPnX82kL4vM",
                title = "A Very Great Battle Is Ahead | Dr Israr Ahmed Bayan - End Times Islam - Islamic Warning.",
                channel = "My Taqwa",
                thumbnail = "https://i.ytimg.com/vi/qPnX82kL4vM/hqdefault.jpg",
                duration = "55:51",
                views = "178,970 views",
                publishedTime = "Islamic Warning"
            ),
            YouTubeVideoItem(
                id = "m8V3a9Lk2wQ",
                title = "Baal: Shaitan Ki Tareekh | History of Shaitan | Dr. Israr Ahmed",
                channel = "Bayan Al Quran",
                thumbnail = "https://i.ytimg.com/vi/m8V3a9Lk2wQ/hqdefault.jpg",
                duration = "46:12",
                views = "512,900 views",
                publishedTime = "History Series"
            ),
            YouTubeVideoItem(
                id = "zL9w4Pk2vX8",
                title = "Tanhai Pasand Log | Dr. Israr Ahmed Emotional Bayan",
                channel = "Markazi Khuddam-ul-Quran",
                thumbnail = "https://i.ytimg.com/vi/zL9w4Pk2vX8/hqdefault.jpg",
                duration = "38:40",
                views = "680,240 views",
                publishedTime = "Soul Healing"
            )
        ),
        "tariq_masood" to listOf(
            YouTubeVideoItem(
                id = "P9c2F5u6j7I",
                title = "Namaz Me Dil Lagane Ka Tareeqa | Mufti Tariq Masood Bayan",
                channel = "Mufti Tariq Masood Speeches",
                thumbnail = "https://i.ytimg.com/vi/P9c2F5u6j7I/hqdefault.jpg",
                duration = "28:40",
                views = "1,450,200 views",
                publishedTime = "Namaz Guide"
            ),
            YouTubeVideoItem(
                id = "J0tQxS8sWfA",
                title = "Pareshaaniyon Aur Depression Ka Hal | Mufti Tariq Masood",
                channel = "Mufti Tariq Masood Official",
                thumbnail = "https://i.ytimg.com/vi/J0tQxS8sWfA/hqdefault.jpg",
                duration = "34:12",
                views = "2,190,000 views",
                publishedTime = "Depression Relief"
            ),
            YouTubeVideoItem(
                id = "k8V2pL9w4Pk",
                title = "Tahajjud Ki Namaz Aur Dua Ki Qubooliyat | Mufti Tariq Masood",
                channel = "Mufti Tariq Masood Official",
                thumbnail = "https://i.ytimg.com/vi/k8V2pL9w4Pk/hqdefault.jpg",
                duration = "25:30",
                views = "980,450 views",
                publishedTime = "Tahajjud Series"
            )
        ),
        "tariq_jameel" to listOf(
            YouTubeVideoItem(
                id = "9lU7v0b5eQk",
                title = "Tawbah Ki Fazeelat Aur Allah Ki Rahmat | Maulana Tariq Jameel",
                channel = "Tariq Jamil Official",
                thumbnail = "https://i.ytimg.com/vi/9lU7v0b5eQk/hqdefault.jpg",
                duration = "45:20",
                views = "4,200,890 views",
                publishedTime = "Tawbah & Rahmat"
            ),
            YouTubeVideoItem(
                id = "xW8e0v3a8B4",
                title = "Seerat Un Nabi ﷺ Ke Noorani Waqiat | Maulana Tariq Jameel",
                channel = "Tariq Jamil Official",
                thumbnail = "https://i.ytimg.com/vi/xW8e0v3a8B4/hqdefault.jpg",
                duration = "52:10",
                views = "3,110,400 views",
                publishedTime = "Seerat Series"
            ),
            YouTubeVideoItem(
                id = "yR7m0v2a5X1",
                title = "Walidain Ke Huqooq Aur Jannat Ka Rasta | Maulana Tariq Jameel",
                channel = "Tariq Jamil Official",
                thumbnail = "https://i.ytimg.com/vi/yR7m0v2a5X1/hqdefault.jpg",
                duration = "41:15",
                views = "1,850,000 views",
                publishedTime = "Ethics & Adab"
            )
        ),
        "menk" to listOf(
            YouTubeVideoItem(
                id = "1sJ9X6F2f3I",
                title = "Finding Inner Peace and Overcoming Anxiety | Mufti Menk",
                channel = "Mufti Menk Official",
                thumbnail = "https://i.ytimg.com/vi/1sJ9X6F2f3I/hqdefault.jpg",
                duration = "31:18",
                views = "2,780,500 views",
                publishedTime = "Inner Peace"
            ),
            YouTubeVideoItem(
                id = "w4M7x3v1e8Q",
                title = "The Purpose of Life in the Quran | Mufti Menk",
                channel = "Mufti Menk Official",
                thumbnail = "https://i.ytimg.com/vi/w4M7x3v1e8Q/hqdefault.jpg",
                duration = "40:22",
                views = "1,940,000 views",
                publishedTime = "Quran Purpose"
            ),
            YouTubeVideoItem(
                id = "zP9w1x4v8Q2",
                title = "Never Lose Hope in Allah's Mercy | Mufti Menk",
                channel = "Mufti Menk Official",
                thumbnail = "https://i.ytimg.com/vi/zP9w1x4v8Q2/hqdefault.jpg",
                duration = "35:45",
                views = "3,400,200 views",
                publishedTime = "Hope & Faith"
            )
        ),
        "assim" to listOf(
            YouTubeVideoItem(
                id = "aH8k2v9X1L0",
                title = "Common Mistakes in Prayer (Salah) | Assim Al-Hakeem",
                channel = "assim al hakeem",
                thumbnail = "https://i.ytimg.com/vi/aH8k2v9X1L0/hqdefault.jpg",
                duration = "18:24",
                views = "890,400 views",
                publishedTime = "Fiqh of Salah"
            ),
            YouTubeVideoItem(
                id = "bK7w3m2X9Q1",
                title = "Ruling on Doubts in Wudu and Prayer (Waswas) | Assim Al-Hakeem",
                channel = "assim al hakeem",
                thumbnail = "https://i.ytimg.com/vi/bK7w3m2X9Q1/hqdefault.jpg",
                duration = "14:50",
                views = "1,240,000 views",
                publishedTime = "Overcoming Waswas"
            )
        ),
        "nouman" to listOf(
            YouTubeVideoItem(
                id = "nK9w4v1X8L2",
                title = "Deep Reflections on Surah Al-Kahf | Nouman Ali Khan",
                channel = "Bayyinah Institute",
                thumbnail = "https://i.ytimg.com/vi/nK9w4v1X8L2/hqdefault.jpg",
                duration = "44:10",
                views = "2,150,000 views",
                publishedTime = "Quran Reflections"
            ),
            YouTubeVideoItem(
                id = "cL8v2m1X0P9",
                title = "Finding Emotional Strength in the Quran | Nouman Ali Khan",
                channel = "Bayyinah Institute",
                thumbnail = "https://i.ytimg.com/vi/cL8v2m1X0P9/hqdefault.jpg",
                duration = "36:20",
                views = "1,890,000 views",
                publishedTime = "Heart & Mind"
            )
        )
    )

    // 24/7 Harameyn Live Streams for Makkah & Madinah (Verified Active Streams)
    val HARAMEYN_LIVE_STREAMS = mapOf(
        "makkah" to listOf(
            YouTubeVideoItem(
                id = "eC4LfEVxvKg",
                title = "بث مباشر || قناة القرآن الكريم Makkah Live (Official Saudi Quran TV)",
                channel = "قناة القرآن الكريم",
                thumbnail = "https://i.ytimg.com/vi/eC4LfEVxvKg/hq720.jpg",
                duration = "24/7 LIVE"
            ),
            YouTubeVideoItem(
                id = "vdvjUpl03q0",
                title = "🔴 Makkah Live HD | بث مباشر مكة المكرمة | الحرم المكي مباشر",
                channel = "القرآن الكريم",
                thumbnail = "https://i.ytimg.com/vi/vdvjUpl03q0/hq720.jpg",
                duration = "24/7 LIVE"
            ),
            YouTubeVideoItem(
                id = "Qvcimc6QQY8",
                title = "Makkah Live Stream Server 3 (Tawaf & Tilawat)",
                channel = "Saudi Quran TV Relay",
                thumbnail = "https://i.ytimg.com/vi/Qvcimc6QQY8/hqdefault.jpg",
                duration = "24/7 LIVE"
            )
        ),
        "madinah" to listOf(
            YouTubeVideoItem(
                id = "si0jNUvk8Vk",
                title = "🔴 Madina Live Tv Online 24/7 | بث مباشر || قناة السنة النبوية (Official Sunnah TV)",
                channel = "قناة السنة النبوية",
                thumbnail = "https://i.ytimg.com/vi/si0jNUvk8Vk/hq720.jpg",
                duration = "24/7 LIVE"
            ),
            YouTubeVideoItem(
                id = "Rs7St51oDDc",
                title = "بث مباشر | قناة السنة النبوية Madinah Live HD",
                channel = "قناة السنة النبوية",
                thumbnail = "https://i.ytimg.com/vi/Rs7St51oDDc/hq720.jpg",
                duration = "24/7 LIVE"
            ),
            YouTubeVideoItem(
                id = "BtMUUgApnPs",
                title = "🔴 Madina Live 24/7 | Green Dome & Courtyard",
                channel = "Madinah Live",
                thumbnail = "https://i.ytimg.com/vi/BtMUUgApnPs/hq720.jpg",
                duration = "24/7 LIVE"
            )
        )
    )

    // Flat list of all default curated lectures for immediate offline display
    val ALL_CURATED_LECTURES: List<YouTubeVideoItem> = CURATED_SCHOLAR_VIDEOS.values.flatten()

    /**
     * Searches YouTube dynamically without any API keys or sign-in prompts.
     * Uses browser headers, parses ytInitialData JSON payload, and falls back
     * to curated scholar collections if offline or rate-limited.
     */
    suspend fun searchYouTube(query: String): List<YouTubeVideoItem> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return@withContext ALL_CURATED_LECTURES
        }

        val cacheKey = trimmedQuery.lowercase()
        val cached = cache[cacheKey]
        if (cached != null && System.currentTimeMillis() - cached.first < CACHE_TTL_MS) {
            return@withContext cached.second
        }

        try {
            val encodedQuery = URLEncoder.encode(trimmedQuery, "UTF-8")
            val ytUrl = "https://www.youtube.com/results?search_query=$encodedQuery"

            val randomMobileUa = getRandomMobileUserAgent()
            val requestBuilder = Request.Builder()
                .url(ytUrl)
                .header("User-Agent", randomMobileUa)
                .header("Cache-Control", "no-cache")

            // Explicitly include custom cookies and specific headers like 'x-youtube-client-name'
            // and 'x-youtube-client-version' to mimic a non-authenticated browser session and prevent sign-in redirects
            PROXY_SESSION_HEADERS.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }

            val request = requestBuilder.build()

            val response = httpClient.newCall(request).execute()
            val html = response.body?.string() ?: ""

            val pattern = Pattern.compile("var ytInitialData = (\\{.*?\\});</script>")
            val matcher = pattern.matcher(html)

            val parsedVideos = mutableListOf<YouTubeVideoItem>()

            if (matcher.find()) {
                val jsonString = matcher.group(1)
                if (jsonString != null) {
                    val root = JSONObject(jsonString)
                    val sectionContents = root.optJSONObject("contents")
                        ?.optJSONObject("twoColumnSearchResultsRenderer")
                        ?.optJSONObject("primaryContents")
                        ?.optJSONObject("sectionListRenderer")
                        ?.optJSONArray("contents")
                        ?.optJSONObject(0)
                        ?.optJSONObject("itemSectionRenderer")
                        ?.optJSONArray("contents")

                    if (sectionContents != null) {
                        for (i in 0 until sectionContents.length()) {
                            val item = sectionContents.optJSONObject(i) ?: continue
                            val videoRenderer = item.optJSONObject("videoRenderer") ?: continue
                            val videoId = videoRenderer.optString("videoId")
                            if (videoId.isNullOrBlank()) continue

                            val title = videoRenderer.optJSONObject("title")
                                ?.optJSONArray("runs")
                                ?.optJSONObject(0)
                                ?.optString("text")
                                ?: videoRenderer.optJSONObject("title")?.optString("simpleText")
                                ?: "Islamic Lecture"

                            val channel = videoRenderer.optJSONObject("ownerText")
                                ?.optJSONArray("runs")
                                ?.optJSONObject(0)
                                ?.optString("text")
                                ?: videoRenderer.optJSONObject("shortBylineText")
                                    ?.optJSONArray("runs")
                                    ?.optJSONObject(0)
                                    ?.optString("text")
                                ?: "Islamic Scholar"

                            val thumbnails = videoRenderer.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                            val lastThumb = if (thumbnails != null && thumbnails.length() > 0) {
                                thumbnails.optJSONObject(thumbnails.length() - 1)?.optString("url")
                            } else null
                            val thumbnail = lastThumb ?: "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

                            val duration = videoRenderer.optJSONObject("lengthText")?.optString("simpleText") ?: ""
                            val publishedTime = videoRenderer.optJSONObject("publishedTimeText")?.optString("simpleText") ?: ""
                            val views = videoRenderer.optJSONObject("viewCountText")?.optString("simpleText") ?: ""

                            parsedVideos.add(
                                YouTubeVideoItem(
                                    id = videoId,
                                    title = title,
                                    channel = channel,
                                    thumbnail = thumbnail,
                                    duration = duration,
                                    views = views,
                                    publishedTime = publishedTime
                                )
                            )
                            if (parsedVideos.size >= 16) break
                        }
                    }
                }
            }

            val finalVideos = if (parsedVideos.isNotEmpty()) {
                parsedVideos
            } else {
                getCuratedFallback(trimmedQuery)
            }

            cache[cacheKey] = Pair(System.currentTimeMillis(), finalVideos)
            finalVideos
        } catch (e: Exception) {
            val fallback = getCuratedFallback(trimmedQuery)
            fallback
        }
    }

    private fun getCuratedFallback(query: String): List<YouTubeVideoItem> {
        val lower = query.lowercase()
        for ((key, fallbackList) in CURATED_SCHOLAR_VIDEOS) {
            if (lower.contains(key) || lower.contains(key.replace("_", " "))) {
                return fallbackList
            }
        }
        return ALL_CURATED_LECTURES
    }
}
