package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.AppLocationManager
import com.example.data.model.AppLanguage
import com.example.data.repository.IslamicCalendarRepository
import com.example.data.repository.IslamicCalendarEvent
import com.example.localization.AppLocalization
import com.example.ui.components.NoorTopBar
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriCalendarScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Initialize repository
    LaunchedEffect(Unit) {
        IslamicCalendarRepository.init(context)
    }

    val userLocation by AppLocationManager.currentLocation.collectAsState()
    val manualAdjustment by IslamicCalendarRepository.manualAdjustmentDays.collectAsState()
    val usePakistanOfficial by IslamicCalendarRepository.usePakistanOfficial.collectAsState()

    var activeTab by remember { mutableStateOf("calendar") } // "calendar", "events", "ramadan"

    // Calendar Navigation State
    val todayCal = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) } // 0-11

    val currentDisplayedCal = remember(displayedYear, displayedMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, displayedYear)
            set(Calendar.MONTH, displayedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val currentTodayHijri = remember(todayCal, manualAdjustment, usePakistanOfficial) {
        IslamicCalendarRepository.getHijriDate(todayCal, manualAdjustment)
    }

    Scaffold(
        topBar = {
            NoorTopBar(
                title = AppLocalization.get("calendar", language),
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
            // Tab Selector: Dual Calendar / Islamic Events / Ramadan Schedule
            TabRow(
                selectedTabIndex = when (activeTab) {
                    "events" -> 1
                    "ramadan" -> 2
                    else -> 0
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == "calendar",
                    onClick = { activeTab = "calendar" },
                    text = { Text("Dual Calendar") },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == "events",
                    onClick = { activeTab = "events" },
                    text = { Text("Islamic Events") },
                    icon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == "ramadan",
                    onClick = { activeTab = "ramadan" },
                    text = { Text("Ramadan Schedule") },
                    icon = { Icon(Icons.Default.NightsStay, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (activeTab) {
                "calendar" -> {
                    CalendarView(
                        userLocation = userLocation,
                        language = language,
                        todayCal = todayCal,
                        currentDisplayedCal = currentDisplayedCal,
                        currentTodayHijri = currentTodayHijri,
                        displayedYear = displayedYear,
                        displayedMonth = displayedMonth,
                        manualAdjustment = manualAdjustment,
                        usePakistanOfficial = usePakistanOfficial,
                        onPrevMonth = {
                            if (displayedMonth == 0) {
                                displayedMonth = 11
                                displayedYear--
                            } else {
                                displayedMonth--
                            }
                        },
                        onNextMonth = {
                            if (displayedMonth == 11) {
                                displayedMonth = 0
                                displayedYear++
                            } else {
                                displayedMonth++
                            }
                        },
                        onResetToToday = {
                            displayedYear = todayCal.get(Calendar.YEAR)
                            displayedMonth = todayCal.get(Calendar.MONTH)
                        },
                        onAdjustmentChange = { newOffset ->
                            IslamicCalendarRepository.setManualAdjustment(context, newOffset)
                        },
                        onSourceToggle = { useOfficial ->
                            IslamicCalendarRepository.setUsePakistanOfficial(context, useOfficial)
                        }
                    )
                }
                "events" -> {
                    IslamicEventsView(language = language)
                }
                "ramadan" -> {
                    RamadanScheduleView(
                        userLocation = userLocation,
                        language = language
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    userLocation: com.example.data.location.UserLocation,
    language: AppLanguage,
    todayCal: Calendar,
    currentDisplayedCal: Calendar,
    currentTodayHijri: com.example.data.repository.HijriDate,
    displayedYear: Int,
    displayedMonth: Int,
    manualAdjustment: Int,
    usePakistanOfficial: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetToToday: () -> Unit,
    onAdjustmentChange: (Int) -> Unit,
    onSourceToggle: (Boolean) -> Unit
) {
    val monthNamesEn = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    // Calculate days for the grid
    val daysInMonth = currentDisplayedCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = currentDisplayedCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
    val leadingEmptySpaces = (firstDayOfWeek - 1) % 7

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Today's Dual Date Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (userLocation.isGpsFix) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${userLocation.cityName}${if (userLocation.countryName.isNotBlank()) ", " + userLocation.countryName else ""} • ${userLocation.timeZoneId}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "Today's Verified Islamic & Gregorian Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${currentTodayHijri.day} ${currentTodayHijri.monthNameEn} ${currentTodayHijri.year} AH",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${currentTodayHijri.day} ${currentTodayHijri.monthNameUr} ${currentTodayHijri.year}ھ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(
                            Locale.US,
                            "Gregorian: %s, %02d %s %d",
                            todayCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US),
                            todayCal.get(Calendar.DAY_OF_MONTH),
                            todayCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US),
                            todayCal.get(Calendar.YEAR)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Source: ${currentTodayHijri.sourceDescription}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Ruet-e-Hilal vs Astronomical Mode & Manual Adjustment
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Calendar Convention & Moon Sighting",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Astronomical tabular estimate",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "No live official Pakistan moon-sighting data is bundled. Confirm dates with local announcements.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Official sighting mode is intentionally not offered without verified data.
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Local Sighting Adjustment:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (manualAdjustment == 0) "No manual adjustment" else "${if (manualAdjustment > 0) "+$manualAdjustment" else "$manualAdjustment"} day(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onAdjustmentChange((manualAdjustment - 1).coerceAtLeast(-2)) },
                                modifier = Modifier.size(36.dp).testTag("moon_adjust_minus")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Minus Day")
                            }

                            Text(
                                text = "${if (manualAdjustment > 0) "+$manualAdjustment" else "$manualAdjustment"}",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = { onAdjustmentChange((manualAdjustment + 1).coerceAtMost(2)) },
                                modifier = Modifier.size(36.dp).testTag("moon_adjust_plus")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Plus Day")
                            }
                        }
                    }
                }
            }
        }

        // Dual Monthly Calendar Grid Header & Nav
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${monthNamesEn[displayedMonth]} $displayedYear",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(
                                onClick = onResetToToday,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Today", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Row {
                            IconButton(onClick = onPrevMonth) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month")
                            }
                            IconButton(onClick = onNextMonth) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day of Week Header (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (dayLabel == "Fri") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Grid Cells
                    val totalCells = leadingEmptySpaces + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (r in 0 until rows) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            for (c in 0 until 7) {
                                val cellIndex = r * 7 + c
                                val dayNum = cellIndex - leadingEmptySpaces + 1

                                if (dayNum in 1..daysInMonth) {
                                    val cellCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, displayedYear)
                                        set(Calendar.MONTH, displayedMonth)
                                        set(Calendar.DAY_OF_MONTH, dayNum)
                                    }
                                    val cellHijri = IslamicCalendarRepository.getHijriDate(cellCal, manualAdjustment)
                                    val isToday = (todayCal.get(Calendar.YEAR) == displayedYear &&
                                            todayCal.get(Calendar.MONTH) == displayedMonth &&
                                            todayCal.get(Calendar.DAY_OF_MONTH) == dayNum)

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                            )
                                            .border(
                                                width = if (isToday) 1.5.dp else 0.5.dp,
                                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${cellHijri.day}",
                                                fontSize = 9.sp,
                                                color = Color(0xFFC88A00),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicEventsView(language: AppLanguage) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Significant Islamic Events & Occasions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Dates marked with 'Moon Sighting' are subject to official visual confirmation by the Central Ruet-e-Hilal Committee.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(IslamicCalendarRepository.ISLAMIC_EVENTS) { event ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${event.hijriDay} ${event.hijriMonthNameEn}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (event.isMoonSightingDependent) Color(0xFFFFF3CD) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (event.isMoonSightingDependent) "🌙 Moon Sighting Dependent" else "✓ Established Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (event.isMoonSightingDependent) Color(0xFF856404) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = event.titleEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.titleUr,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = event.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RamadanScheduleView(
    userLocation: com.example.data.location.UserLocation,
    language: AppLanguage
) {
    val context = LocalContext.current
    val ramadanStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.FEBRUARY)
            set(Calendar.DAY_OF_MONTH, 18) // Projected 1st Ramadan 1447 / 1448
        }
    }

    val schedule = remember(userLocation) {
        IslamicCalendarRepository.generateRamadanSchedule(
            latitude = userLocation.latitude,
            longitude = userLocation.longitude,
            timeZoneId = userLocation.timeZoneId,
            gregorianStartCal = ramadanStart
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🌙 Dedicated Ramadan Timetable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calculated for ${userLocation.cityName} (${userLocation.countryName}) using precise local sunset (Iftar) and true dawn (Sehri) solar calculations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Text("Day", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Date", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Sehri (Fajr)", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Iftar (Maghrib)", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        items(schedule) { day ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                ) {
                    Text("Day ${day.ramadanDay}", modifier = Modifier.weight(0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("${day.dayOfWeek}, ${day.gregorianDateStr}", modifier = Modifier.weight(1.3f), fontSize = 12.sp)
                    Text(day.sehriTime, modifier = Modifier.weight(1.3f), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(day.iftarTime, modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC88A00))
                }
            }
        }

        item {
            Button(
                onClick = {
                    val shareText = buildString {
                        append("Ramadan Timetable - ${userLocation.cityName}\n")
                        append("Day | Date | Sehri | Iftar\n")
                        schedule.forEach { d ->
                            append("Day ${d.ramadanDay} | ${d.gregorianDateStr} | ${d.sehriTime} | ${d.iftarTime}\n")
                        }
                        append("\nNoor-e-Hidayat 2")
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Ramadan Schedule"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Full Ramadan Timetable")
            }
        }
    }
}
