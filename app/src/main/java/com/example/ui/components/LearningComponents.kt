package com.example.ui.components

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.QuranAudioManager
import com.example.data.repository.QuranLearningRepository
import com.example.ui.theme.QuranNaskhFontFamily

// Line path data class for touch drawing on Canvas
data class DrawingLine(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

/**
 * Interactive Touch-Based Arabic Letter & Word Writing Pad
 */
@Composable
fun ArabicWritingPad(
    targetLetterOrWord: String,
    guidanceHint: String,
    modifier: Modifier = Modifier
) {
    val lines = remember { mutableStateListOf<DrawingLine>() }
    var currentLine by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color(0xFF10B981)) }
    var strokeWidth by remember { mutableFloatStateOf(14f) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with target and instructions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Touch-Based Writing Practice (مشقِ کتابت)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = guidanceHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        lines.clear()
                        currentLine = emptyList()
                    },
                    modifier = Modifier.testTag("clear_writing_canvas_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Clear Canvas",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Tracing Canvas with watermark guideline
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .pointerInput(selectedColor, strokeWidth) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentLine = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentLine = currentLine + change.position
                            },
                            onDragEnd = {
                                if (currentLine.isNotEmpty()) {
                                    lines.add(DrawingLine(currentLine, selectedColor, strokeWidth))
                                    currentLine = emptyList()
                                }
                            }
                        )
                    }
                    .testTag("arabic_tracing_canvas")
            ) {
                // Background Guide Letter Watermark (Original Arabic Script with Naskh Calligraphy)
                Text(
                    text = targetLetterOrWord,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = QuranNaskhFontFamily,
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    textAlign = TextAlign.Center
                )

                // Foreground Canvas Rendering Touch Strokes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    lines.forEach { line ->
                        if (line.points.size > 1) {
                            val path = Path().apply {
                                moveTo(line.points.first().x, line.points.first().y)
                                for (i in 1 until line.points.size) {
                                    lineTo(line.points[i].x, line.points[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = line.color,
                                style = Stroke(
                                    width = line.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (currentLine.size > 1) {
                        val path = Path().apply {
                            moveTo(currentLine.first().x, currentLine.first().y)
                            for (i in 1 until currentLine.size) {
                                lineTo(currentLine[i].x, currentLine[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = selectedColor,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Palette & Size Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val colors = listOf(
                        Color(0xFF10B981), // Emerald
                        Color(0xFFD97706), // Gold
                        Color(0xFF2563EB), // Sapphire
                        Color(0xFFDC2626)  // Crimson
                    )
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { selectedColor = c }
                                .then(
                                    if (selectedColor == c) Modifier.border(2.dp, Color.White, CircleShape) else Modifier
                                )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { strokeWidth = 10f },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Fine", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { strokeWidth = 18f },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Medium", fontSize = 11.sp)
                    }
                    FilledTonalButton(
                        onClick = { strokeWidth = 26f },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Bold", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/**
 * Interactive Flashcard for Quranic Words & Vocabulary
 */
@Composable
fun QuranicWordFlashcard(
    word: QuranicWord,
    language: AppLanguage,
    isLearned: Boolean,
    onToggleLearned: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFlipped by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { isFlipped = !isFlipped }
            .testTag("flashcard_${word.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${word.category} • ${word.frequencyInQuran}x in Quran",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            QuranAudioManager.speakArabic(context, word.arabic, word.transliteration)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Pronounce",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Checkbox(
                        checked = isLearned,
                        onCheckedChange = { onToggleLearned() },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arabic Word in authentic script
            Text(
                text = word.arabic,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp
                ),
                color = if (isFlipped) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = word.transliteration,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Translation in the active language
            val meaningText = when (language) {
                AppLanguage.URDU -> word.meaningUr
                AppLanguage.ARABIC -> word.meaningAr
                AppLanguage.HINDI -> word.meaningHi
                else -> word.meaningEn
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = meaningText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isFlipped) "Tap to hide details" else "Tap to flip",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded Usage Example when flipped
            AnimatedVisibility(visible = isFlipped) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Example in Quran: ${word.exampleAyah}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Reference: ${word.exampleSurahRef}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Word-by-Word Quran Ayah Display Component
 */
@Composable
fun WordByWordAyahCard(
    ayahData: WordByWordAyah,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Surah & Ayah badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Ayah ${ayahData.ayahNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Full Arabic Text in Authentic Script (Naskh Calligraphy)
            Text(
                text = ayahData.fullArabic,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = QuranNaskhFontFamily,
                    fontSize = 28.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Word-by-Word Analysis (تحلیل الکلمات):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row of Word Boxes
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ayahData.words.forEach { wp ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = wp.arabic,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = wp.translit,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val wordMeaning = when (language) {
                                AppLanguage.URDU -> wp.meaningUr
                                AppLanguage.ARABIC -> wp.meaningAr
                                AppLanguage.HINDI -> wp.meaningHi
                                else -> wp.meaningEn
                            }
                            Text(
                                text = wordMeaning,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Full Translation in Selected Language
            val fullTrans = when (language) {
                AppLanguage.URDU -> ayahData.translationUr
                AppLanguage.HINDI -> ayahData.translationHi
                else -> ayahData.translationEn
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = fullTrans,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

/**
 * Audio Recitation and Repeat Controller Bar
 */
@Composable
fun AudioPronunciationController(
    surahNumber: Int,
    ayahNumber: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var repeatTarget by remember { mutableIntStateOf(1) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    val isPlaying = QuranAudioManager.isAudioPlaying.value &&
        QuranAudioManager.currentPlayingSurahNumber.value == surahNumber &&
        QuranAudioManager.currentPlayingAyahNumber.value == ayahNumber
    val isBuffering = QuranAudioManager.isBuffering.value &&
        QuranAudioManager.currentPlayingSurahNumber.value == surahNumber &&
        QuranAudioManager.currentPlayingAyahNumber.value == ayahNumber
    val currentLoop = QuranAudioManager.currentLoopCount.intValue

    DisposableEffect(surahNumber, ayahNumber) {
        onDispose {
            if (QuranAudioManager.currentPlayingSurahNumber.value == surahNumber &&
                QuranAudioManager.currentPlayingAyahNumber.value == ayahNumber) {
                QuranAudioManager.stopMedia()
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Play / Stop Button
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (isPlaying || isBuffering) {
                            QuranAudioManager.stopMedia()
                        } else {
                            QuranAudioManager.playAyah(
                                context = context,
                                surahNumber = surahNumber,
                                ayahNumber = ayahNumber,
                                speed = playbackSpeed,
                                repeatTarget = repeatTarget
                            )
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    if (isBuffering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = if (isBuffering) "Connecting Recitation..." else if (isPlaying) "Playing Recitation..." else "Listen & Pronounce",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Loop: ${if (repeatTarget == Int.MAX_VALUE) "Continuous" else "${currentLoop}/${repeatTarget}x"} • ${playbackSpeed}x Speed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Repeat and Speed Toggles
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Repeat counter toggle: 1x -> 3x -> 5x
                FilledTonalButton(
                    onClick = {
                        repeatTarget = when (repeatTarget) {
                            1 -> 3
                            3 -> 5
                            5 -> 10
                            else -> 1
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("${repeatTarget}x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Speed toggle: 0.75x (Slow Tajweed practice) -> 1.0x (Standard)
                FilledTonalButton(
                    onClick = {
                        playbackSpeed = if (playbackSpeed == 1.0f) 0.75f else 1.0f
                        if (isPlaying) {
                            QuranAudioManager.playAyah(
                                context = context,
                                surahNumber = surahNumber,
                                ayahNumber = ayahNumber,
                                speed = playbackSpeed,
                                repeatTarget = repeatTarget
                            )
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(if (playbackSpeed == 0.75f) "Slow (0.75x)" else "Normal", fontSize = 11.sp)
                }
            }
        }
    }
}

/**
 * Interactive Academy Assessment Quiz Card with Instant Quadrilingual Explanation
 */
@Composable
fun InteractiveQuizCard(
    quiz: AcademyQuizQuestion,
    language: AppLanguage,
    onQuizPassed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }

    val questionText = when (language) {
        AppLanguage.URDU -> quiz.questionUr
        AppLanguage.ARABIC -> quiz.questionAr
        AppLanguage.HINDI -> quiz.questionHi
        else -> quiz.questionEn
    }

    val options = when (language) {
        AppLanguage.URDU -> quiz.optionsUr
        AppLanguage.ARABIC -> quiz.optionsAr
        AppLanguage.HINDI -> quiz.optionsHi
        else -> quiz.optionsEn
    }

    val explanationText = when (language) {
        AppLanguage.URDU -> quiz.explanationUr
        AppLanguage.ARABIC -> quiz.explanationAr
        AppLanguage.HINDI -> quiz.explanationHi
        else -> quiz.explanationEn
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = quiz.category,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Arabic Prompt if available
            if (!quiz.arabicPrompt.isNullOrBlank()) {
                Text(
                    text = quiz.arabicPrompt,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            // Question
            Text(
                text = questionText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Options
            options.forEachIndexed { idx, opt ->
                val isSelected = selectedOptionIndex == idx
                val isCorrect = idx == quiz.correctIndex

                val optionBgColor by animateColorAsState(
                    targetValue = when {
                        !isAnswerSubmitted && isSelected -> MaterialTheme.colorScheme.primaryContainer
                        isAnswerSubmitted && isCorrect -> Color(0xFF10B981).copy(alpha = 0.25f)
                        isAnswerSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                    label = "opt_bg"
                )

                Surface(
                    onClick = {
                        if (!isAnswerSubmitted) {
                            selectedOptionIndex = idx
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = optionBgColor,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isAnswerSubmitted && isCorrect -> Color(0xFF10B981)
                            isAnswerSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> Color.Transparent
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${('A' + idx)}. ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAnswerSubmitted) {
                            if (isCorrect) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF10B981))
                            } else if (isSelected) {
                                Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Submit or Explanation
            if (!isAnswerSubmitted) {
                Button(
                    onClick = {
                        if (selectedOptionIndex != null) {
                            isAnswerSubmitted = true
                            if (selectedOptionIndex == quiz.correctIndex) {
                                onQuizPassed()
                            }
                        }
                    },
                    enabled = selectedOptionIndex != null,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check Answer", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedOptionIndex == quiz.correctIndex) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (selectedOptionIndex == quiz.correctIndex) "✓ Excellent! Correct Answer." else "Notice: The correct answer is option ${('A' + quiz.correctIndex)}.",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedOptionIndex == quiz.correctIndex) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = explanationText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
