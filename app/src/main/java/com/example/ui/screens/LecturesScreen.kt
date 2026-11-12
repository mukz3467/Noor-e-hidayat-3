package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.repository.YouTubeRepository
import com.example.data.repository.YouTubeVideoItem
import com.example.localization.AppLocalization
import com.example.ui.components.InAppYouTubePlayer
import com.example.ui.components.NoorTopBar
import com.example.ui.components.YouTubeAuthManager
import com.example.ui.components.YouTubeSignInDialog
import kotlinx.coroutines.launch

private val TOPIC_CATEGORIES = listOf(
    "All Topics",
    "🕌 Worship & Namaz",
    "ﷺ Seerat un Nabi",
    "📜 Prophets Stories",
    "📖 Quran & Tafseer",
    "❤️ Tazkiyah & Ethics",
    "🏛️ History & Kaaba"
)

private val TRENDING_TOPIC_CHIPS = listOf(
    "🕌 Namaz Ka Tareeqa",
    "🌙 Tahajjud Ki Fazeelat",
    "🤲 Tawbah & Astaghfar",
    "🌙 Ramadan & Roza",
    "✨ Power of Dua",
    "🕋 Seerat un Nabi ﷺ",
    "🏔️ First Revelation at Cave Hira",
    "🐫 The Hijrah to Madinah",
    "📜 Farewell Pilgrimage (Hajjat ul Wida)",
    "📜 Stories of the Prophets",
    "👑 Prophet Yusuf (AS)",
    "⚡ Prophet Musa (AS)",
    "🔥 Prophet Ibrahim (AS)",
    "🏛️ Companions of the Cave",
    "📖 Dr Israr Ahmed Tafseer",
    "🎵 Quran Recitations",
    "🛡️ Surah Al-Kahf & End Times",
    "❤️ Purification of Heart (Tazkiyah)",
    "🕊️ Patience in Trials (Sabr)",
    "⭐ Islamic Manners (Adab)",
    "🕋 History of Holy Kaaba",
    "🏛️ Al-Aqsa & Jerusalem Heritage",
    "🔭 Golden Age of Islamic Science",
    "🎬 Islamic Dramas & Parables",
    "👨‍👩‍👧‍👦 Kids & Family Guidance",
    "🎙️ Islamic Podcasts"
)

@Composable
fun LecturesScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    initialQuery: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedScholarKey by remember { mutableStateOf("israr") }
    var selectedCategory by remember { mutableStateOf("All Topics") }
    var searchQuery by remember { mutableStateOf(initialQuery ?: "Dr Israr Ahmed Bayan") }
    var isSearching by remember { mutableStateOf(false) }

    // Initial video is Dr. Israr Ahmed - Zindagi Ki Haqeeqat (from user's curated collection)
    val defaultInitialVideo = YouTubeRepository.CURATED_SCHOLAR_VIDEOS["israr"]?.first()
        ?: YouTubeRepository.ALL_CURATED_LECTURES[0]
    var activeVideo by remember { mutableStateOf(defaultInitialVideo) }

    var videoList by remember { mutableStateOf(YouTubeRepository.CURATED_SCHOLAR_VIDEOS["israr"] ?: YouTubeRepository.ALL_CURATED_LECTURES) }
    var showSignInDialog by remember { mutableStateOf(false) }
    var authRefreshTrigger by remember { mutableIntStateOf(0) }
    val isUserSignedIn = remember(authRefreshTrigger) {
        YouTubeAuthManager.isSignedIn(context)
    }

    if (showSignInDialog) {
        YouTubeSignInDialog(
            onDismissRequest = { showSignInDialog = false },
            onSignInSuccess = {
                showSignInDialog = false
                authRefreshTrigger++
            }
        )
    }

    // Fetch or filter videos when scholar chip, search query or topic changes
    fun loadVideos(query: String, scholarKey: String?) {
        coroutineScope.launch {
            isSearching = true
            val effectiveQuery = when {
                query.isNotBlank() -> query
                scholarKey != null && scholarKey != "All" -> {
                    YouTubeRepository.FEATURED_SCHOLARS.firstOrNull { it.key == scholarKey }?.nameEnglish ?: scholarKey
                }
                else -> ""
            }

            val results = if (effectiveQuery.isBlank()) {
                YouTubeRepository.ALL_CURATED_LECTURES
            } else if (query.isBlank() && scholarKey != null && YouTubeRepository.CURATED_SCHOLAR_VIDEOS.containsKey(scholarKey)) {
                YouTubeRepository.CURATED_SCHOLAR_VIDEOS[scholarKey] ?: YouTubeRepository.ALL_CURATED_LECTURES
            } else {
                YouTubeRepository.searchYouTube(effectiveQuery)
            }

            videoList = results
            if (results.isNotEmpty()) {
                activeVideo = results[0]
            }
            isSearching = false
        }
    }

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            searchQuery = initialQuery
            loadVideos(initialQuery, null)
        }
    }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("lectures", language),
                language = language,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Banner
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(
                        text = "ISLAMIC LECTURES & PODCASTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Watch and listen to authentic bayans from renowned scholars",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Watch and listen to authentic Islamic bayans, podcasts, and Tafseer lectures directly inside the app with zero external links.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar with explicit [Search] Button & Clear (X)
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search lectures, scholars or Bayanat...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    loadVideos("", selectedScholarKey)
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotBlank()) {
                                loadVideos(searchQuery, null)
                            }
                        }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_lectures_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                loadVideos(searchQuery, null)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier.height(52.dp).testTag("search_submit_btn")
                    ) {
                        Text("Search", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Active Video Player Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .testTag("in_app_lecture_player")
                    ) {
                        key(activeVideo.id) {
                            InAppYouTubePlayer(
                                videoId = activeVideo.id,
                                modifier = Modifier.fillMaxSize(),
                                autoplay = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Active Lecture Details Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = activeVideo.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${activeVideo.channel}${if (activeVideo.duration.isNotBlank()) " • ${activeVideo.duration}" else ""}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (activeVideo.views.isNotBlank()) {
                                    Text(
                                        text = activeVideo.views,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // In-App YouTube Account & Sign-In Card (100% Inside the App, Zero External Redirects)
                            if (!isUserSignedIn) {
                                Surface(
                                    onClick = { showSignInDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("lectures_signin_youtube_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "Sign In",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Sign in to YouTube",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Sign in to unlock all YouTube bayans & search results without bot checks",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = { showSignInDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().testTag("lectures_signed_in_badge")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "YouTube Account Connected ✓",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                                    color = Color(0xFF10B981)
                                                )
                                                Text(
                                                    text = "All YouTube bayans & search results unlocked directly inside the app",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        TextButton(
                                            onClick = {
                                                YouTubeAuthManager.signOut(context)
                                                authRefreshTrigger++
                                            }
                                        ) {
                                            Text("Sign Out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FEATURED SCHOLARS Row
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FEATURED SCHOLARS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(YouTubeRepository.FEATURED_SCHOLARS) { scholar ->
                            val isSelected = selectedScholarKey == scholar.key
                            Card(
                                onClick = {
                                    selectedScholarKey = scholar.key
                                    searchQuery = "${scholar.nameEnglish} Bayan"
                                    loadVideos("", scholar.key)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.testTag("scholar_chip_${scholar.key}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = scholar.nameEnglish,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = scholar.nameUrdu,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TRENDING TOPICS Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRENDING TOPICS",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Tap any topic to watch instantly",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Topic Categories Filter Row
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TOPIC_CATEGORIES) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    val cleanCat = cat.replace(Regex("[^a-zA-Z &]"), "").trim()
                                    if (cat != "All Topics") {
                                        searchQuery = cleanCat
                                        loadVideos(cleanCat, null)
                                    } else {
                                        searchQuery = "Islamic Lectures"
                                        loadVideos("", "israr")
                                    }
                                },
                                label = { Text(cat, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Trending Topic Quick Chips Row 1
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TRENDING_TOPIC_CHIPS.take(13)) { topic ->
                            val cleanTopic = topic.replace(Regex("[^a-zA-Z0-9 &ﷺ()]+"), "").trim()
                            SuggestionChip(
                                onClick = {
                                    searchQuery = cleanTopic
                                    loadVideos(cleanTopic, null)
                                },
                                label = { Text(topic, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Trending Topic Quick Chips Row 2
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TRENDING_TOPIC_CHIPS.drop(13)) { topic ->
                            val cleanTopic = topic.replace(Regex("[^a-zA-Z0-9 &ﷺ()]+"), "").trim()
                            SuggestionChip(
                                onClick = {
                                    searchQuery = cleanTopic
                                    loadVideos(cleanTopic, null)
                                },
                                label = { Text(topic, fontSize = 12.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Results Counter & Refresh Row
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Showing results for: \"$searchQuery\"",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { loadVideos(searchQuery, selectedScholarKey) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh", fontSize = 12.sp)
                    }
                }
            }

            // 2-COLUMN GRID OF VIDEO CARDS (Matching user screenshot exactly!)
            val videoPairs = videoList.chunked(2)
            items(videoPairs, key = { it.first().id + it.first().title }) { pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    for (video in pair) {
                        Box(modifier = Modifier.weight(1f)) {
                            GridVideoCard(
                                video = video,
                                isPlayingNow = activeVideo.id == video.id,
                                onClick = { activeVideo = video }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 2-Column Vertical Video Card matching user's design reference:
 * - 16:9 Thumbnail
 * - Central Golden Circular Play button
 * - Duration badge at bottom-right
 * - Video title in bold (2 lines)
 * - Channel name
 * - Bottom row: "Watch in-app" indicator + Views count
 */
@Composable
fun GridVideoCard(
    video: YouTubeVideoItem,
    isPlayingNow: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlayingNow) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlayingNow) 3.dp else 1.5.dp),
        border = if (isPlayingNow) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("grid_lecture_card_${video.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail container with center Play icon and duration overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark vignette overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )

                // Centered Golden Circular Play Button (as in user screenshot)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.5.dp, Color(0xFFF59E0B), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Duration badge at bottom-right
                if (video.duration.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = video.duration,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Text content padding
            Column(modifier = Modifier.padding(10.dp)) {
                // Title (2 lines max)
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Channel Name
                Text(
                    text = video.channel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: "Watch in-app" indicator + Views count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Watch in-app",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (video.views.isNotBlank()) {
                        Text(
                            text = video.views,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
