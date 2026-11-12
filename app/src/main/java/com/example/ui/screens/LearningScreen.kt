package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.MediaAndLearningData
import com.example.data.repository.QuranAudioManager
import com.example.data.repository.QuranLearningRepository
import com.example.localization.AppLocalization
import com.example.ui.components.*

@Composable
fun LearningScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // In-Academy 4-Language Selection: Arabic, Urdu, English, Hindi
    var academyLanguage by remember { mutableStateOf(language) }

    // Active Learning Module (10 Academy Modules)
    var activeModule by remember { mutableStateOf(LearningModuleType.NOORANI_QAIDA) }

    // Selected Noorani Qaida Lesson index
    var selectedQaidaLessonIndex by remember { mutableIntStateOf(0) }

    // Selected Writing Exercise Letter
    var selectedWritingLetter by remember { mutableStateOf(QuranLearningRepository.writingExercises.first()) }

    // Track student progress with persistence
    var progressRefreshTrigger by remember { mutableIntStateOf(0) }
    val progress = remember(progressRefreshTrigger) {
        QuranLearningRepository.getProgress(context)
    }

    LaunchedEffect(Unit) {
        QuranAudioManager.init(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            QuranAudioManager.stopMedia()
        }
    }

    val isRtl = academyLanguage.isRtl

    CompositionLocalProvider(LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        Scaffold(
            topBar = {
                // Upgraded Quran Learning Academy Top Bar with Multilingual Selector
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.testTag("academy_back_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = when (academyLanguage) {
                                            AppLanguage.ARABIC -> "أكاديمية تعليم القرآن"
                                            AppLanguage.URDU -> "قرآن لرننگ اکیڈمی"
                                            AppLanguage.HINDI -> "क़ुरआन लर्निंग एकेडमी"
                                            else -> "Quran Learning Academy"
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = when (academyLanguage) {
                                            AppLanguage.ARABIC -> "تجويد • قراءة • حفظ • مفردات"
                                            AppLanguage.URDU -> "قاعدہ • تجوید • حفظ • مفردات • ترجمہ"
                                            AppLanguage.HINDI -> "क़ाइदा • तजवीद • हिफ़्ज़ • शब्दावली"
                                            else -> "Qaida • Tajweed • Reading • Hifz"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 4-Language Quick Selector Dropdown / Segment
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    AppLanguage.values().forEach { lang ->
                                        val isSel = lang == academyLanguage
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                                .clickable { academyLanguage = lang }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when (lang) {
                                                    AppLanguage.ENGLISH -> "EN"
                                                    AppLanguage.URDU -> "اردو"
                                                    AppLanguage.ARABIC -> "عربي"
                                                    AppLanguage.HINDI -> "हिन्दी"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // 10 Academy Learning Module Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = activeModule.ordinal,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LearningModuleType.values().forEach { mod ->
                        val isSelected = activeModule == mod
                        Tab(
                            selected = isSelected,
                            onClick = { activeModule = mod },
                            text = {
                                Text(
                                    text = when (academyLanguage) {
                                        AppLanguage.URDU -> mod.titleUr
                                        AppLanguage.ARABIC -> mod.titleAr
                                        AppLanguage.HINDI -> mod.titleHi
                                        else -> mod.titleEn
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_learning_${mod.id}")
                        )
                    }
                }

                // Main Academy Learning View
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Daily Learning Progress Dashboard Card
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("academy_progress_dashboard")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (academyLanguage) {
                                                AppLanguage.ARABIC -> "لوحة تقدم الطالب"
                                                AppLanguage.URDU -> "آپ کی تعلیمی پیش رفت"
                                                AppLanguage.HINDI -> "आपकी शिक्षण प्रगति"
                                                else -> "Student Learning Dashboard"
                                            },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${progress.completedLessonIds.size} Lessons Done • ${progress.totalQuizzesPassed} Quizzes Passed • ${progress.memorizedSurahNumbers.size}/114 Surahs",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            text = "🔥 ${progress.currentStreakDays}d Streak",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Overall Progress Bar
                                val progressRatio = ((progress.completedLessonIds.size + progress.totalQuizzesPassed) / 25f).coerceIn(0.05f, 1f)
                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Continue Learning Action
                                Surface(
                                    onClick = {
                                        // Resume last active module or default to Qaida
                                        activeModule = LearningModuleType.values().firstOrNull { it.id == progress.lastActiveModuleId }
                                            ?: LearningModuleType.NOORANI_QAIDA
                                        Toast.makeText(context, "Resumed: ${progress.lastActiveLessonTitle}", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircleFilled,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Continue: ${progress.lastActiveLessonTitle}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Content Switcher for Active Module
                    when (activeModule) {
                        LearningModuleType.NOORANI_QAIDA -> {
                            // Lesson Horizontal Selector
                            item {
                                Text(
                                    text = "Select Qaida Lesson (اختر الدرس):",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(QuranLearningRepository.qaidaLessons) { idx, ql ->
                                        val isSel = idx == selectedQaidaLessonIndex
                                        val isDone = progress.completedLessonIds.contains(ql.id)
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedQaidaLessonIndex = idx },
                                            label = {
                                                Text(
                                                    text = "L${ql.lessonNumber}${if (isDone) " ✓" else ""}",
                                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }

                            // Active Qaida Lesson Content
                            val curLesson = QuranLearningRepository.qaidaLessons[selectedQaidaLessonIndex]

                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = when (academyLanguage) {
                                                    AppLanguage.URDU -> curLesson.titleUr
                                                    AppLanguage.ARABIC -> curLesson.titleAr
                                                    AppLanguage.HINDI -> curLesson.titleHi
                                                    else -> curLesson.titleEn
                                                },
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Button(
                                                onClick = {
                                                    QuranLearningRepository.markLessonCompleted(
                                                        context,
                                                        curLesson.id,
                                                        curLesson.titleEn,
                                                        "qaida"
                                                    )
                                                    progressRefreshTrigger++
                                                    Toast.makeText(context, "Lesson ${curLesson.lessonNumber} marked completed! 🎉", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Done ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when (academyLanguage) {
                                                AppLanguage.URDU -> curLesson.descriptionUr
                                                AppLanguage.ARABIC -> curLesson.descriptionAr
                                                AppLanguage.HINDI -> curLesson.descriptionHi
                                                else -> curLesson.descriptionEn
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Items in active Qaida lesson
                            items(curLesson.items) { qItem ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            QuranAudioManager.speakArabic(context, qItem.arabic, qItem.transliteration)
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            ) {
                                                Text(
                                                    text = qItem.arabic,
                                                    style = MaterialTheme.typography.headlineMedium.copy(
                                                        fontSize = 28.sp,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = qItem.transliteration,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                val note = when (academyLanguage) {
                                                    AppLanguage.URDU -> qItem.noteUr
                                                    AppLanguage.ARABIC -> qItem.noteAr
                                                    AppLanguage.HINDI -> qItem.noteHi
                                                    else -> qItem.noteEn
                                                }
                                                if (note.isNotBlank()) {
                                                    Text(
                                                        text = note,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                QuranAudioManager.speakArabic(context, qItem.arabic, qItem.transliteration)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Pronounce",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        LearningModuleType.TAJWEED -> {
                            item {
                                Text(
                                    text = "Tajweed Rules & Makharij (أحكام التجويد):",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(QuranLearningRepository.advancedTajweedRules) { rule ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = when (academyLanguage) {
                                                AppLanguage.URDU -> rule.titleUr
                                                AppLanguage.ARABIC -> rule.titleAr
                                                AppLanguage.HINDI -> rule.titleHi
                                                else -> rule.titleEn
                                            },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val exp = when (academyLanguage) {
                                            AppLanguage.URDU -> rule.explanationUr
                                            AppLanguage.ARABIC -> rule.explanationAr
                                            AppLanguage.HINDI -> rule.explanationHi
                                            else -> rule.explanationEn
                                        }
                                        Text(
                                            text = exp,
                                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Quranic Practice Examples (أمثلة قرآنية):",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        rule.examples.forEach { eg ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.padding(10.dp)
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = eg.arabicText,
                                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Text(
                                                            text = eg.ruleApplicationNote,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = eg.surahReference,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        IconButton(
                                                            onClick = {
                                                                QuranAudioManager.speakArabic(context, eg.arabicText, eg.surahReference)
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.VolumeUp,
                                                                contentDescription = "Pronounce example",
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
                            }
                        }

                        LearningModuleType.QURAN_READING -> {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Surah Al-Fatihah Reading Practice",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Listen, repeat, and follow along with Mishary Rashid Alafasy audio.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        AudioPronunciationController(surahNumber = 1, ayahNumber = 1)
                                    }
                                }
                            }

                            // Ayah by Ayah reading practice
                            items(QuranLearningRepository.wordByWordSurahFatihah) { wAyah ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = wAyah.fullArabic,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontSize = 24.sp,
                                                lineHeight = 38.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AudioPronunciationController(surahNumber = wAyah.surahNumber, ayahNumber = wAyah.ayahNumber)
                                    }
                                }
                            }
                        }

                        LearningModuleType.QURANIC_ARABIC -> {
                            item {
                                Text(
                                    text = "80% High-Frequency Quranic Words (مفردات القرآن):",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap any flashcard to flip and reveal its meaning and Quranic example.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            items(QuranLearningRepository.quranicVocabulary) { qWord ->
                                val isLearned = progress.learnedWordIds.contains(qWord.id)
                                QuranicWordFlashcard(
                                    word = qWord,
                                    language = academyLanguage,
                                    isLearned = isLearned,
                                    onToggleLearned = {
                                        QuranLearningRepository.toggleLearnedWord(context, qWord.id)
                                        progressRefreshTrigger++
                                    }
                                )
                            }
                        }

                        LearningModuleType.TRANSLATION -> {
                            item {
                                Text(
                                    text = "Authentic Word-by-Word Translation (ترجمہ و فہم):",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Surah Al-Fatihah word breakdown with verified authentic meanings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            items(QuranLearningRepository.wordByWordSurahFatihah) { wAyah ->
                                WordByWordAyahCard(ayahData = wAyah, language = academyLanguage)
                            }
                        }

                        LearningModuleType.HIFZ -> {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Hifz Memorization & Daily Revision (حفظ القرآن)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Memorize short Surahs and maintain your daily Sabqi & Dhor revision streak.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        AudioPronunciationController(surahNumber = 112, ayahNumber = 1)
                                    }
                                }
                            }

                            // Memorization Checklist
                            val hifzList = listOf(
                                Pair(1, "Surah Al-Fatihah (الفاتحة) • 7 Ayahs"),
                                Pair(114, "Surah An-Nas (الناس) • 6 Ayahs"),
                                Pair(113, "Surah Al-Falaq (الفلق) • 5 Ayahs"),
                                Pair(112, "Surah Al-Ikhlas (الإخلاص) • 4 Ayahs"),
                                Pair(111, "Surah Al-Masad (المسد) • 5 Ayahs"),
                                Pair(110, "Surah An-Nasr (النصر) • 3 Ayahs"),
                                Pair(109, "Surah Al-Kafirun (الكافرون) • 6 Ayahs"),
                                Pair(108, "Surah Al-Kawthar (الكوثر) • 3 Ayahs"),
                                Pair(103, "Surah Al-Asr (العصر) • 3 Ayahs")
                            )

                            items(hifzList) { (sNum, sTitle) ->
                                val isMem = progress.memorizedSurahNumbers.contains(sNum)
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sTitle,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (isMem) "Memorized ✓ (In Revision Schedule)" else "In Progress",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isMem) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        val isSurahPlaying = QuranAudioManager.isAudioPlaying.value && QuranAudioManager.currentPlayingSurahNumber.value == sNum && QuranAudioManager.currentPlayingAyahNumber.value == null
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    if (isSurahPlaying) {
                                                        QuranAudioManager.togglePlayPause()
                                                    } else {
                                                        QuranAudioManager.playSurah(context, sNum, sTitle)
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSurahPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                                    contentDescription = "Play Surah Recitation",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Checkbox(
                                                checked = isMem,
                                                onCheckedChange = {
                                                    QuranLearningRepository.toggleMemorizedSurah(context, sNum)
                                                    progressRefreshTrigger++
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        LearningModuleType.ARABIC_WRITING -> {
                            item {
                                Text(
                                    text = "Select Letter to Trace & Write:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(QuranLearningRepository.writingExercises) { (letter, hint) ->
                                        val isSel = letter == selectedWritingLetter.first
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedWritingLetter = Pair(letter, hint) },
                                            label = { Text(letter, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }

                            item {
                                ArabicWritingPad(
                                    targetLetterOrWord = selectedWritingLetter.first,
                                    guidanceHint = selectedWritingLetter.second
                                )
                            }
                        }

                        LearningModuleType.PRONUNCIATION -> {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Listening & Pronunciation (صوتیات و مخارج)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Master authentic recitation with repeated loops and slow playback (0.75x).",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        AudioPronunciationController(surahNumber = 1, ayahNumber = 7)
                                    }
                                }
                            }

                            // 28 Letters Pronunciation Grid
                            items(MediaAndLearningData.arabicAlphabet) { letter ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            QuranAudioManager.speakArabic(context, letter.letter, letter.nameTranslit)
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = letter.letter,
                                                style = MaterialTheme.typography.headlineMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 28.sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "${letter.nameArabic} (${letter.nameTranslit})",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                )
                                                val makhraj = when (academyLanguage) {
                                                    AppLanguage.URDU -> letter.makhrajUr
                                                    AppLanguage.HINDI -> letter.makhrajHi
                                                    else -> letter.makhrajEn
                                                }
                                                Text(
                                                    text = makhraj,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                QuranAudioManager.speakArabic(context, letter.letter, letter.nameTranslit)
                                            }
                                        ) {
                                            Icon(Icons.Default.VolumeUp, contentDescription = "Pronounce letter", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        LearningModuleType.UNDERSTANDING -> {
                            item {
                                Text(
                                    text = "Surah Exegesis, Asbab-e-Nuzool & Themes (تدبر و اسباب نزول):",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            items(QuranLearningRepository.surahIntroductions) { exegesis ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = exegesis.nameEnglish,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = exegesis.revelationPlace,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "Asbab-e-Nuzool (Occasion of Revelation):",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        val asbab = when (academyLanguage) {
                                            AppLanguage.URDU -> exegesis.asbabNuzoolUr
                                            AppLanguage.ARABIC -> exegesis.asbabNuzoolAr
                                            AppLanguage.HINDI -> exegesis.asbabNuzoolHi
                                            else -> exegesis.asbabNuzoolEn
                                        }
                                        Text(
                                            text = asbab,
                                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "Source Reference: ${exegesis.sourceAttribution}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        LearningModuleType.ASSESSMENTS -> {
                            item {
                                Text(
                                    text = "Academy Tests & Assessments (امتحانات و ٹیسٹ):",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Complete interactive quizzes to test your knowledge and track your mastery.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            items(QuranLearningRepository.academyQuizzes) { quiz ->
                                InteractiveQuizCard(
                                    quiz = quiz,
                                    language = academyLanguage,
                                    onQuizPassed = {
                                        QuranLearningRepository.recordQuizPass(context)
                                        progressRefreshTrigger++
                                        Toast.makeText(context, "Quiz Passed! Score recorded ✓", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
