package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.Dua
import com.example.data.repository.DuaAudioPlayer
import com.example.data.repository.DuaData
import com.example.data.repository.UserPreferencesRepository
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.QuranNaskhFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuasScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefsRepo = remember { UserPreferencesRepository(context) }

    // State collections from preferences
    val favoriteIds by prefsRepo.favoriteDuaIds.collectAsState()
    val recentViewedIds by prefsRepo.recentViewedDuaIds.collectAsState()
    val arabicFontSizePref by prefsRepo.arabicFontSize.collectAsState()

    // Screen local state
    var selectedCategory by remember { mutableStateOf("all") } // "all", "favorites", "recent", or categoryId
    var searchQuery by remember { mutableStateOf("") }
    var selectedDuaForDetails by remember { mutableStateOf<Dua?>(null) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    // Audio states
    val currentlyPlayingId by DuaAudioPlayer.currentDuaId.collectAsState()
    val isPlayingAudio by DuaAudioPlayer.isPlaying.collectAsState()
    val currentLoop by DuaAudioPlayer.currentLoop.collectAsState()
    val targetRepeat by DuaAudioPlayer.repeatCount.collectAsState()

    // Initialize audio player
    LaunchedEffect(Unit) {
        DuaAudioPlayer.init(context)
    }

    // Daily Featured Dua
    val dailyFeaturedDua = remember { DuaData.getDailyFeaturedDua() }

    // Filtered list calculation
    val filteredDuas = remember(selectedCategory, searchQuery, favoriteIds, recentViewedIds) {
        DuaData.duas.filter { dua ->
            val matchCategory = when (selectedCategory) {
                "all" -> true
                "favorites" -> favoriteIds.contains(dua.id)
                "recent" -> recentViewedIds.contains(dua.id)
                else -> dua.categoryId == selectedCategory
            }

            val q = searchQuery.trim()
            val matchQuery = if (q.isBlank()) true else {
                dua.titleEn.contains(q, ignoreCase = true) ||
                        dua.titleUr.contains(q) ||
                        dua.titleAr.contains(q) ||
                        dua.titleHi.contains(q) ||
                        dua.arabic.contains(q) ||
                        dua.transliteration.contains(q, ignoreCase = true) ||
                        dua.translationEn.contains(q, ignoreCase = true) ||
                        dua.translationUr.contains(q) ||
                        dua.reference.contains(q, ignoreCase = true) ||
                        dua.grade.contains(q, ignoreCase = true)
            }

            matchCategory && matchQuery
        }
    }

    val isRtl = language == AppLanguage.URDU || language == AppLanguage.ARABIC

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("duas", language),
                language = language,
                onBackClick = onBack,
                actions = {
                    IconButton(
                        onClick = { showFontSizeDialog = true },
                        modifier = Modifier.testTag("duas_font_size_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Adjust Font Size",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = AppLocalization.get("search_duas_hint", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("duas_search_input")
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // All Duas Chip
                item {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = { selectedCategory = "all" },
                        leadingIcon = {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            Text("${AppLocalization.get("all_duas", language)} (${DuaData.duas.size})")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("dua_chip_all")
                    )
                }

                // Favorites Chip
                item {
                    FilterChip(
                        selected = selectedCategory == "favorites",
                        onClick = { selectedCategory = "favorites" },
                        leadingIcon = {
                            Icon(
                                imageVector = if (selectedCategory == "favorites") Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text("${AppLocalization.get("favorite_duas", language)} (${favoriteIds.size})")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("dua_chip_favorites")
                    )
                }

                // Recently Viewed Chip
                item {
                    FilterChip(
                        selected = selectedCategory == "recent",
                        onClick = { selectedCategory = "recent" },
                        leadingIcon = {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            Text(AppLocalization.get("recent_duas", language))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("dua_chip_recent")
                    )
                }

                // 12 Authentic Dua Categories
                items(DuaData.categories) { cat ->
                    val catTitle = when (language) {
                        AppLanguage.URDU -> cat.titleUr
                        AppLanguage.ARABIC -> cat.titleAr
                        AppLanguage.HINDI -> cat.titleHi
                        else -> cat.titleEn
                    }
                    val isSelected = selectedCategory == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat.id },
                        label = { Text(catTitle) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("dua_chip_${cat.id}")
                    )
                }
            }

            // Duas List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Daily Featured Dua Banner (shown when browsing "all" and not actively searching)
                if (selectedCategory == "all" && searchQuery.isBlank()) {
                    item(key = "daily_featured_dua_banner") {
                        DailyFeaturedDuaCard(
                            dua = dailyFeaturedDua,
                            language = language,
                            arabicFontSize = arabicFontSizePref,
                            onOpenDetails = {
                                prefsRepo.recordViewedDua(dailyFeaturedDua.id)
                                selectedDuaForDetails = dailyFeaturedDua
                            },
                            isPlaying = currentlyPlayingId == dailyFeaturedDua.id && isPlayingAudio,
                            onToggleAudio = {
                                prefsRepo.recordPlayedDua(dailyFeaturedDua.id)
                                DuaAudioPlayer.playDua(
                                    context = context,
                                    duaId = dailyFeaturedDua.id,
                                    arabicText = dailyFeaturedDua.arabic,
                                    transliteration = dailyFeaturedDua.transliteration,
                                    repeatTarget = 1
                                )
                            }
                        )
                    }
                }

                if (filteredDuas.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (selectedCategory == "favorites") "No favorite Duas yet. Tap the heart icon on any Dua to save it here." else "No Duas found matching your query.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredDuas, key = { it.id }) { dua ->
                        val isFav = favoriteIds.contains(dua.id)
                        val isThisPlaying = currentlyPlayingId == dua.id && isPlayingAudio

                        DuaCard(
                            dua = dua,
                            language = language,
                            arabicFontSize = arabicFontSizePref,
                            isFavorite = isFav,
                            isPlaying = isThisPlaying,
                            currentLoop = if (currentlyPlayingId == dua.id) currentLoop else 1,
                            targetRepeat = if (currentlyPlayingId == dua.id) targetRepeat else 1,
                            onToggleFavorite = {
                                val added = prefsRepo.toggleFavoriteDua(dua.id)
                                val msg = if (added) AppLocalization.get("dua_added_fav", language) else AppLocalization.get("dua_removed_fav", language)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onPlayAudio = { repeatCount ->
                                prefsRepo.recordPlayedDua(dua.id)
                                DuaAudioPlayer.playDua(
                                    context = context,
                                    duaId = dua.id,
                                    arabicText = dua.arabic,
                                    transliteration = dua.transliteration,
                                    repeatTarget = repeatCount
                                )
                            },
                            onPauseAudio = {
                                DuaAudioPlayer.pause()
                            },
                            onOpenDetails = {
                                prefsRepo.recordViewedDua(dua.id)
                                selectedDuaForDetails = dua
                            }
                        )
                    }
                }
            }
        }
    }

    // Dua Details Modal Bottom Sheet
    selectedDuaForDetails?.let { detailedDua ->
        DuaDetailBottomSheet(
            dua = detailedDua,
            language = language,
            arabicFontSize = arabicFontSizePref,
            isFavorite = favoriteIds.contains(detailedDua.id),
            isPlaying = currentlyPlayingId == detailedDua.id && isPlayingAudio,
            currentLoop = if (currentlyPlayingId == detailedDua.id) currentLoop else 1,
            targetRepeat = if (currentlyPlayingId == detailedDua.id) targetRepeat else 1,
            onDismiss = { selectedDuaForDetails = null },
            onToggleFavorite = {
                val added = prefsRepo.toggleFavoriteDua(detailedDua.id)
                val msg = if (added) AppLocalization.get("dua_added_fav", language) else AppLocalization.get("dua_removed_fav", language)
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            },
            onPlayAudio = { repeatTarget ->
                prefsRepo.recordPlayedDua(detailedDua.id)
                DuaAudioPlayer.playDua(
                    context = context,
                    duaId = detailedDua.id,
                    arabicText = detailedDua.arabic,
                    transliteration = detailedDua.transliteration,
                    repeatTarget = repeatTarget
                )
            },
            onPauseAudio = {
                DuaAudioPlayer.pause()
            }
        )
    }

    // Font Size Dialog
    if (showFontSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            title = {
                Text(
                    text = AppLocalization.get("arabic_font_size", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontFamily = QuranNaskhFontFamily,
                        fontSize = arabicFontSizePref.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(20f, 24f, 28f, 32f).forEach { size ->
                            val label = when (size) {
                                20f -> "Small"
                                24f -> "Medium"
                                28f -> "Large"
                                else -> "Extra"
                            }
                            val isSelected = (arabicFontSizePref == size)
                            FilterChip(
                                selected = isSelected,
                                onClick = { prefsRepo.setArabicFontSize(size) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontSizeDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

/**
 * Daily Featured Authentic Dua Hero Banner
 */
@Composable
fun DailyFeaturedDuaCard(
    dua: Dua,
    language: AppLanguage,
    arabicFontSize: Float,
    onOpenDetails: () -> Unit,
    isPlaying: Boolean,
    onToggleAudio: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    Color(0xFFE6A100)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_featured_dua_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFE6A100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppLocalization.get("daily_featured_dua", language),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = dua.grade,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Text
            Text(
                text = dua.arabic,
                fontFamily = QuranNaskhFontFamily,
                fontSize = (arabicFontSize - 2f).sp,
                lineHeight = (arabicFontSize + 12f).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDirection = TextDirection.Rtl
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Localized Translation snippet
            val transText = when (language) {
                AppLanguage.URDU -> dua.translationUr
                AppLanguage.HINDI -> dua.translationHi
                else -> dua.translationEn
            }
            Text(
                text = transText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dua.reference,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = onToggleAudio,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play Audio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = onOpenDetails,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Recite & Details", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

/**
 * Modern Islamic Dua Card
 */
@Composable
fun DuaCard(
    dua: Dua,
    language: AppLanguage,
    arabicFontSize: Float,
    isFavorite: Boolean,
    isPlaying: Boolean,
    currentLoop: Int,
    targetRepeat: Int,
    onToggleFavorite: () -> Unit,
    onPlayAudio: (Int) -> Unit,
    onPauseAudio: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val context = LocalContext.current
    var selectedRepeat by remember { mutableIntStateOf(dua.repeatCount.coerceAtLeast(1)) }
    var showAllLanguages by remember { mutableStateOf(false) }

    val title = when (language) {
        AppLanguage.URDU -> dua.titleUr
        AppLanguage.ARABIC -> dua.titleAr
        AppLanguage.HINDI -> dua.titleHi
        else -> dua.titleEn
    }

    val primaryTrans = when (language) {
        AppLanguage.URDU -> dua.translationUr
        AppLanguage.HINDI -> dua.translationHi
        else -> dua.translationEn
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dua_card_${dua.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Category Tag, Authenticity Badge & Favorite Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = dua.grade,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (dua.recommendedTime.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE6A100).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = dua.recommendedTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB57E00),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dua Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Arabic Quranic Calligraphy Text (RTL)
            Text(
                text = dua.arabic,
                fontFamily = QuranNaskhFontFamily,
                fontSize = arabicFontSize.sp,
                lineHeight = (arabicFontSize + 14f).sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Roman Transliteration
            Text(
                text = dua.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Translation
            Text(
                text = primaryTrans,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    textDirection = if (language == AppLanguage.URDU) TextDirection.Rtl else TextDirection.Ltr
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // All Languages Expansion
            AnimatedVisibility(visible = showAllLanguages, enter = fadeIn(), exit = fadeOut()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Urdu: ${dua.translationUr}",
                        style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Rtl),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "English: ${dua.translationEn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hindi: ${dua.translationHi}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reference Source Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📖 ${dua.reference}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (showAllLanguages) "Hide Translations" else "All 4 Languages",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { showAllLanguages = !showAllLanguages }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Audio Controls & Action Toolbar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    // Playback & Repeat Count Selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (isPlaying) onPauseAudio() else onPlayAudio(selectedRepeat)
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = if (isPlaying) "Pause" else "Play Recitation",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        if (isPlaying) {
                            Text(
                                text = "Reciting $currentLoop/$targetRepeat",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        } else {
                            // Repeat Count Pills (1x, 3x, 7x)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                listOf(1, 3, 7).forEach { times ->
                                    val isCur = selectedRepeat == times
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isCur) MaterialTheme.colorScheme.primary else Color.Transparent
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isCur) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable { selectedRepeat = times }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${times}x",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isCur) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Toolbar actions: Copy, Share, Details
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText(
                                    "Islamic Dua",
                                    "${dua.arabic}\n\n${dua.transliteration}\n\n$primaryTrans\n\n[${dua.reference} - Noor-e-Hidayat 2]"
                                )
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, AppLocalization.get("dua_copied", language), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Dua", modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    append("${dua.titleEn} / ${dua.titleUr}\n\n")
                                    append("${dua.arabic}\n\n")
                                    append("Transliteration:\n${dua.transliteration}\n\n")
                                    append("Translation:\n$primaryTrans\n\n")
                                    append("Reference: ${dua.reference} (${dua.grade})\n")
                                    append("Shared via Noor-e-Hidayat 2")
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, dua.titleEn)
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, AppLocalization.get("share_dua", language)))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Dua", modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = onOpenDetails,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Dua Details",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full Dua Detail Modal Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaDetailBottomSheet(
    dua: Dua,
    language: AppLanguage,
    arabicFontSize: Float,
    isFavorite: Boolean,
    isPlaying: Boolean,
    currentLoop: Int,
    targetRepeat: Int,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPlayAudio: (Int) -> Unit,
    onPauseAudio: () -> Unit
) {
    val context = LocalContext.current
    var sheetRepeatTarget by remember { mutableIntStateOf(dua.repeatCount.coerceAtLeast(1)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header: Category, Grade & Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = dua.grade,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE6A100).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Hadith / Reference: ${dua.hadithOrAyahNumber.ifBlank { dua.reference }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB57E00),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Titles
            Text(
                text = dua.titleUr,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDirection = TextDirection.Rtl
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = dua.titleEn,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Full Calligraphy Arabic Text
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dua.arabic,
                    fontFamily = QuranNaskhFontFamily,
                    fontSize = (arabicFontSize + 4f).sp,
                    lineHeight = (arabicFontSize + 18f).sp,
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transliteration
            Text(
                text = "Transliteration:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = dua.transliteration,
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // All 4 Translations Section
            Text(
                text = "Translations in 4 Languages:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Urdu Translation
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "اردو ترجمہ:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = dua.translationUr,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDirection = TextDirection.Rtl,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // English Translation
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "English Translation:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dua.translationEn,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Hindi Translation
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "हिन्दी अनुवाद:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dua.translationHi,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Virtues & Occasion
            if (dua.virtuesBenefitsEn.isNotBlank() || dua.virtuesBenefitsUr.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE6A100).copy(alpha = 0.1f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFE6A100), Color(0xFFFFA000)))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFFC88A00), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppLocalization.get("virtues_benefits", language),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8D6200)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (dua.virtuesBenefitsUr.isNotBlank()) {
                            Text(
                                text = dua.virtuesBenefitsUr,
                                style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Rtl),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        if (dua.virtuesBenefitsEn.isNotBlank()) {
                            Text(
                                text = dua.virtuesBenefitsEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Player Controls Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Recitation Audio & Repetition:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (isPlaying) onPauseAudio() else onPlayAudio(sheetRepeatTarget)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPlaying) "Pause Recitation" else "Play Recitation")
                        }

                        // Repetition Selector (1x, 3x, 7x, 10x)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 3, 7, 10).forEach { r ->
                                FilterChip(
                                    selected = sheetRepeatTarget == r,
                                    onClick = { sheetRepeatTarget = r },
                                    label = { Text("${r}x") }
                                )
                            }
                        }
                    }

                    if (isPlaying) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { currentLoop.toFloat() / targetRepeat.toFloat().coerceAtLeast(1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Playing repetition $currentLoop of $targetRepeat",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Actions (Share & Copy)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "Dua Details",
                            "${dua.titleEn}\n\n${dua.arabic}\n\n${dua.transliteration}\n\nUrdu: ${dua.translationUr}\n\nEnglish: ${dua.translationEn}\n\nReference: ${dua.reference}"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, AppLocalization.get("dua_copied", language), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppLocalization.get("copy_dua", language))
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, dua.titleEn)
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${dua.arabic}\n\n${dua.translationUr}\n\n${dua.translationEn}\n\n[${dua.reference} (${dua.grade}) - Noor-e-Hidayat 2]"
                            )
                        }
                        context.startActivity(Intent.createChooser(intent, AppLocalization.get("share_dua", language)))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppLocalization.get("share_dua", language))
                }
            }
        }
    }
}
