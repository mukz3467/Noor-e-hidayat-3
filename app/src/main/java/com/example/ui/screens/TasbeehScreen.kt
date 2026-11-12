package com.example.ui.screens

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.data.model.TasbeehDhikr
import com.example.data.model.TasbeehHistoryItem
import com.example.data.repository.TasbeehRepository
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.QuranNaskhFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { TasbeehRepository(context) }

    val currentCount by repository.currentCount.collectAsStateWithLifecycle()
    val currentTarget by repository.currentTarget.collectAsStateWithLifecycle()
    val currentRounds by repository.currentRounds.collectAsStateWithLifecycle()
    val selectedDhikrId by repository.selectedDhikrId.collectAsStateWithLifecycle()
    val soundEnabled by repository.soundEnabled.collectAsStateWithLifecycle()
    val vibrationEnabled by repository.vibrationEnabled.collectAsStateWithLifecycle()
    val keepScreenOn by repository.keepScreenOn.collectAsStateWithLifecycle()
    val todayTotal by repository.todayTotal.collectAsStateWithLifecycle()
    val todayCompletedTargets by repository.todayCompletedTargets.collectAsStateWithLifecycle()
    val allTimeTotal by repository.allTimeTotal.collectAsStateWithLifecycle()
    val history by repository.history.collectAsStateWithLifecycle()
    val dhikrBreakdown by repository.dhikrBreakdown.collectAsStateWithLifecycle()
    val customDhikrs by repository.customDhikrs.collectAsStateWithLifecycle()

    val allDhikrs = remember(customDhikrs) { repository.getAllDhikrs() }
    val activeDhikr = remember(selectedDhikrId, customDhikrs) { repository.getSelectedDhikr() }

    // Dialog & Sheet States
    var showResetDialog by remember { mutableStateOf(false) }
    var showTargetReachedDialog by remember { mutableStateOf(false) }
    var showDhikrPickerSheet by remember { mutableStateOf(false) }
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var showCustomDhikrDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showStatsSheet by remember { mutableStateOf(false) }
    var itemToDeleteHistory by remember { mutableStateOf<TasbeehHistoryItem?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    // Tap Animation & Accidental Double-Tap Debouncer
    var buttonScale by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = buttonScale,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 800f),
        label = "tasbeeh_scale"
    )
    var lastTapTimestamp by remember { mutableLongStateOf(0L) }

    // Screen Awake (Keep Screen On)
    val view = LocalView.current
    DisposableEffect(keepScreenOn) {
        view.keepScreenOn = keepScreenOn
        onDispose {
            view.keepScreenOn = false
        }
    }

    // Audio & Haptic Feedback Helpers
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }

    fun playFeedback(isTargetCompleted: Boolean = false) {
        // Sound
        if (soundEnabled) {
            try {
                audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
            } catch (_: Exception) {}
        }

        // Vibration
        if (vibrationEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    val vibrator = vibratorManager?.defaultVibrator
                    if (isTargetCompleted) {
                        vibrator?.vibrate(
                            VibrationEffect.createWaveform(longArrayOf(0, 70, 70, 100), -1)
                        )
                    } else {
                        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (isTargetCompleted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 70, 70, 100), -1))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(120)
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(35)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Counting Action (One Tap increases by 1)
    fun onCountTap() {
        val now = System.currentTimeMillis()
        if (now - lastTapTimestamp < 65) return // Prevent accidental double taps
        lastTapTimestamp = now

        buttonScale = 0.94f
        val reached = repository.increment()
        playFeedback(isTargetCompleted = reached)
        buttonScale = 1f

        if (reached) {
            showTargetReachedDialog = true
        }
    }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("tasbeeh", language),
                language = language,
                onBackClick = onBack,
                actions = {
                    // Sound Toggle
                    IconButton(
                        onClick = { repository.toggleSound() },
                        modifier = Modifier.testTag("tasbeeh_sound_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound",
                            tint = if (soundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    // Vibration Toggle
                    IconButton(
                        onClick = { repository.toggleVibration() },
                        modifier = Modifier.testTag("tasbeeh_vibration_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (vibrationEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                            contentDescription = "Toggle Vibration",
                            tint = if (vibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    // Statistics Button
                    IconButton(
                        onClick = { showStatsSheet = true },
                        modifier = Modifier.testTag("tasbeeh_stats_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // History Button
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("tasbeeh_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Reset Button
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("reset_tasbeeh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Counter",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Selected Zikr Display Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDhikrPickerSheet = true }
                    .testTag("tasbeeh_dhikr_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Arabic Zikr Text in Verified Naskh Script
                    Text(
                        text = activeDhikr.arabic,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = QuranNaskhFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (activeDhikr.arabic.length > 40) 22.sp else 30.sp,
                            lineHeight = if (activeDhikr.arabic.length > 40) 36.sp else 44.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = activeDhikr.translit,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val meaningText = when (language) {
                        AppLanguage.URDU -> activeDhikr.meaningUrdu
                        AppLanguage.HINDI -> activeDhikr.meaningHindi
                        AppLanguage.ARABIC -> activeDhikr.arabic
                        AppLanguage.ENGLISH -> activeDhikr.meaningEn
                    }
                    Text(
                        text = meaningText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Change Zikr Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { showDhikrPickerSheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppLocalization.get("change_dhikr", language),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Target, Round & Today Stats Pill Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Target Pill (Clickable to change target)
                Surface(
                    onClick = { showCustomTargetDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("tasbeeh_target_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${AppLocalization.get("target", language)}: $currentTarget",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Round Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("tasbeeh_rounds_pill")
                ) {
                    Text(
                        text = "${AppLocalization.get("rounds", language)}: ${currentRounds + 1}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Today's Total Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("tasbeeh_today_pill")
                ) {
                    Text(
                        text = "${AppLocalization.get("today_total", language)}: $todayTotal",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. Central Large Circular Digital Counter
            val progressFraction = remember(currentCount, currentTarget) {
                if (currentTarget > 0) {
                    (currentCount.toFloat() / currentTarget.toFloat()).coerceIn(0f, 1f)
                } else 0f
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
                    .scale(animatedScale)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 130.dp, color = MaterialTheme.colorScheme.primary),
                        onClick = { onCountTap() }
                    )
                    .testTag("tasbeeh_tap_button")
            ) {
                // Circular Progress Arc
                val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                val primaryColor = MaterialTheme.colorScheme.primary
                val goldColor = Color(0xFFD4AF37)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeftOffset = strokeWidth / 2

                    // Background Track Ring
                    drawCircle(
                        color = trackColor,
                        radius = diameter / 2,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active Progress Arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, goldColor, primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = progressFraction * 360f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset),
                        size = androidx.compose.ui.geometry.Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Counter Body
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(2.dp, Color(0xFFD4AF37).copy(alpha = 0.35f)),
                    modifier = Modifier.size(216.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Round & Percentage Badge
                        Text(
                            text = "${(progressFraction * 100).toInt()}% • ${AppLocalization.get("rounds", language)} ${currentRounds + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Large Digital Number with animated content
                        AnimatedContent(
                            targetState = currentCount,
                            label = "counter_number"
                        ) { countValue ->
                            Text(
                                text = countValue.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Target Subtitle
                        Text(
                            text = "/ $currentTarget",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // TAP TO COUNT label
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = AppLocalization.get("tap_to_count", language),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Physical Controls Toolbar (Undo, Big +, Reset, Save Session)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Undo Button (Decreases count by 1)
                IconButton(
                    onClick = {
                        repository.undo()
                        playFeedback(false)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("tasbeeh_undo_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Prominent Main Tap Button
                Button(
                    onClick = { onCountTap() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(72.dp)
                        .testTag("tasbeeh_plus_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Count +1",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Save Session to History Button
                IconButton(
                    onClick = {
                        if (currentCount > 0) {
                            repository.saveHistorySession(isCompleted = currentCount >= currentTarget)
                            android.widget.Toast.makeText(context, "Session saved to history", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Count is at zero", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("tasbeeh_save_session_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = "Save Session",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Target Selector Row (33, 34, 99, 100, 500, 1000, Custom)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                val targets = listOf(33, 34, 99, 100, 500, 1000)
                items(targets) { tgt ->
                    FilterChip(
                        selected = currentTarget == tgt,
                        onClick = {
                            // User changes target without losing current count
                            repository.setTarget(tgt)
                        },
                        label = { Text("$tgt", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("tasbeeh_target_chip_$tgt")
                    )
                }
                item {
                    AssistChip(
                        onClick = { showCustomTargetDialog = true },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                        },
                        label = { Text(AppLocalization.get("custom_target", language)) },
                        modifier = Modifier.testTag("tasbeeh_custom_target_chip")
                    )
                }
            }

            // 6. Quick Screen Awake Toggle Pill at bottom
            Surface(
                onClick = { repository.toggleKeepScreenOn() },
                shape = RoundedCornerShape(12.dp),
                color = if (keepScreenOn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (keepScreenOn) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (keepScreenOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${AppLocalization.get("screen_awake", language)}: ${if (keepScreenOn) "ON" else "OFF"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = if (keepScreenOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- DIALOGS AND BOTTOM SHEETS ---

        // A. Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(AppLocalization.get("reset", language)) },
                text = { Text("Are you sure you want to reset current count to 0? (Daily statistics will remain saved)") },
                confirmButton = {
                    TextButton(onClick = {
                        repository.reset()
                        showResetDialog = false
                    }) {
                        Text(AppLocalization.get("reset", language), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
                }
            )
        }

        // B. Target Reached Celebration Dialog
        if (showTargetReachedDialog) {
            AlertDialog(
                onDismissRequest = { showTargetReachedDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = AppLocalization.get("target_completed", language),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = activeDhikr.arabic,
                            fontFamily = QuranNaskhFontFamily,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Target of $currentTarget completed! (Total rounds: $currentRounds)",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        if (activeDhikr.virtueOrHadith.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = activeDhikr.virtueOrHadith,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        repository.saveHistorySession(isCompleted = true)
                        repository.nextRound()
                        showTargetReachedDialog = false
                    }) {
                        Text("Next Round")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTargetReachedDialog = false }) {
                        Text("Continue Counting")
                    }
                }
            )
        }

        // C. Select Zikr Bottom Sheet (All 9 Presets + Custom Dhikrs)
        if (showDhikrPickerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDhikrPickerSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Preset & Custom Zikr",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Button(
                            onClick = {
                                showDhikrPickerSheet = false
                                showCustomDhikrDialog = true
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppLocalization.get("add_custom_dhikr", language))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allDhikrs) { dhikr ->
                            val isSelected = dhikr.id == selectedDhikrId
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        repository.selectDhikr(dhikr)
                                        showDhikrPickerSheet = false
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dhikr.arabic,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = QuranNaskhFontFamily,
                                                fontSize = 20.sp
                                            ),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = dhikr.translit,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val m = when (language) {
                                            AppLanguage.URDU -> dhikr.meaningUrdu
                                            AppLanguage.HINDI -> dhikr.meaningHindi
                                            AppLanguage.ARABIC -> dhikr.arabic
                                            AppLanguage.ENGLISH -> dhikr.meaningEn
                                        }
                                        Text(
                                            text = m,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
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
        }

        // D. Custom Target Input Dialog
        if (showCustomTargetDialog) {
            var inputTarget by remember { mutableStateOf(currentTarget.toString()) }
            AlertDialog(
                onDismissRequest = { showCustomTargetDialog = false },
                title = { Text(AppLocalization.get("custom_target", language)) },
                text = {
                    Column {
                        Text("Enter your desired counting target (e.g. 33, 100, 500, 1000):")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputTarget,
                            onValueChange = { inputTarget = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Target count") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val num = inputTarget.toIntOrNull()
                        if (num != null && num > 0) {
                            repository.setTarget(num)
                        }
                        showCustomTargetDialog = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomTargetDialog = false }) { Text("Cancel") }
                }
            )
        }

        // E. Add Custom Zikr Dialog
        if (showCustomDhikrDialog) {
            var customArabic by remember { mutableStateOf("") }
            var customTranslit by remember { mutableStateOf("") }
            var customMeaning by remember { mutableStateOf("") }
            var customTargetText by remember { mutableStateOf("33") }

            AlertDialog(
                onDismissRequest = { showCustomDhikrDialog = false },
                title = { Text(AppLocalization.get("add_custom_dhikr", language)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customArabic,
                            onValueChange = { customArabic = it },
                            label = { Text("Arabic Text") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customTranslit,
                            onValueChange = { customTranslit = it },
                            label = { Text("Title / Transliteration") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customMeaning,
                            onValueChange = { customMeaning = it },
                            label = { Text("Meaning / Translation") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = customTargetText,
                            onValueChange = { customTargetText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Recommended Target (e.g. 33)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (customArabic.isNotBlank() || customTranslit.isNotBlank()) {
                            val tgt = customTargetText.toIntOrNull() ?: 33
                            repository.addCustomDhikr(
                                arabic = if (customArabic.isNotBlank()) customArabic else customTranslit,
                                translit = if (customTranslit.isNotBlank()) customTranslit else "Custom Zikr",
                                meaning = customMeaning,
                                defaultTarget = tgt
                            )
                        }
                        showCustomDhikrDialog = false
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomDhikrDialog = false }) { Text("Cancel") }
                }
            )
        }

        // F. Statistics Bottom Sheet
        if (showStatsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStatsSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = AppLocalization.get("statistics", language),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary Stats Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = AppLocalization.get("today_total", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$todayTotal",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Completed Targets",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$todayCompletedTargets",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // All-Time Grand Total
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = AppLocalization.get("all_time_total", language),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "All recorded Tasbeeh on device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "$allTimeTotal",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Zikr-wise Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (dhikrBreakdown.isEmpty()) {
                        Text(
                            text = "No recitations recorded yet. Start tapping to view breakdown.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(dhikrBreakdown.toList()) { (title, count) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text("$count", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }

        // G. History Bottom Sheet
        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = AppLocalization.get("history", language),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (history.isNotEmpty()) {
                            TextButton(onClick = { showClearHistoryDialog = true }) {
                                Text("Clear All", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (history.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No saved Tasbeeh sessions yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap the bookmark icon to save your current counting session.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(history, key = { it.id }) { item ->
                                val dateStr = remember(item.timestamp) {
                                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
                                }
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.dhikrArabic,
                                                style = MaterialTheme.typography.titleSmall.copy(fontFamily = QuranNaskhFontFamily),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = item.dhikrTitle,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "$dateStr • Count: ${item.count} / ${item.target}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (item.isTargetCompleted) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "Completed",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }

                                            IconButton(
                                                onClick = { itemToDeleteHistory = item },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete record",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(18.dp)
                                                )
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
            }
        }

        // H. Delete Single History Item Confirmation
        if (itemToDeleteHistory != null) {
            val item = itemToDeleteHistory!!
            AlertDialog(
                onDismissRequest = { itemToDeleteHistory = null },
                title = { Text("Delete History Record?") },
                text = { Text("Delete session for '${item.dhikrTitle}' (${item.count} counts)?") },
                confirmButton = {
                    TextButton(onClick = {
                        repository.deleteHistoryItem(item.id)
                        itemToDeleteHistory = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDeleteHistory = null }) { Text("Cancel") }
                }
            )
        }

        // I. Clear All History Confirmation
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text("Clear All History?") },
                text = { Text("Are you sure you want to permanently clear all saved Tasbeeh sessions?") },
                confirmButton = {
                    TextButton(onClick = {
                        repository.clearAllHistory()
                        showClearHistoryDialog = false
                    }) {
                        Text("Clear All", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
