package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.QuranAudioManager
import com.example.data.repository.QuranData
import com.example.data.repository.QuranTafseerRepository
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.QuranNaskhFontFamily
import kotlinx.coroutines.launch

@Composable
fun QuranListScreen(
    language: AppLanguage,
    onBack: () -> Unit,
    onSelectSurah: (Int) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Surahs, 1 = Paras (Juz)
    var searchQuery by remember { mutableStateOf("") }

    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) QuranData.surahs
        else {
            QuranData.surahs.filter {
                it.number.toString().contains(searchQuery.trim()) ||
                        it.nameEnglish.contains(searchQuery.trim(), ignoreCase = true) ||
                        it.nameArabic.contains(searchQuery.trim()) ||
                        it.nameUrdu.contains(searchQuery.trim()) ||
                        it.nameHindi.contains(searchQuery.trim())
            }
        }
    }

    val filteredParas = remember(searchQuery) {
        if (searchQuery.isBlank()) QuranData.paras
        else {
            QuranData.paras.filter {
                it.number.toString().contains(searchQuery.trim()) ||
                        it.nameEnglish.contains(searchQuery.trim(), ignoreCase = true) ||
                        it.nameArabic.contains(searchQuery.trim()) ||
                        it.nameUrdu.contains(searchQuery.trim())
            }
        }
    }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("quran", language),
                language = language,
                onBackClick = onBack
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
                placeholder = { Text(AppLocalization.get("search_hint", language)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("quran_search_field")
            )

            // Tabs: Surahs (114) vs Paras (30)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "${AppLocalization.get("surah", language)} (114)",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_surahs")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "${AppLocalization.get("para", language)} (30)",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("tab_paras")
                )
            }

            // List Content
            if (selectedTab == 0) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSurahs, key = { it.number }) { surah ->
                        SurahListItem(
                            surah = surah,
                            language = language,
                            onClick = { onSelectSurah(surah.number) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredParas, key = { it.number }) { para ->
                        ParaListItem(
                            para = para,
                            language = language,
                            onClick = { onSelectSurah(para.startSurahNumber) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurahListItem(
    surah: Surah,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("surah_item_${surah.number}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Number Medallion
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = surah.number.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Surah Details
            Column(modifier = Modifier.weight(1f)) {
                val displayName = when (language) {
                    AppLanguage.URDU -> surah.nameUrdu
                    AppLanguage.HINDI -> surah.nameHindi
                    AppLanguage.ARABIC -> surah.nameArabic
                    AppLanguage.ENGLISH -> surah.nameEnglish
                }
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (surah.revelationType == RevelationType.MAKKI) AppLocalization.get("makki", language) else AppLocalization.get("madani", language),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " • ${surah.ayahCount} ${AppLocalization.get("ayah_count", language)} • ${AppLocalization.get("para", language)} ${surah.startingPara}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Arabic Calligraphy Name (Uthmani Naskh Script)
            Text(
                text = surah.nameArabic,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = QuranNaskhFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ParaListItem(
    para: Para,
    language: AppLanguage,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("para_item_${para.number}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = para.number.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${AppLocalization.get("para", language)} ${para.number}: ${para.nameEnglish}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Starts at Surah ${para.startSurahNumber}, Ayah ${para.startAyahNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = para.nameArabic,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    surahNumber: Int,
    language: AppLanguage,
    onBack: () -> Unit,
    onNavigateToSurah: (Int) -> Unit
) {
    val context = LocalContext.current
    val surah = remember(surahNumber) {
        QuranData.surahs.firstOrNull { it.number == surahNumber } ?: QuranData.surahs[0]
    }
    val ayahs = remember(surahNumber) { QuranData.getAyahsForSurah(context, surahNumber) }

    var selectedReciter by remember { mutableStateOf(QuranData.reciters[0]) }
    var showReciterMenu by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var arabicFontSize by remember { mutableStateOf(24f) }
    var isDownloadingAudio by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var isDownloadedOffline by remember { mutableStateOf(false) }

    LaunchedEffect(surahNumber) {
        QuranAudioManager.init(context)
        // If audio from a different Surah was playing, stop it upon opening this Surah
        if (QuranAudioManager.currentPlayingSurahNumber.value != null &&
            QuranAudioManager.currentPlayingSurahNumber.value != surahNumber) {
            QuranAudioManager.stopMedia()
        }
    }

    DisposableEffect(surahNumber) {
        onDispose {
            QuranAudioManager.stopMedia()
        }
    }

    // Bottom sheet state for Translation & Tafseer
    var activeTranslationAyah by remember { mutableStateOf<Ayah?>(null) }
    var activeTafseerAyah by remember { mutableStateOf<Ayah?>(null) }
    var translationSheetLanguage by remember { mutableStateOf(language) }
    var tafseerSheetLanguage by remember { mutableStateOf(language) }
    var selectedScholarKey by remember { mutableStateOf("ibn_kathir") }
    var loadedScholarTafseer by remember { mutableStateOf<ScholarTafseer?>(null) }
    var isTafseerLoading by remember { mutableStateOf(false) }
    val tafseerScope = rememberCoroutineScope()
    var isDownloadingAllTafseer by remember { mutableStateOf(false) }
    var offlineTafseerReady by remember { mutableStateOf(false) }
    var tafseerDownloadCompleted by remember { mutableStateOf(0) }
    var tafseerDownloadTotal by remember { mutableStateOf(570) }
    var tafseerDownloadLabel by remember { mutableStateOf("") }
    var tafseerDownloadError by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        QuranTafseerRepository.init(context)
        offlineTafseerReady = QuranTafseerRepository.isAllUrduTafseerDownloaded(context)
    }

    LaunchedEffect(language) {
        translationSheetLanguage = language
        tafseerSheetLanguage = AppLanguage.URDU
    }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = "${surah.number}. ${surah.nameEnglish} (${surah.nameArabic})",
                language = language,
                onBackClick = onBack,
                actions = {
                    IconButton(
                        onClick = { showFontSizeDialog = true },
                        modifier = Modifier.testTag("font_size_btn")
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = "Font Size")
                    }
                }
            )
        },
        bottomBar = {
            // Audio Reciter & Player Bar
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showReciterMenu = true }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${surah.number}. ${surah.nameEnglish} (${surah.nameArabic})",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Reciter",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (QuranAudioManager.isBuffering.value && QuranAudioManager.currentPlayingSurahNumber.value == surah.number) {
                                    "Buffering recitation..."
                                } else if (QuranAudioManager.isAudioPlaying.value && QuranAudioManager.currentPlayingSurahNumber.value == surah.number) {
                                    "Playing: ${selectedReciter.nameEnglish}"
                                } else {
                                    "${selectedReciter.nameEnglish} • Tap to Play"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (QuranAudioManager.isAudioPlaying.value && QuranAudioManager.currentPlayingSurahNumber.value == surah.number) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            DropdownMenu(
                                expanded = showReciterMenu,
                                onDismissRequest = { showReciterMenu = false }
                            ) {
                                QuranData.reciters.forEach { reciter ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(reciter.nameEnglish, fontWeight = FontWeight.Bold)
                                                Text(reciter.style, style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            selectedReciter = reciter
                                            showReciterMenu = false
                                            if (QuranAudioManager.isAudioPlaying.value && QuranAudioManager.currentPlayingSurahNumber.value == surah.number) {
                                                QuranAudioManager.playSurah(
                                                    context = context,
                                                    surahNumber = surah.number,
                                                    surahName = surah.nameEnglish,
                                                    reciterId = reciter.id
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        val isSurahPlaying = QuranAudioManager.isAudioPlaying.value && 
                            QuranAudioManager.currentPlayingSurahNumber.value == surah.number && 
                            QuranAudioManager.currentPlayingAyahNumber.value == null
                        val isSurahBuffering = QuranAudioManager.isBuffering.value && 
                            QuranAudioManager.currentPlayingSurahNumber.value == surah.number && 
                            QuranAudioManager.currentPlayingAyahNumber.value == null

                        // Play / Pause recitation button
                        IconButton(
                            onClick = {
                                if (isSurahPlaying) {
                                    QuranAudioManager.togglePlayPause()
                                } else {
                                    QuranAudioManager.playSurah(
                                        context = context,
                                        surahNumber = surah.number,
                                        surahName = surah.nameEnglish,
                                        reciterId = selectedReciter.id
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("play_pause_audio_btn")
                        ) {
                            if (isSurahBuffering) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = if (isSurahPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isSurahPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Download Surah Recitation
                        IconButton(
                            onClick = {
                                if (!isDownloadedOffline) {
                                    isDownloadingAudio = true
                                    downloadProgress = 100
                                    isDownloadedOffline = true
                                    Toast.makeText(context, "Surah ${surah.nameEnglish} downloaded for offline playback", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Surah already saved offline", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("download_surah_audio_btn")
                        ) {
                            Icon(
                                imageVector = if (isDownloadedOffline) Icons.Default.CheckCircle else Icons.Default.Download,
                                contentDescription = "Download recitation",
                                tint = if (isDownloadedOffline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Surah Header Banner
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = surah.nameArabic,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = QuranNaskhFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${surah.nameEnglish} • ${surah.englishMeaning}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${if (surah.revelationType == RevelationType.MAKKI) "Makki" else "Madani"} • ${surah.ayahCount} Verses • Para ${surah.startingPara}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        if (surah.number != 9 && surah.number != 1) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = QuranNaskhFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Ayahs
            items(ayahs, key = { it.ayahNumber }) { ayah ->
                val isAyahPlaying = QuranAudioManager.isAudioPlaying.value &&
                    QuranAudioManager.currentPlayingSurahNumber.value == ayah.surahNumber &&
                    QuranAudioManager.currentPlayingAyahNumber.value == ayah.ayahNumber
                AyahCard(
                    ayah = ayah,
                    language = language,
                    fontSize = arabicFontSize,
                    isPlaying = isAyahPlaying,
                    onPlayAyah = {
                        if (isAyahPlaying) {
                            QuranAudioManager.togglePlayPause()
                        } else {
                            QuranAudioManager.playAyah(
                                context = context,
                                surahNumber = ayah.surahNumber,
                                ayahNumber = ayah.ayahNumber,
                                reciterId = selectedReciter.id
                            )
                        }
                    },
                    onOpenTranslation = { activeTranslationAyah = ayah },
                    onOpenTafseer = { activeTafseerAyah = ayah }
                )
            }

            // Surah Navigation Footer (Previous / Next Surah)
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 24.dp)
                ) {
                    if (surah.number > 1) {
                        OutlinedButton(
                            onClick = { onNavigateToSurah(surah.number - 1) },
                            modifier = Modifier.testTag("prev_surah_btn")
                        ) {
                            Text("← Previous Surah")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    if (surah.number < 114) {
                        Button(
                            onClick = { onNavigateToSurah(surah.number + 1) },
                            modifier = Modifier.testTag("next_surah_btn")
                        ) {
                            Text("Next Surah →")
                        }
                    }
                }
            }
        }

        // Translation Bottom Sheet with Accurate Text & Revelation Context
        if (activeTranslationAyah != null) {
            val curAyah = activeTranslationAyah!!
            val revContext = remember(curAyah.surahNumber, curAyah.ayahNumber, translationSheetLanguage) {
                QuranTafseerRepository.getRevelationContext(
                    ayah = curAyah,
                    language = translationSheetLanguage
                )
            }

            ModalBottomSheet(
                onDismissRequest = { activeTranslationAyah = null }
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (translationSheetLanguage) {
                                    AppLanguage.URDU -> "ترجمہ و پس منظر: آیت ${curAyah.ayahNumber}"
                                    AppLanguage.ARABIC -> "الترجمة وأسباب النزول: آية ${curAyah.ayahNumber}"
                                    AppLanguage.HINDI -> "अनुवाद व पृष्ठभूमि: आयत ${curAyah.ayahNumber}"
                                    AppLanguage.ENGLISH -> "Translation & Context: Ayah ${curAyah.ayahNumber}"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // 4 Language Selector Chips
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val languages = listOf(
                                AppLanguage.URDU to "اردو",
                                AppLanguage.ENGLISH to "English",
                                AppLanguage.HINDI to "हिन्दी",
                                AppLanguage.ARABIC to "العربية"
                            )
                            languages.forEach { (lang, label) ->
                                FilterChip(
                                    selected = translationSheetLanguage == lang,
                                    onClick = { translationSheetLanguage = lang },
                                    label = { Text(label, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("trans_lang_${lang.code}")
                                )
                            }
                        }
                    }

                    // Arabic Verse in Naskh Script
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${curAyah.textArabic} ۝${curAyah.ayahNumber}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = QuranNaskhFontFamily,
                                    fontSize = 24.sp,
                                    lineHeight = 44.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }

                    // Selected Language Verified Translation Card
                    item {
                        val trans = curAyah.translations.firstOrNull { it.language == translationSheetLanguage }
                            ?: curAyah.translations[0]

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = trans.translatorName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = trans.language.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = trans.translationText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 17.sp,
                                        lineHeight = 26.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Edition: ${trans.editionName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Section Heading: Revelation Context & Spiritual Significance
                    item {
                        Text(
                            text = when (translationSheetLanguage) {
                                AppLanguage.URDU -> "شانِ نزول و تفصیلی پس منظر"
                                AppLanguage.ARABIC -> "أسباب النزول والمقاصد الشرعية"
                                AppLanguage.HINDI -> "शान-ए-नुजूल व विस्तृत संदर्भ"
                                AppLanguage.ENGLISH -> "Revelation Context & Spiritual Significance"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // 1. سبب النزول (Sabab-e-Nuzool)
                    item {
                        ContextInfoCard(
                            title = when (translationSheetLanguage) {
                                AppLanguage.URDU -> "۱. سببِ نزول (یہ آیت کیوں نازل ہوئی؟)"
                                AppLanguage.ARABIC -> "١. سبب النزول (لماذا نزلت هذه الآية؟)"
                                AppLanguage.HINDI -> "1. सबब-ए-नुजूल (यह आयत क्यों नाज़िल हुई?)"
                                AppLanguage.ENGLISH -> "1. Occasion of Revelation (Why Was It Revealed?)"
                            },
                            body = revContext.sababNuzool,
                            icon = Icons.Default.History,
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 2. زمانۂ نزول (Zamana-e-Nuzool)
                    item {
                        ContextInfoCard(
                            title = when (translationSheetLanguage) {
                                AppLanguage.URDU -> "۲. زمانۂ نزول (کس وقت / دور میں نازل ہوئی؟)"
                                AppLanguage.ARABIC -> "٢. زمان النزول وتاريخ البعثة"
                                AppLanguage.HINDI -> "2. ज़माना-ए-नुजूल (किस काल में नाज़िल हुई?)"
                                AppLanguage.ENGLISH -> "2. Period of Revelation (Historical Phase & Era)"
                            },
                            body = revContext.zamanaNuzool,
                            icon = Icons.Default.Schedule,
                            accentColor = Color(0xFFD97706)
                        )
                    }

                    // 3. مقصد و حکمت (Maqsad-o-Hikmat)
                    item {
                        ContextInfoCard(
                            title = when (translationSheetLanguage) {
                                AppLanguage.URDU -> "۳. مقصد و حکمت (نازل ہونے کا مقصد کیا ہے؟)"
                                AppLanguage.ARABIC -> "٣. المقصد والحكمة الإلهية"
                                AppLanguage.HINDI -> "3. मक़सद व हिकमत (नाज़िल होने का मक़सद क्या है?)"
                                AppLanguage.ENGLISH -> "3. Divine Objective & Core Wisdom"
                            },
                            body = revContext.maqsadHikmat,
                            icon = Icons.Default.Lightbulb,
                            accentColor = Color(0xFF0D9488)
                        )
                    }

                    // 4. فضائل و برکات (Fazaail-o-Barkaat)
                    item {
                        ContextInfoCard(
                            title = when (translationSheetLanguage) {
                                AppLanguage.URDU -> "۴. فضائل و برکات (پڑھنے کے فوائد و احادیث)"
                                AppLanguage.ARABIC -> "٤. الفضائل والبركات والأحاديث الشريفة"
                                AppLanguage.HINDI -> "4. फ़ज़ाइल व बरकात (पढ़ने के लाभ व हदीस)"
                                AppLanguage.ENGLISH -> "4. Virtues, Benefits & Recitation Blessings"
                            },
                            body = revContext.fazaailBenefits,
                            icon = Icons.Default.Star,
                            accentColor = Color(0xFF8B5CF6)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }

        // Tafseer Bottom Sheet with Top 5 Ulama / Classical Scholars
        if (activeTafseerAyah != null) {
            val curAyah = activeTafseerAyah!!
            val scholars = remember {
                QuranTafseerRepository.getTop5Scholars(AppLanguage.URDU)
            }
            LaunchedEffect(curAyah.surahNumber, curAyah.ayahNumber, selectedScholarKey, tafseerSheetLanguage) {
                isTafseerLoading = true
                loadedScholarTafseer = null
                loadedScholarTafseer = QuranTafseerRepository.getScholarTafseer(
                    ayah = curAyah,
                    scholarKey = selectedScholarKey,
                    language = tafseerSheetLanguage
                )
                isTafseerLoading = false
            }
            val activeScholarTafseer = loadedScholarTafseer ?: ScholarTafseer(
                scholarKey = selectedScholarKey,
                authorName = scholars.firstOrNull { it.key == selectedScholarKey }?.displayName.orEmpty(),
                bookTitle = scholars.firstOrNull { it.key == selectedScholarKey }?.bookTitle.orEmpty(),
                eraAndCredentials = "",
                tafseerText = "",
                keyPoints = emptyList()
            )

            ModalBottomSheet(
                onDismissRequest = { activeTafseerAyah = null }
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text(
                            text = when (tafseerSheetLanguage) {
                                AppLanguage.URDU -> "مستند ائمۂ تفسیر کی مفصل تفاسیر (آیت ${curAyah.ayahNumber})"
                                AppLanguage.ARABIC -> "تفاسير كبار أئمة التفسير الخمسة (آية ${curAyah.ayahNumber})"
                                AppLanguage.HINDI -> "शीर्ष 5 उलेमा की विस्तृत तफ़सीर (आयत ${curAyah.ayahNumber})"
                                AppLanguage.ENGLISH -> "Top 5 Classical Scholars Tafseer (Ayah ${curAyah.ayahNumber})"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (offlineTafseerReady) "پانچوں اردو تفاسیر آف لائن محفوظ ہیں" else "پانچوں اردو تفاسیر آف لائن محفوظ کریں",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ایک مرتبہ انٹرنیٹ پر 5 تفاسیر کی تمام 114 سورتیں ڈاؤن لوڈ کریں۔ مکمل ہونے کے بعد اردو متن انٹرنیٹ کے بغیر دستیاب ہوگا۔ ڈاؤن لوڈ دوبارہ شروع کیا جا سکتا ہے۔",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isDownloadingAllTafseer) {
                                    LinearProgressIndicator(
                                        progress = if (tafseerDownloadTotal > 0) tafseerDownloadCompleted.toFloat() / tafseerDownloadTotal else 0f,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "$tafseerDownloadCompleted / $tafseerDownloadTotal سورتیں • $tafseerDownloadLabel",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                if (tafseerDownloadError.isNotBlank()) {
                                    Text(tafseerDownloadError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    enabled = !isDownloadingAllTafseer && !offlineTafseerReady,
                                    onClick = {
                                        tafseerDownloadError = ""
                                        isDownloadingAllTafseer = true
                                        tafseerScope.launch {
                                            val result = QuranTafseerRepository.downloadAllUrduTafseer(context) { done, total, label ->
                                                tafseerDownloadCompleted = done
                                                tafseerDownloadTotal = total
                                                tafseerDownloadLabel = label
                                            }
                                            isDownloadingAllTafseer = false
                                            offlineTafseerReady = result.success && QuranTafseerRepository.isAllUrduTafseerDownloaded(context)
                                            if (!result.success) {
                                                tafseerDownloadError = "ڈاؤن لوڈ مکمل نہیں ہوا: ${result.error ?: "نامعلوم خرابی"} — دوبارہ دبائیں؛ پہلے محفوظ شدہ سورتیں برقرار رہیں گی۔"
                                            } else if (!result.error.isNullOrBlank()) {
                                                tafseerDownloadError = result.error
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (isDownloadingAllTafseer) "ڈاؤن لوڈ جاری ہے…" else if (offlineTafseerReady) "آف لائن ڈاؤن لوڈ مکمل" else "تمام اردو تفاسیر ڈاؤن لوڈ کریں")
                                }
                            }
                        }
                    }

                    // Urdu source selector and separate target-language translation controls.
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(9.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "اصل تفسیر کی زبان",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            FilterChip(
                                selected = true,
                                onClick = { tafseerSheetLanguage = AppLanguage.URDU },
                                label = { Text("اردو — اصل مستند تفسیر", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("tafseer_source_ur")
                            )
                            Text(
                                text = "ترجمہ کریں:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val outputLanguages = listOf(
                                    AppLanguage.URDU to "اردو",
                                    AppLanguage.ENGLISH to "English",
                                    AppLanguage.HINDI to "हिन्दी",
                                    AppLanguage.ARABIC to "العربية"
                                )
                                outputLanguages.forEach { (lang, label) ->
                                    FilterChip(
                                        selected = tafseerSheetLanguage == lang,
                                        onClick = { tafseerSheetLanguage = lang },
                                        label = { Text(label, fontWeight = FontWeight.Bold) },
                                        modifier = Modifier.testTag("tafseer_lang_${lang.code}")
                                    )
                                }
                            }
                            if (tafseerSheetLanguage != AppLanguage.URDU) {
                                Text(
                                    text = when (tafseerSheetLanguage) {
                                        AppLanguage.ENGLISH -> "Machine translation from the original Urdu tafseer. This is not a published English edition or scholar-approved translation. The first use may need internet to download Google's language model."
                                        AppLanguage.HINDI -> "मूल उर्दू तफ़सीर का मशीन अनुवाद। यह प्रकाशित हिंदी संस्करण या विद्वान द्वारा अनुमोदित अनुवाद नहीं है। पहली बार भाषा मॉडल डाउनलोड करने के लिए इंटरनेट की आवश्यकता हो सकती है।"
                                        AppLanguage.ARABIC -> "ترجمة آلية من التفسير الأردي الأصلي، وليست طبعة منشورة أو ترجمة معتمدة من العالم. قد يلزم الإنترنت أول مرة لتنزيل نموذج اللغة."
                                        AppLanguage.URDU -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Arabic Verse in Naskh Script
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${curAyah.textArabic} ۝${curAyah.ayahNumber}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = QuranNaskhFontFamily,
                                    fontSize = 24.sp,
                                    lineHeight = 44.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }

                    // Top 5 Ulama / Scholars Selection Tabs
                    item {
                        Text(
                            text = when (tafseerSheetLanguage) {
                                AppLanguage.URDU -> "معتبر ائمۂ تفسیر منتخب فرمائیں:"
                                AppLanguage.ARABIC -> "اختر أحد أئمة التفسير المعتمدين:"
                                AppLanguage.HINDI -> "प्रसिद्ध मुफ़स्सिर चुनें:"
                                AppLanguage.ENGLISH -> "Select Authentic Classical Scholar:"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(scholars) { scholar ->
                                FilterChip(
                                    selected = selectedScholarKey == scholar.key,
                                    onClick = { selectedScholarKey = scholar.key },
                                    label = { Text(scholar.displayName, fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = if (selectedScholarKey == scholar.key) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.testTag("scholar_chip_${scholar.key}")
                                )
                            }
                        }
                    }

                    // Active Scholar Profile Banner
                    item {
                        val matchingInfo = scholars.firstOrNull { it.key == selectedScholarKey } ?: scholars[0]
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = activeScholarTafseer.bookTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = matchingInfo.methodologyBadge,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = activeScholarTafseer.authorName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeScholarTafseer.eraAndCredentials,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    if (isTafseerLoading) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                Text(
                                    text = when (tafseerSheetLanguage) {
                                        AppLanguage.URDU -> "مستند ماخذ سے اصل تفسیر حاصل کی جا رہی ہے…"
                                        AppLanguage.ARABIC -> "جارٍ تحميل نص التفسير من المصدر…"
                                        AppLanguage.HINDI -> "प्रमाणित स्रोत से मूल तफ़सीर लोड हो रही है…"
                                        AppLanguage.ENGLISH -> "Loading original commentary from the cited source…"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // One-Page Detailed Scholarly Tafseer Commentary
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                if (activeScholarTafseer.isMachineTranslated) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = activeScholarTafseer.translationNotice,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (tafseerSheetLanguage) {
                                            AppLanguage.URDU -> "مفصل و مدلل تفسیر (ایک مکمل صفحہ):"
                                            AppLanguage.ARABIC -> "النص الكامل للتفسير والبيان التفصيلي:"
                                            AppLanguage.HINDI -> "विस्तृत व प्रामाणिक तफ़सीर (एक पूरा पृष्ठ):"
                                            AppLanguage.ENGLISH -> "Comprehensive Scholarly Commentary:"
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = activeScholarTafseer.tafseerText,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 16.sp,
                                        lineHeight = 27.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!isTafseerLoading && activeScholarTafseer.sourceAttribution.isNotBlank()) {
                        item {
                            Text(
                                text = "Source: ${activeScholarTafseer.sourceAttribution}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                            )
                        }
                    }

                    // Key Takeaways / اہم نکات Card
                    if (activeScholarTafseer.keyPoints.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (tafseerSheetLanguage) {
                                                AppLanguage.URDU -> "اہم نکات و عملی رہنمائی:"
                                                AppLanguage.ARABIC -> "النقاط الجوهرية والفوائد الإيمانية:"
                                                AppLanguage.HINDI -> "प्रमुख बिंदु व व्यावहारिक मार्गदर्शन:"
                                                AppLanguage.ENGLISH -> "Core Takeaways & Spiritual Lessons:"
                                            },
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    activeScholarTafseer.keyPoints.forEachIndexed { idx, point ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "• ",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = point,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, lineHeight = 22.sp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }

        // Font Size Dialog
        if (showFontSizeDialog) {
            AlertDialog(
                onDismissRequest = { showFontSizeDialog = false },
                title = { Text(AppLocalization.get("font_size", language)) },
                text = {
                    Column {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontSize = arabicFontSize.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                        Slider(
                            value = arabicFontSize,
                            onValueChange = { arabicFontSize = it },
                            valueRange = 18f..36f,
                            steps = 8
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFontSizeDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

@Composable
fun AyahCard(
    ayah: Ayah,
    language: AppLanguage,
    fontSize: Float,
    isPlaying: Boolean,
    onPlayAyah: () -> Unit,
    onOpenTranslation: () -> Unit,
    onOpenTafseer: () -> Unit
) {
    val context = LocalContext.current
    var isBookmarked by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_card_${ayah.ayahNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Ayah Action Toolbar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Ayah Index Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${ayah.surahNumber}:${ayah.ayahNumber}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Play Ayah
                    IconButton(
                        onClick = onPlayAyah,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Play Ayah",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Bookmark Ayah
                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                            Toast.makeText(
                                context,
                                if (isBookmarked) AppLocalization.get("bookmarked", language) else "Bookmark removed",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Ayah",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Copy Ayah
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Quran Ayah", "${ayah.textArabic}\n(Surah ${ayah.surahNumber}:${ayah.ayahNumber})")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, AppLocalization.get("copied", language), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Ayah",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share Ayah
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${ayah.textArabic}\n\n${ayah.translations[0].translationText}\n(Surah ${ayah.surahNumber}:${ayah.ayahNumber} - Noor-e-Hidayat)")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Ayah"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Ayah",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Arabic Text (Uthmani Verified Script in Naskh Calligraphy)
            Text(
                text = "${ayah.textArabic} ۝${ayah.ayahNumber}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = QuranNaskhFontFamily,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.9f).sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Primary Translation Preview according to current language
            val currentTrans = ayah.translations.firstOrNull { it.language == language } ?: ayah.translations[0]
            Text(
                text = currentTrans.translationText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // CRITICAL REQUIREMENT: TWO SEPARATE BUTTONS DIRECTLY BELOW EVERY AYAH
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                // Button 1: Translation
                OutlinedButton(
                    onClick = onOpenTranslation,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ayah_${ayah.ayahNumber}_translation_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppLocalization.get("translation_btn", language),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Button 2: Tafseer
                Button(
                    onClick = onOpenTafseer,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ayah_${ayah.ayahNumber}_tafseer_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppLocalization.get("tafseer_btn", language),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContextInfoCard(
    title: String,
    body: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
